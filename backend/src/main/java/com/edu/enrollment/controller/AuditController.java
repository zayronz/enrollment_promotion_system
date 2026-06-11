package com.edu.enrollment.controller;

import com.edu.enrollment.dto.AuditRequestDTO;
import com.edu.enrollment.dto.BatchAuditDTO;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.AuditService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/single")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> audit(@Valid @RequestBody AuditRequestDTO dto,
                             @CurrentUserId Long auditorId) {
        auditService.audit(dto, auditorId);
        return ResultVO.success();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> batchAudit(@RequestBody BatchAuditDTO dto,
                                  @CurrentUserId Long auditorId) {
        auditService.batchAudit(dto.getRegistrationIds(), dto.getPassed(), dto.getComment(), auditorId);
        return ResultVO.success();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> history(@CurrentUserId Long auditorId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long activityId,
                               @RequestParam(required = false) String result) {
        return ResultVO.success(auditService.getAuditHistory(auditorId, page, size, keyword, activityId, result));
    }
}