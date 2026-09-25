package com.itops.modules.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itops.modules.ticket.dto.TicketQueryDTO;
import com.itops.modules.ticket.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

    /**
     * 工单分页(关联分类/部门/资产名称, 带数据权限片段)
     */
    IPage<Ticket> selectTicketPage(Page<Ticket> page,
                                   @Param("q") TicketQueryDTO q,
                                   @Param("dataScope") String dataScope,
                                   @Param("currentUserId") Long currentUserId);

    /** 详情(带分类/部门/资产名称) */
    Ticket selectDetailById(@Param("ticketId") Long ticketId);
}
