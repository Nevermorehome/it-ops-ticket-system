package com.itops.modules.ticket.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itops.common.api.R;
import com.itops.modules.system.annotation.OperLog;
import com.itops.modules.ticket.dto.TicketActionDTO;
import com.itops.modules.ticket.dto.TicketQueryDTO;
import com.itops.modules.ticket.entity.FieldRecord;
import com.itops.modules.ticket.entity.Ticket;
import com.itops.modules.ticket.entity.TicketAttachment;
import com.itops.modules.ticket.enums.TicketAction;
import com.itops.modules.ticket.service.FieldRecordService;
import com.itops.modules.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "工单中心")
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final FieldRecordService fieldRecordService;

    @Operation(summary = "工单分页(支持数据权限与移动端维度)")
    @GetMapping("/page")
    public R<IPage<Ticket>> page(TicketQueryDTO query) {
        return R.ok(ticketService.page(query));
    }

    @Operation(summary = "工单详情(含时间线/协同人/附件/现场记录)")
    @GetMapping("/{ticketId}")
    public R<Ticket> detail(@PathVariable Long ticketId) {
        return R.ok(ticketService.detail(ticketId));
    }

    @OperLog(title = "工单创建", businessType = 1)
    @PreAuthorize("hasAuthority('ticket:add')")
    @PostMapping
    public R<Ticket> create(@RequestBody TicketCreateBody body) {
        return R.ok(ticketService.create(body.getTicket(), body.getAttachments()));
    }

    @OperLog(title = "工单编辑", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Ticket ticket) {
        ticketService.edit(ticket);
        return R.ok();
    }

    @OperLog(title = "工单指派", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:assign')")
    @PutMapping("/assign")
    public R<Void> assign(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.ASSIGN, dto);
        return R.ok();
    }

    @OperLog(title = "工单转派", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:transfer')")
    @PutMapping("/transfer")
    public R<Void> transfer(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.TRANSFER, dto);
        return R.ok();
    }

    @OperLog(title = "设置协同", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:collaborate')")
    @PutMapping("/collaborate")
    public R<Void> collaborate(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.COLLABORATE, dto);
        return R.ok();
    }

    @OperLog(title = "工单受理", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:accept')")
    @PutMapping("/accept")
    public R<Void> accept(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.ACCEPT, dto);
        return R.ok();
    }

    @OperLog(title = "提交处理", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:handle')")
    @PutMapping("/handle")
    public R<Void> handle(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.HANDLE, dto);
        return R.ok();
    }

    @OperLog(title = "工单挂起", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:suspend')")
    @PutMapping("/suspend")
    public R<Void> suspend(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.SUSPEND, dto);
        return R.ok();
    }

    @OperLog(title = "工单恢复", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:suspend')")
    @PutMapping("/resume")
    public R<Void> resume(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.RESUME, dto);
        return R.ok();
    }

    @OperLog(title = "工单解决", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:resolve')")
    @PutMapping("/resolve")
    public R<Void> resolve(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.RESOLVE, dto);
        return R.ok();
    }

    @OperLog(title = "工单关闭", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:close')")
    @PutMapping("/close")
    public R<Void> close(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.CLOSE, dto);
        return R.ok();
    }

    @OperLog(title = "工单重开", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:reopen')")
    @PutMapping("/reopen")
    public R<Void> reopen(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.REOPEN, dto);
        return R.ok();
    }

    @OperLog(title = "工单取消", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:cancel')")
    @PutMapping("/cancel")
    public R<Void> cancel(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.CANCEL, dto);
        return R.ok();
    }

    @OperLog(title = "工单合并", businessType = 2)
    @PreAuthorize("hasAuthority('ticket:merge')")
    @PutMapping("/merge")
    public R<Void> merge(@RequestBody TicketActionDTO dto) {
        ticketService.execute(TicketAction.MERGE, dto);
        return R.ok();
    }

    // ---------------- 现场记录 ----------------

    @OperLog(title = "现场记录", businessType = 1)
    @PreAuthorize("hasAuthority('ticket:field')")
    @PostMapping("/{ticketId}/field-record")
    public R<FieldRecord> addFieldRecord(@PathVariable Long ticketId,
                                         @RequestBody FieldRecordBody body) {
        return R.ok(fieldRecordService.add(ticketId, body.getRecord(), body.getImages()));
    }

    @GetMapping("/{ticketId}/field-record/list")
    public R<List<FieldRecord>> fieldRecords(@PathVariable Long ticketId) {
        return R.ok(fieldRecordService.listByTicket(ticketId));
    }

    @Data
    public static class TicketCreateBody {
        private Ticket ticket;
        private List<TicketAttachment> attachments;
    }

    @Data
    public static class FieldRecordBody {
        private FieldRecord record;
        private List<TicketAttachment> images;
    }
}
