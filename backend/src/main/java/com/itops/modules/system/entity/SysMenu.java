package com.itops.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @TableId
    private Long menuId;
    private Long parentId;
    private String menuName;
    /** M目录 C菜单 F按钮 */
    private String menuType;
    private String path;
    private String component;
    private String perms;
    private String icon;
    private Integer orderNum;
    /** 0显示 1隐藏 */
    private String visible;
    private String status;

    @TableField(exist = false)
    private List<SysMenu> children;
}
