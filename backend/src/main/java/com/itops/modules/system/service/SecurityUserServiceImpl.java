package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.itops.modules.system.entity.SysMenu;
import com.itops.modules.system.entity.SysRole;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.mapper.SysMenuMapper;
import com.itops.modules.system.mapper.SysRoleMapper;
import com.itops.modules.system.mapper.SysUserMapper;
import com.itops.security.LoginUser;
import com.itops.security.SecurityUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户装配: 用户 -> 角色 -> 数据范围 -> 权限标识
 */
@Service
@RequiredArgsConstructor
public class SecurityUserServiceImpl implements SecurityUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;

    @Override
    public LoginUser loadByUsername(String username) {
        SysUser user = userMapper.selectByUsername(username);
        return user == null ? null : build(user);
    }

    @Override
    public LoginUser loadById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        return user == null ? null : build(user);
    }

    private LoginUser build(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRealName(user.getRealName());
        loginUser.setEnabled("0".equals(user.getStatus()));

        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getUserId());
        Set<String> roleKeys = roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet());
        loginUser.setRoles(roleKeys);

        loginUser.setRoleScopes(roles.stream()
                .map(r -> new LoginUser.RoleScope(
                        r.getDataScope(),
                        "2".equals(r.getDataScope()) ? roleMapper.selectCustomDeptIds(r.getRoleId()) : null))
                .toList());

        List<SysMenu> menus = user.getUserId() == 1L
                ? menuMapper.selectAllMenus()
                : menuMapper.selectMenusByUserId(user.getUserId());
        Set<String> perms = menus.stream()
                .map(SysMenu::getPerms)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
        loginUser.setPermissions(perms);
        return loginUser;
    }
}
