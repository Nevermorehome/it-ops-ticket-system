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
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @TableId
    private Long roleId;
    private String roleName;
    private String roleKey;
    /** 1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
    private String dataScope;
    private Integer orderNum;
    private String status;
    private String remark;

    /** 关联菜单ID(保存入参用) */
    @TableField(exist = false)
    private List<Long> menuIds;

    /** 自定义数据范围部门ID */
    @TableField(exist = false)
    private List<Long> deptIds;
}
