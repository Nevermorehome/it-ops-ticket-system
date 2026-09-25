package com.itops.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    @TableId
    private Long userId;
    private Long deptId;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String realName;
    private String nickName;
    private String phone;
    private String email;
    private String sex;
    private String avatar;
    /** 0正常 1停用 */
    private String status;
    private String loginIp;
    private LocalDateTime loginDate;
    private String remark;
}
