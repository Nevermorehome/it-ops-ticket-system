package com.itops.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysRole;
import com.itops.modules.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "系统管理-角色")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "角色分页")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/page")
    public R<IPage<SysRole>> page(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize,
                                  @RequestParam(required = false) String roleName,
                                  @RequestParam(required = false) String status) {
        List<SysRole> all = roleService.listAll(roleName, status);
        int from = (int) Math.min((pageNum - 1) * pageSize, all.size());
        int to = (int) Math.min(from + pageSize, all.size());
        Page<SysRole> page = new Page<>(pageNum, pageSize, all.size());
        page.setRecords(all.subList(from, to));
        return R.ok(page);
    }

    @Operation(summary = "全部可用角色(下拉)")
    @GetMapping("/all")
    public R<List<SysRole>> all() {
        return R.ok(roleService.listAll(null, "0"));
    }

    @Operation(summary = "角色详情(含菜单/数据部门)")
    @PreAuthorize("hasAuthority('system:role:list')")
    @GetMapping("/{roleId}")
    public R<SysRole> detail(@PathVariable Long roleId) {
        return R.ok(roleService.detail(roleId));
    }

    @OperLog(title = "角色管理", businessType = 1)
    @PreAuthorize("hasAuthority('system:role:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysRole role) {
        roleService.create(role);
        return R.ok();
    }

    @OperLog(title = "角色管理", businessType = 2)
    @PreAuthorize("hasAuthority('system:role:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        roleService.update(role);
        return R.ok();
    }

    @OperLog(title = "角色管理", businessType = 3)
    @PreAuthorize("hasAuthority('system:role:remove')")
    @DeleteMapping("/{roleId}")
    public R<Void> remove(@PathVariable Long roleId) {
        roleService.remove(roleId);
        return R.ok();
    }
}
