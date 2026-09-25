package com.itops.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itops.modules.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 某用户可见的全部菜单(含按钮, 用于权限标识) */
    @Select("""
            SELECT DISTINCT m.* FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = '0'
            ORDER BY m.parent_id, m.order_num
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_menu WHERE deleted = 0 AND status = '0' ORDER BY parent_id, order_num")
    List<SysMenu> selectAllMenus();

    /** 某用户全部权限标识 */
    @Select("""
            SELECT DISTINCT m.perms FROM sys_menu m
            INNER JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
            INNER JOIN sys_user_role ur ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = '0'
              AND m.perms IS NOT NULL AND m.perms <> ''
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    @Select("SELECT perms FROM sys_menu WHERE deleted = 0 AND status = '0' AND perms IS NOT NULL AND perms <> ''")
    List<String> selectAllPerms();

    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE menu_id = #{menuId}")
    long countRoleRef(@Param("menuId") Long menuId);
}
