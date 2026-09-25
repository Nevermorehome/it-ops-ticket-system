package com.itops.modules.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itops.common.exception.BusinessException;
import com.itops.common.util.TreeUtils;
import com.itops.modules.system.entity.SysDept;
import com.itops.modules.system.entity.SysUser;
import com.itops.modules.system.mapper.SysDeptMapper;
import com.itops.modules.system.mapper.SysUserMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    private final SysUserMapper userMapper;

    public SysDeptService(@Lazy SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<SysDept> listAll(String deptName, String status) {
        return list(Wrappers.<SysDept>lambdaQuery()
                .like(deptName != null && !deptName.isBlank(), SysDept::getDeptName, deptName)
                .eq(status != null && !status.isBlank(), SysDept::getStatus, status)
                .orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getOrderNum));
    }

    public List<SysDept> tree(String deptName, String status) {
        return TreeUtils.build(listAll(deptName, status),
                SysDept::getDeptId, SysDept::getParentId,
                SysDept::getChildren, SysDept::setChildren,
                Comparator.comparing(SysDept::getOrderNum));
    }

    public void create(SysDept dept) {
        fillAncestors(dept);
        save(dept);
    }

    public void update(SysDept dept) {
        if (dept.getDeptId().equals(dept.getParentId())) {
            throw BusinessException.of("上级部门不能选择自身");
        }
        fillAncestors(dept);
        updateById(dept);
    }

    public void remove(Long deptId) {
        long children = count(Wrappers.<SysDept>lambdaQuery().eq(SysDept::getParentId, deptId));
        if (children > 0) {
            throw BusinessException.of("存在下级部门，不允许删除");
        }
        long users = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeptId, deptId));
        if (users > 0) {
            throw BusinessException.of("部门下存在用户，不允许删除");
        }
        removeById(deptId);
    }

    private void fillAncestors(SysDept dept) {
        Long parentId = dept.getParentId() == null ? 0L : dept.getParentId();
        dept.setParentId(parentId);
        if (parentId == 0L) {
            dept.setAncestors("0");
        } else {
            SysDept parent = getById(parentId);
            if (parent == null) {
                throw BusinessException.of("上级部门不存在");
            }
            dept.setAncestors(TreeUtils.joinAncestors(parent.getAncestors(), parentId));
        }
    }

    /** 本部门及全部下级部门ID */
    public List<Long> selfAndChildrenIds(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        SysDept self = getById(deptId);
        String prefix = self == null ? "" : self.getAncestors() + "," + deptId;
        List<SysDept> all = list();
        return all.stream()
                .filter(d -> d.getDeptId().equals(deptId)
                        || (d.getAncestors() != null && d.getAncestors().startsWith(prefix)))
                .map(SysDept::getDeptId)
                .toList();
    }
}
