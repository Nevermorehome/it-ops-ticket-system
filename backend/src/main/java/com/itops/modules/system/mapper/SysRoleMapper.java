package com.itops.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itops.modules.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("""
            SELECT r.* FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = '0'
            """)
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectCustomDeptIds(@Param("roleId") Long roleId);
}
