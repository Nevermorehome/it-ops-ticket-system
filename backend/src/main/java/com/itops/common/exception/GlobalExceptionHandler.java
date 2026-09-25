package com.itops.common.exception;

import com.itops.common.api.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常(保留自定义 code, 如 403) */
    @ExceptionHandler(BusinessException.class)
    public R<Void> businessException(HttpServletRequest request, BusinessException e) {
        log.warn("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** Spring Security 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> accessDenied(AccessDeniedException e) {
        return R.fail(403, "没有访问权限");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> methodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> bindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(400, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> missingParam(MissingServletRequestParameterException e) {
        return R.fail(400, "缺少请求参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> methodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(405, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> illegalArgument(IllegalArgumentException e) {
        return R.fail(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> exception(HttpServletRequest request, Exception e) {
        log.error("系统异常: {}", request.getRequestURI(), e);
        return R.fail(500, "系统繁忙，请稍后再试");
    }
}
