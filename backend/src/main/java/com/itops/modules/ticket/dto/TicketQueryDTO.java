package com.itops.modules.ticket.dto;

import com.itops.common.api.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketQueryDTO extends PageQuery {

    /** 编号/标题模糊 */
    private String keyword;
    private Long categoryId;
    private String status;
    private Integer priority;
    private String source;
    private Long deptId;
    private Long handlerId;
    private Long reporterId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;

    /**
     * 移动端维度: created=我报修的 assigned=待我处理(处理人是我且未关闭)
     * collaborator=我协同的 all=全部(受数据权限约束)
     */
    private String dimension;
}
