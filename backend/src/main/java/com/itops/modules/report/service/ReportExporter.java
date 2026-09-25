package com.itops.modules.report.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.itops.modules.ticket.entity.Ticket;
import com.itops.modules.ticket.enums.TicketStatus;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报表导出: Excel(EasyExcel) / Word(POI XWPF) / PDF(openhtmltopdf)
 */
@Slf4j
@Component
public class ReportExporter {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] DETAIL_HEAD =
            {"工单编号", "标题", "分类", "优先级", "状态", "报修人", "部门", "处理人", "来源", "创建时间", "解决时间", "处理时长(小时)"};

    @Value("${itops.report.font-path:/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc}")
    private String fontPath;

    // ============================ Excel ============================

    public byte[] excel(List<Ticket> tickets, Map<String, Object> overview, String range) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ExcelWriter writer = EasyExcel.write(out).build()) {

            // Sheet1 概览
            WriteSheet summarySheet = EasyExcel.writerSheet(0, "统计概览")
                    .head(List.of(List.of("指标"), List.of("数值")))
                    .build();
            List<List<Object>> summary = new ArrayList<>();
            summary.add(List.of("统计区间", range));
            summary.add(List.of("新建工单", nv(overview.get("created"))));
            summary.add(List.of("已解决", nv(overview.get("resolved"))));
            summary.add(List.of("已关闭", nv(overview.get("closed"))));
            summary.add(List.of("待受理(存量)", nv(overview.get("pending"))));
            summary.add(List.of("处理中(存量)", nv(overview.get("processing"))));
            summary.add(List.of("已挂起(存量)", nv(overview.get("suspended"))));
            summary.add(List.of("平均处理时长(小时)", nv(overview.get("avgDuration"))));
            writer.write(summary, summarySheet);

            // Sheet2 明细
            List<List<String>> head = new ArrayList<>();
            for (String h : DETAIL_HEAD) {
                head.add(List.of(h));
            }
            WriteSheet detailSheet = EasyExcel.writerSheet(1, "工单明细").head(head).build();
            List<List<Object>> rows = new ArrayList<>();
            for (Ticket t : tickets) {
                rows.add(detailRow(t));
            }
            writer.write(rows, detailSheet);
            writer.finish();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Excel 导出失败: " + e.getMessage(), e);
        }
    }

    // ============================ Word ============================

    public byte[] word(List<Ticket> tickets, Map<String, Object> overview, String range) {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("IT 运维工单统计报表");
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            setChineseFont(titleRun, "宋体");

            doc.createParagraph().createRun().setText("统计区间：" + range);

            XWPFParagraph sub = doc.createParagraph();
            sub.createRun().setText("一、统计概览");
            XWPFTable table = doc.createTable(overviewRows(overview).size(), 2);
            fillWordTable(table, List.of("指标", "数值"), overviewRows(overview));

            doc.createParagraph().createRun().setText("二、工单明细（共 " + tickets.size() + " 条）");
            List<List<String>> rows = new ArrayList<>();
            for (Ticket t : tickets) {
                List<Object> r = detailRow(t);
                rows.add(r.stream().map(String::valueOf).toList());
            }
            XWPFTable detailTable = doc.createTable(
                    Math.min(rows.size() + 1, 1000), DETAIL_HEAD.length);
            fillWordTable(detailTable, List.of(DETAIL_HEAD), rows);

            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Word 导出失败: " + e.getMessage(), e);
        }
    }

    private void fillWordTable(XWPFTable table, List<String> headers, List<List<String>> rows) {
        for (int i = 0; i < headers.size(); i++) {
            XWPFRun run = table.getRow(0).getCell(i).getParagraphs().get(0).createRun();
            run.setText(headers.get(i));
            run.setBold(true);
            setChineseFont(run, "宋体");
        }
        for (int r = 0; r < rows.size() && r + 1 < table.getNumberOfRows(); r++) {
            List<String> row = rows.get(r);
            for (int c = 0; c < headers.size(); c++) {
                String value = c < row.size() ? row.get(c) : "";
                XWPFRun run = table.getRow(r + 1).getCell(c).getParagraphs().get(0).createRun();
                run.setText(value);
                run.setFontSize(9);
                setChineseFont(run, "宋体");
            }
        }
    }

    private void setChineseFont(XWPFRun run, String font) {
        // FontCharRange.eastAsia: 同时设置 ascii/hAnsi/eastAsia 字体, 兼容 poi-ooxml-lite
        run.setFontFamily(font, XWPFRun.FontCharRange.eastAsia);
    }

    // ============================ PDF ============================

    public byte[] pdf(List<Ticket> tickets, Map<String, Object> overview, String range) {
        String html = buildHtml(tickets, overview, range);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            File font = new File(fontPath);
            if (font.exists()) {
                builder.useFont(font, "CJK");
            } else {
                log.warn("PDF 中文字体文件不存在: {}，中文可能无法正常显示(请在容器内安装 Noto Sans CJK)", fontPath);
            }
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("PDF 导出失败: " + e.getMessage(), e);
        }
    }

    private String buildHtml(List<Ticket> tickets, Map<String, Object> overview, String range) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <html><head><meta charset="utf-8"/><style>
                body{font-family:'CJK','SansSerif';font-size:12px;color:#333;}
                h1{text-align:center;font-size:20px;} h2{font-size:15px;margin-top:18px;}
                table{border-collapse:collapse;width:100%;margin-top:8px;}
                th,td{border:1px solid #999;padding:4px 6px;font-size:10px;}
                th{background:#f0f5ff;} .muted{color:#888;}
                </style></head><body>
                """);
        sb.append("<h1>IT 运维工单统计报表</h1>");
        sb.append("<p class='muted'>统计区间：").append(range).append("</p>");
        sb.append("<h2>一、统计概览</h2><table>");
        for (List<String> row : overviewRows(overview)) {
            sb.append("<tr><td>").append(row.get(0)).append("</td><td>").append(row.get(1)).append("</td></tr>");
        }
        sb.append("</table><h2>二、工单明细（共 ").append(tickets.size()).append(" 条）</h2>");
        sb.append("<table><tr>");
        for (String h : DETAIL_HEAD) {
            sb.append("<th>").append(h).append("</th>");
        }
        sb.append("</tr>");
        for (Ticket t : tickets) {
            sb.append("<tr>");
            for (Object v : detailRow(t)) {
                sb.append("<td>").append(v == null ? "" : escape(String.valueOf(v))).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ============================ 公共 ============================

    private List<Object> detailRow(Ticket t) {
        List<Object> row = new ArrayList<>();
        row.add(t.getTicketNo());
        row.add(t.getTitle());
        row.add(t.getCategoryName() == null ? "" : t.getCategoryName());
        String[] ps = {"低", "中", "高", "紧急"};
        row.add(t.getPriority() != null && t.getPriority() >= 0 && t.getPriority() < 4 ? ps[t.getPriority()] : "");
        row.add(TicketStatus.labelOf(t.getStatus()));
        row.add(t.getReporterName());
        row.add(t.getDeptName() == null ? "" : t.getDeptName());
        row.add(t.getHandlerName() == null ? "" : t.getHandlerName());
        row.add(switch (t.getSource() == null ? "" : t.getSource()) {
            case "phone" -> "电话报修";
            case "wechat" -> "微信/企微";
            case "onsite" -> "现场报障";
            case "self" -> "自助提单";
            default -> "其他";
        });
        row.add(t.getCreateTime() == null ? "" : t.getCreateTime().format(DT));
        row.add(t.getResolveTime() == null ? "" : t.getResolveTime().format(DT));
        row.add(t.getDurationSeconds() == null ? 0 : Math.round(t.getDurationSeconds() / 3600.0 * 100) / 100.0);
        return row;
    }

    private List<List<String>> overviewRows(Map<String, Object> overview) {
        return List.of(
                List.of("新建工单", String.valueOf(nv(overview.get("created")))),
                List.of("已解决", String.valueOf(nv(overview.get("resolved")))),
                List.of("已关闭", String.valueOf(nv(overview.get("closed")))),
                List.of("待受理(存量)", String.valueOf(nv(overview.get("pending")))),
                List.of("处理中(存量)", String.valueOf(nv(overview.get("processing")))),
                List.of("已挂起(存量)", String.valueOf(nv(overview.get("suspended")))),
                List.of("平均处理时长(小时)", toHours(overview.get("avgDuration")))
        );
    }

    private Object nv(Object value) {
        return value == null ? 0 : value;
    }

    private String toHours(Object seconds) {
        if (seconds == null) {
            return "0";
        }
        long s = ((Number) seconds).longValue();
        return String.valueOf(Math.round(s / 3600.0 * 100) / 100.0);
    }
}
