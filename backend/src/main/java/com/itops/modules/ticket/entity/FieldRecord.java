package com.itops.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_field_record")
public class FieldRecord {

    @TableId
    private Long recordId;
    private Long ticketId;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String address;
    private String content;
    /** 服务器接收时间 */
    private LocalDateTime recordTime;
    private Long createById;
    private String createBy;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<TicketAttachment> images;
}
