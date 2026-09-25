package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.modules.system.entity.SysRole;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.mapper.SysRelationMapper;
import com.itops.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysRelationMapper relationMapper;
    private final SysRoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @com.itops.security.datascope.DataScope(deptColumn = "dept_id", selfSql = " user_id = {userId} ")
    public IPage<SysUser> page(long pageNum, long pageSize, String username, String realName,
                               String phone, Long deptId, String status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, Wrappers.<SysUser>lambdaQuery()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(realName), SysUser::getRealName, realName)
                .like(StringUtils.hasText(phone), SysUser::getPhone, phone)
                .eq(deptId != null, SysUser::getDeptId, deptId)
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .last(com.itops.security.datascope.DataScopeContext.get())
                .orderByDesc(SysUser::getUserId));
    }

    /** 指派人/协同人选择器用精简列表(仅启用用户) */
    public List<SysUser> options(String keyword) {
        return list(Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getUserId, SysUser::getRealName, SysUser::getUsername,
                        SysUser::getPhone, SysUser::getDeptId)
                .eq(SysUser::getStatus, "0")
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getRealName, keyword)
                        .or().like(SysUser::getUsername, keyword))
                .orderByAsc(SysUser::getUserId)
                .last("LIMIT 100"));
    }

    public SysUser detail(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public List<Long> roleIdsOfUser(Long userId) {
        return baseMapper.selectRoleIds(userId);
    }

    public List<SysRole> rolesOfUser(Long userId) {
        return roleService.rolesOfUser(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(SysUser user, List<Long> roleIds) {
        long count = count(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw BusinessException.of("登录账号已存在");
        }
        String rawPwd = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
        user.setPassword(passwordEncoder.encode(rawPwd));
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("0");
        }
        save(user);
        saveRoles(user.getUserId(), roleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysUser user, List<Long> roleIds) {
        SysUser db = getById(user.getUserId());
        if (db == null) {
            throw BusinessException.of("用户不存在");
        }
        user.setUsername(null);
        user.setPassword(null);
        updateById(user);
        if (roleIds != null) {
            relationMapper.deleteUserRoles(user.getUserId());
            saveRoles(user.getUserId(), roleIds);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId) {
        if (userId != null && userId == 1L) {
            throw BusinessException.of("超级管理员不允许删除");
        }
        relationMapper.deleteUserRoles(userId);
        removeById(userId);
    }

    public void resetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(StringUtils.hasText(newPassword) ? newPassword : "123456"));
        updateById(update);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw BusinessException.of("原密码不正确");
        }
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        updateById(update);
    }

    public void updateLoginInfo(Long userId, String ip) {
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setLoginIp(ip);
        update.setLoginDate(java.time.LocalDateTime.now());
        updateById(update);
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        if (roleIds != null) {
            roleIds.forEach(roleId -> relationMapper.insertUserRole(userId, roleId));
        }
    }
}
