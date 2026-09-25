package com.itops.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysRole;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "用户分页")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/page")
    public R<IPage<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize,
                                  @RequestParam(required = false) String username,
                                  @RequestParam(required = false) String realName,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) Long deptId,
                                  @RequestParam(required = false) String status) {
        return R.ok(userService.page(pageNum, pageSize, username, realName, phone, deptId, status));
    }

    @Operation(summary = "用户精简列表(派单选人, 登录即可用)")
    @GetMapping("/options")
    public R<List<SysUser>> options(@RequestParam(required = false) String keyword) {
        return R.ok(userService.options(keyword));
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('system:user:list')")
    @GetMapping("/{userId}")
    public R<Map<String, Object>> detail(@PathVariable Long userId) {
        List<Long> roleIds = userService.roleIdsOfUser(userId);
        List<SysRole> roles = userService.rolesOfUser(userId);
        return R.ok(Map.of("user", userService.detail(userId), "roleIds", roleIds, "roles", roles));
    }

    @OperLog(title = "用户管理", businessType = 1)
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping
    public R<Void> create(@RequestBody UserSaveBody body) {
        userService.create(body.getUser(), body.getRoleIds());
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = 2)
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping
    public R<Void> update(@RequestBody UserSaveBody body) {
        userService.update(body.getUser(), body.getRoleIds());
        return R.ok();
    }

    @OperLog(title = "用户管理", businessType = 3)
    @PreAuthorize("hasAuthority('system:user:remove')")
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable List<Long> userIds) {
        userIds.forEach(userService::remove);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @OperLog(title = "用户管理-重置密码", businessType = 2)
    @PreAuthorize("hasAuthority('system:user:reset')")
    @PutMapping("/reset-pwd")
    public R<Void> resetPassword(@RequestBody Map<String, String> body) {
        userService.resetPassword(Long.valueOf(body.get("userId")), body.get("newPassword"));
        return R.ok();
    }

    @Data
    public static class UserSaveBody {
        private SysUser user;
        private List<Long> roleIds;
    }
}
