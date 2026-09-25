package com.itops.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId
    private Long operId;
    private String title;
    /** 0其它 1新增 2修改 3删除 4导出 5导入 */
    private Integer businessType;
    private String method;
    private String requestMethod;
    private String operName;
    private String deptName;
    private String operUrl;
    private String operIp;
    private String operParam;
    private String jsonResult;
    /** 0正常 1异常 */
    private String status;
    private String errorMsg;
    private Long costTime;
    private LocalDateTime operTime;
}
