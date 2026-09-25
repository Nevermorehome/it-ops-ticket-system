package com.itops.modules.system.controller;

import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.system.entity.SysMenu;
import com.itops.modules.system.service.SysMenuService;
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

@Tag(name = "系统管理-菜单")
@RestController
@RequestMapping("/api/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "菜单列表(平铺)")
    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) String status) {
        return R.ok(menuService.listAll(menuName, status));
    }

    @Operation(summary = "菜单树")
    @PreAuthorize("hasAuthority('system:menu:list')")
    @GetMapping("/tree")
    public R<List<SysMenu>> tree(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) String status) {
        return R.ok(menuService.tree(menuName, status));
    }

    @OperLog(title = "菜单管理", businessType = 1)
    @PreAuthorize("hasAuthority('system:menu:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysMenu menu) {
        menuService.create(menu);
        return R.ok();
    }

    @OperLog(title = "菜单管理", businessType = 2)
    @PreAuthorize("hasAuthority('system:menu:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        menuService.update(menu);
        return R.ok();
    }

    @OperLog(title = "菜单管理", businessType = 3)
    @PreAuthorize("hasAuthority('system:menu:remove')")
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        menuService.remove(menuId);
        return R.ok();
    }
}
