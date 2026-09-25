package com.itops.modules.report.controller;

import com.itops.common.api.R;
import com.itops.modules.report.service.ReportExporter;
import com.itops.modules.report.service.ReportService;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.ticket.entity.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Tag(name = "统计报表")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExporter exporter;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "区间概览")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview(@RequestParam(required = false) String start,
                                           @RequestParam(required = false) String end) {
        return R.ok(reportService.overview(start, end));
    }

    @Operation(summary = "按日趋势(新建/解决)")
    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend(@RequestParam(required = false) String start,
                                              @RequestParam(required = false) String end) {
        return R.ok(reportService.trend(start, end));
    }

    @Operation(summary = "分布统计(category/priority/source/status)")
    @GetMapping("/distribution")
    public R<List<Map<String, Object>>> distribution(@RequestParam(defaultValue = "category") String groupBy,
                                                     @RequestParam(required = false) String start,
                                                     @RequestParam(required = false) String end) {
        return R.ok(reportService.distribution(groupBy, start, end));
    }

    @Operation(summary = "处理人排行")
    @GetMapping("/rank")
    public R<List<Map<String, Object>>> rank(@RequestParam(required = false) String start,
                                             @RequestParam(required = false) String end) {
        return R.ok(reportService.handlerRank(start, end));
    }

    @Operation(summary = "部门工单量")
    @GetMapping("/dept")
    public R<List<Map<String, Object>>> dept(@RequestParam(required = false) String start,
                                             @RequestParam(required = false) String end) {
        return R.ok(reportService.deptStat(start, end));
    }

    @OperLog(title = "报表导出", businessType = 4)
    @PreAuthorize("hasAuthority('report:export')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "excel") String type,
                                         @RequestParam(required = false) String start,
                                         @RequestParam(required = false) String end) {
        ReportService.Range range = reportService.parseRange(start, end);
        String rangeText = range.startDate() + " 至 " + range.endDate();
        Map<String, Object> overview = reportService.overview(start, end);
        List<Ticket> tickets = reportService.detailList(start, end);

        byte[] bytes;
        String suffix;
        MediaType mediaType;
        switch (type) {
            case "word" -> {
                bytes = exporter.word(tickets, overview, rangeText);
                suffix = "docx";
                mediaType = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }
            case "pdf" -> {
                bytes = exporter.pdf(tickets, overview, rangeText);
                suffix = "pdf";
                mediaType = MediaType.APPLICATION_PDF;
            }
            default -> {
                bytes = exporter.excel(tickets, overview, rangeText);
                suffix = "xlsx";
                mediaType = MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }
        }

        String fileName = "IT运维工单统计_" + LocalDate.now().format(DAY) + "." + suffix;
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
                .contentType(mediaType)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
