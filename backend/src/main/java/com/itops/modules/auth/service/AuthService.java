package com.itops.modules.auth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.itops.common.exception.BusinessException;
import com.itops.modules.auth.dto.LoginBody;
import com.itops.modules.auth.vo.UserInfoVO;
import com.itops.modules.system.entity.SysLoginLog;
import com.itops.modules.system.entity.SysMenu;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.service.SysLogService;
import com.itops.modules.system.service.SysMenuService;
import com.itops.modules.system.service.SysUserService;
import com.itops.security.JwtUtils;
import com.itops.security.LoginUser;
import com.itops.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysMenuService menuService;
    private final SysLogService logService;
    private final LoginAttemptService loginAttemptService;

    public Map<String, Object> login(LoginBody body, HttpServletRequest request) {
        String clientIp = resolveIp(request);
        // 防爆破: 锁定期内直接拒绝
        loginAttemptService.checkLocked(body.getUsername(), clientIp);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword()));
        } catch (Exception e) {
            loginAttemptService.recordFailure(body.getUsername(), clientIp);
            recordLoginLog(body.getUsername(), request, "1", "账号或密码错误");
            throw new BusinessException(401, "账号或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if (!loginUser.isEnabled()) {
            loginAttemptService.recordFailure(body.getUsername(), clientIp);
            recordLoginLog(body.getUsername(), request, "1", "账号已停用");
            throw new BusinessException(401, "账号已停用，请联系管理员");
        }
        loginAttemptService.recordSuccess(body.getUsername(), clientIp);

        String token = jwtUtils.createToken(loginUser.getUserId(), loginUser.getUsername());
        userService.updateLoginInfo(loginUser.getUserId(), resolveIp(request));
        recordLoginLog(body.getUsername(), request, "0", "登录成功");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", loginUser.getUserId());
        result.put("username", loginUser.getUsername());
        result.put("realName", loginUser.getRealName());
        return result;
    }

    public UserInfoVO info() {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        SysUser user = userService.detail(loginUser.getUserId());
        UserInfoVO vo = new UserInfoVO();
        vo.setUser(user);
        vo.setRoles(loginUser.getRoles() == null ? List.of() : List.copyOf(loginUser.getRoles()));
        vo.setPermissions(loginUser.getPermissions());
        vo.setMenus(menuService.currentUserRouters());
        return vo;
    }

    public void changePassword(String oldPassword, String newPassword) {
        LoginUser loginUser = SecurityUtils.requireLoginUser();
        if (StrUtil.isBlank(newPassword) || newPassword.length() < 6) {
            throw BusinessException.of("新密码长度不能少于6位");
        }
        userService.changePassword(loginUser.getUserId(), oldPassword, newPassword);
    }

    private void recordLoginLog(String username, HttpServletRequest request, String status, String msg) {
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(username);
        loginLog.setStatus(status);
        loginLog.setMsg(msg);
        loginLog.setLoginTime(LocalDateTime.now());
        if (request != null) {
            loginLog.setIpaddr(resolveIp(request));
            UserAgent ua = UserAgentUtil.parse(request.getHeader("User-Agent"));
            if (ua != null) {
                loginLog.setBrowser(ua.getBrowser() == null ? null : ua.getBrowser().getName());
                loginLog.setOs(ua.getOs() == null ? null : ua.getOs().getName());
            }
        }
        logService.saveLoginLog(loginLog);
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return StrUtil.split(ip, ',').get(0);
    }
}
