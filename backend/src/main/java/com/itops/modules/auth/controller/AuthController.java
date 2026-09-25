package com.itops.modules.auth.controller;

import com.itops.common.api.R;
import com.itops.modules.auth.dto.LoginBody;
import com.itops.modules.auth.service.AuthService;
import com.itops.modules.auth.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@Valid @RequestBody LoginBody body, HttpServletRequest request) {
        return R.ok(authService.login(body, request));
    }

    @Operation(summary = "当前登录用户信息(角色/权限/菜单)")
    @GetMapping("/info")
    public R<UserInfoVO> info() {
        return R.ok(authService.info());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        // JWT 无状态, 前端清除令牌即可
        return R.ok();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    public R<Void> password(@RequestBody Map<String, String> body) {
        authService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return R.ok();
    }
}
