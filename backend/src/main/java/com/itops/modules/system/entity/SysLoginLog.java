package com.itops.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
public class SysLoginLog {

    @TableId
    private Long infoId;
    private String username;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    /** 0成功 1失败 */
    private String status;
    private String msg;
    private LocalDateTime loginTime;
}
