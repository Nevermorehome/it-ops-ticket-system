package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.common.util.TreeUtils;
import com.itops.modules.system.entity.SysMenu;
import com.itops.modules.system.mapper.SysMenuMapper;
import com.itops.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    /** 全部菜单(平铺, 管理用) */
    public List<SysMenu> listAll(String menuName, String status) {
        return list(Wrappers.<SysMenu>lambdaQuery()
                .like(menuName != null && !menuName.isBlank(), SysMenu::getMenuName, menuName)
                .eq(status != null && !status.isBlank(), SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getParentId).orderByAsc(SysMenu::getOrderNum));
    }

    /** 菜单树 */
    public List<SysMenu> tree(String menuName, String status) {
        return TreeUtils.build(listAll(menuName, status),
                SysMenu::getMenuId, SysMenu::getParentId,
                SysMenu::getChildren, SysMenu::setChildren,
                Comparator.comparing(SysMenu::getOrderNum));
    }

    /** 当前登录用户的路由菜单(目录+菜单, 不含按钮) */
    public List<SysMenu> currentUserRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = userId != null && userId == 1L
                ? baseMapper.selectAllMenus()
                : baseMapper.selectMenusByUserId(userId);
        List<SysMenu> routers = menus.stream()
                .filter(m -> "M".equals(m.getMenuType()) || "C".equals(m.getMenuType()))
                .filter(m -> "0".equals(m.getVisible()))
                .toList();
        return TreeUtils.build(routers,
                SysMenu::getMenuId, SysMenu::getParentId,
                SysMenu::getChildren, SysMenu::setChildren,
                Comparator.comparing(SysMenu::getOrderNum));
    }

    public void create(SysMenu menu) {
        verify(menu);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        save(menu);
    }

    public void update(SysMenu menu) {
        verify(menu);
        if (menu.getMenuId().equals(menu.getParentId())) {
            throw BusinessException.of("上级菜单不能选择自身");
        }
        updateById(menu);
    }

    public void remove(Long menuId) {
        long children = count(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, menuId));
        if (children > 0) {
            throw BusinessException.of("存在子菜单，不允许删除");
        }
        if (baseMapper.countRoleRef(menuId) > 0) {
            throw BusinessException.of("菜单已分配给角色，不允许删除");
        }
        removeById(menuId);
    }

    private void verify(SysMenu menu) {
        if (menu.getMenuName() == null || menu.getMenuName().isBlank()) {
            throw BusinessException.of("菜单名称不能为空");
        }
    }
}
