package com.itops.modules.ticket.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.itops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ticket")
public class Ticket extends BaseEntity {

    @TableId
    private Long ticketId;
    private String ticketNo;
    private String title;
    private Long categoryId;
    private String description;
    private Long reporterId;
    private String reporterName;
    private Long deptId;
    private String phone;
    private String source;
    /** 0低 1中 2高 3紧急 */
    private Integer priority;
    /** TicketStatus 枚举名 */
    private String status;
    private Long locationId;
    private String locationText;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Long assetId;
    private Long handlerId;
    private String handlerName;
    private String suspendReason;
    private String handleSummary;
    private String solution;
    private Integer satisfaction;
    private String satisfactionComment;
    private Long mergedTo;
    private LocalDateTime assignTime;
    private LocalDateTime acceptTime;
    private LocalDateTime suspendTime;
    private LocalDateTime resolveTime;
    private LocalDateTime closeTime;
    private LocalDateTime finishTime;
    private Long durationSeconds;

    // ------------ 非持久化聚合字段 ------------

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String deptName;
    @TableField(exist = false)
    private String assetName;
    @TableField(exist = false)
    private List<TicketCollaborator> collaborators;
    @TableField(exist = false)
    private List<TicketAttachment> attachments;
    @TableField(exist = false)
    private List<TicketTimeline> timelines;
    @TableField(exist = false)
    private List<FieldRecord> fieldRecords;
}
