package com.itops.modules.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itops.common.exception.BusinessException;
import com.itops.modules.report.mapper.ReportMapper;
import com.itops.modules.ticket.dto.TicketQueryDTO;
import com.itops.modules.ticket.entity.Ticket;
import com.itops.modules.ticket.mapper.TicketMapper;
import com.itops.security.datascope.DataScope;
import com.itops.security.datascope.DataScopeContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SELF_SCOPE_SQL =
            " (t.reporter_id = {userId} OR t.handler_id = {userId} OR EXISTS "
                    + "(SELECT 1 FROM biz_ticket_collaborator c WHERE c.ticket_id = t.ticket_id AND c.user_id = {userId})) ";

    private final ReportMapper reportMapper;
    private final TicketMapper ticketMapper;

    public record Range(LocalDateTime start, LocalDateTime end, LocalDate startDate, LocalDate endDate) {
    }

    public Range parseRange(String start, String end) {
        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
            LocalDate e = LocalDate.now();
            LocalDate s = e.minusDays(6);
            return new Range(s.atStartOfDay(), e.plusDays(1).atStartOfDay(), s, e);
        }
        try {
            LocalDate s = LocalDate.parse(start, DAY);
            LocalDate e = LocalDate.parse(end, DAY);
            if (e.isBefore(s)) {
                throw BusinessException.of("结束日期不能早于开始日期");
            }
            return new Range(s.atStartOfDay(), e.plusDays(1).atStartOfDay(), s, e);
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException("日期格式应为 yyyy-MM-dd");
        }
    }

    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public Map<String, Object> overview(String start, String end) {
        Range r = parseRange(start, end);
        Map<String, Object> data = reportMapper.overview(r.start(), r.end(), DataScopeContext.get());
        if (data == null) {
            data = new LinkedHashMap<>();
        }
        Double avg = reportMapper.avgDuration(r.start(), r.end(), DataScopeContext.get());
        data.put("avgDuration", avg == null ? 0 : avg.longValue());
        data.put("rangeStart", r.startDate().toString());
        data.put("rangeEnd", r.endDate().toString());
        return data;
    }

    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public List<Map<String, Object>> trend(String start, String end) {
        Range r = parseRange(start, end);
        String scope = DataScopeContext.get();
        Map<String, Object> createdMap = toMap(reportMapper.trendCreated(r.start(), r.end(), scope));
        Map<String, Object> resolvedMap = toMap(reportMapper.trendResolved(r.start(), r.end(), scope));

        List<Map<String, Object>> series = new ArrayList<>();
        LocalDate cursor = r.startDate();
        while (!cursor.isAfter(r.endDate())) {
            String day = cursor.format(DAY);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", day);
            item.put("created", createdMap.getOrDefault(day, 0L));
            item.put("resolved", resolvedMap.getOrDefault(day, 0L));
            series.add(item);
            cursor = cursor.plusDays(1);
        }
        return series;
    }

    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public List<Map<String, Object>> distribution(String groupBy, String start, String end) {
        Range r = parseRange(start, end);
        if (!List.of("category", "priority", "source", "status").contains(groupBy)) {
            throw BusinessException.of("不支持的统计维度: " + groupBy);
        }
        return reportMapper.distribution(groupBy, r.start(), r.end(), DataScopeContext.get());
    }

    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public List<Map<String, Object>> handlerRank(String start, String end) {
        Range r = parseRange(start, end);
        return reportMapper.handlerRank(r.start(), r.end(), DataScopeContext.get());
    }

    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public List<Map<String, Object>> deptStat(String start, String end) {
        Range r = parseRange(start, end);
        return reportMapper.deptStat(r.start(), r.end(), DataScopeContext.get());
    }

    /** 导出用明细(最多 500 行) */
    @DataScope(deptAlias = "t", selfSql = SELF_SCOPE_SQL)
    public List<Ticket> detailList(String start, String end) {
        Range r = parseRange(start, end);
        TicketQueryDTO q = new TicketQueryDTO();
        q.setPageNum(1);
        q.setPageSize(500);
        q.setBeginTime(r.start());
        q.setEndTime(r.end());
        Page<Ticket> page = new Page<>(1, 500);
        return ticketMapper.selectTicketPage(page, q, DataScopeContext.get(),
                com.itops.security.SecurityUtils.getUserId()).getRecords();
    }

    private Map<String, Object> toMap(List<Map<String, Object>> rows) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("name")), row.get("value"));
        }
        return map;
    }
}
