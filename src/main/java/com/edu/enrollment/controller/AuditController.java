package com.edu.enrollment.controller;

import com.edu.enrollment.dto.AuditRequestDTO;
import com.edu.enrollment.service.AuditService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping("/single")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> audit(@Valid @RequestBody AuditRequestDTO dto,
                             @AuthenticationPrincipal Long auditorId) {
        auditService.audit(dto, auditorId);
        return ResultVO.success();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> batchAudit(@RequestBody BatchAuditDTO dto,
                                  @AuthenticationPrincipal Long auditorId) {
        auditService.batchAudit(dto.getRegistrationIds(), dto.getPassed(), dto.getComment(), auditorId);
        return ResultVO.success();
    }
}