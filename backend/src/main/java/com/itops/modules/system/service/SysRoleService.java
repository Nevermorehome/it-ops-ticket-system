package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.system.entity.SysRole;
import com.itops.modules.system.mapper.SysRelationMapper;
import com.itops.modules.system.mapper.SysRoleMapper;
import com.itops.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    private final SysRelationMapper relationMapper;
    private final SysUserMapper userMapper;

    public List<SysRole> listAll(String roleName, String status) {
        return list(Wrappers.<SysRole>lambdaQuery()
                .like(roleName != null && !roleName.isBlank(), SysRole::getRoleName, roleName)
                .eq(status != null && !status.isBlank(), SysRole::getStatus, status)
                .orderByAsc(SysRole::getOrderNum));
    }

    /** 用户拥有的角色 */
    public List<SysRole> rolesOfUser(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    /** 角色详情含已选菜单与自定义数据部门 */
    public SysRole detail(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) {
            throw BusinessException.of("角色不存在");
        }
        role.setMenuIds(relationMapper.selectMenuIdsByRoleId(roleId));
        role.setDeptIds(relationMapper.selectDeptIdsByRoleId(roleId));
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(SysRole role) {
        verify(role);
        save(role);
        saveRelations(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysRole role) {
        verify(role);
        updateById(role);
        if (role.getMenuIds() != null) {
            relationMapper.deleteRoleMenus(role.getRoleId());
        }
        if (role.getDeptIds() != null) {
            relationMapper.deleteRoleDepts(role.getRoleId());
        }
        saveRelations(role);
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long roleId) {
        long users = userMapper.selectCount(Wrappers.<com.itops.modules.system.entity.SysUser>lambdaQuery()
                .inSql(com.itops.modules.system.entity.SysUser::getUserId,
                        "SELECT user_id FROM sys_user_role WHERE role_id = " + roleId));
        if (users > 0) {
            throw BusinessException.of("角色已分配给用户，不允许删除");
        }
        relationMapper.deleteRoleMenus(roleId);
        relationMapper.deleteRoleDepts(roleId);
        removeById(roleId);
    }

    private void saveRelations(SysRole role) {
        if (role.getMenuIds() != null) {
            role.getMenuIds().forEach(menuId -> relationMapper.insertRoleMenu(role.getRoleId(), menuId));
        }
        if ("2".equals(role.getDataScope()) && role.getDeptIds() != null) {
            role.getDeptIds().forEach(deptId -> relationMapper.insertRoleDept(role.getRoleId(), deptId));
        }
    }

    private void verify(SysRole role) {
        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw BusinessException.of("角色名称不能为空");
        }
        if (role.getRoleKey() == null || role.getRoleKey().isBlank()) {
            throw BusinessException.of("权限标识不能为空");
        }
        long count = count(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleKey, role.getRoleKey())
                .ne(role.getRoleId() != null, SysRole::getRoleId, role.getRoleId()));
        if (count > 0) {
            throw BusinessException.of("角色权限标识已存在");
        }
    }
}
