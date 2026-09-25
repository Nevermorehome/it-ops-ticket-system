package com.itops.modules.system.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysOperationLog;
import com.itops.modules.system.service.SysLogService;
import com.itops.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 操作日志切面: 拦截 @OperLog 标注的 Controller 方法, 异步落库
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    /** 请求开始时间 */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("operLogCostStart");

    private final SysLogService sysLogService;
    private final ObjectMapper objectMapper;

    @Before(value = "@annotation(operLog)", argNames = "joinPoint,operLog")
    public void doBefore(JoinPoint joinPoint, OperLog operLog) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "@annotation(operLog)", returning = "jsonResult", argNames = "joinPoint,operLog,jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, OperLog operLog, Object jsonResult) {
        handleLog(joinPoint, operLog, null, jsonResult);
    }

    @AfterThrowing(pointcut = "@annotation(operLog)", throwing = "e", argNames = "joinPoint,operLog,e")
    public void doAfterThrowing(JoinPoint joinPoint, OperLog operLog, Exception e) {
        handleLog(joinPoint, operLog, e, null);
    }

    private void handleLog(JoinPoint joinPoint, OperLog operLog, Exception e, Object jsonResult) {
        try {
            SysOperationLog logEntity = new SysOperationLog();
            logEntity.setTitle(operLog.title());
            logEntity.setBusinessType(operLog.businessType());
            logEntity.setStatus(e == null ? "0" : "1");
            logEntity.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + "()");
            logEntity.setOperName(SecurityUtils.getUsername());
            logEntity.setOperTime(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntity.setRequestMethod(request.getMethod());
                logEntity.setOperUrl(truncate(request.getRequestURI(), 255));
                logEntity.setOperIp(getClientIp(request));
            }
            logEntity.setOperParam(truncate(argsToJson(joinPoint.getArgs()), 2000));
            if (jsonResult != null) {
                logEntity.setJsonResult(truncate(toJson(jsonResult), 2000));
            }
            if (e != null) {
                logEntity.setErrorMsg(truncate(e.getMessage(), 2000));
            }
            Long start = TIME_THREADLOCAL.get();
            logEntity.setCostTime(start == null ? 0L : System.currentTimeMillis() - start);

            sysLogService.saveOperationLog(logEntity);
        } catch (Exception ex) {
            log.warn("记录操作日志失败: {}", ex.getMessage());
        } finally {
            TIME_THREADLOCAL.remove();
        }
    }

    /** 过滤不可序列化的参数后转 JSON */
    private String argsToJson(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        String json = Arrays.stream(args)
                .filter(arg -> !(arg instanceof HttpServletRequest)
                        && !(arg instanceof HttpServletResponse)
                        && !(arg instanceof MultipartFile)
                        && !(arg instanceof jakarta.servlet.http.Part))
                .map(arg -> {
                    if (arg instanceof MultipartFile[] || arg instanceof jakarta.servlet.http.Part[]) {
                        return "[file]";
                    }
                    return toJson(arg);
                })
                .collect(Collectors.joining(" "));
        return json;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception ex) {
            return String.valueOf(obj);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
