package com.edu.enrollment.controller;

import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.RegistrationService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/submit")
    public ResultVO<Long> submit(@Valid @RequestBody RegistrationSubmitDTO dto,
                                 @CurrentUserId Long userId) {
        Long id = registrationService.submit(dto, userId);
        return ResultVO.success(id);
    }

    @GetMapping("/my")
    public ResultVO<?> my(@CurrentUserId Long userId) {
        return ResultVO.success(registrationService.getMyRegistrations(userId));
    }

    @GetMapping("/{id}")
    public ResultVO<?> detail(@PathVariable Long id) {
        return ResultVO.success(registrationService.getDetail(id));
    }

    @PutMapping("/{id}/withdraw")
    public ResultVO<?> withdraw(@PathVariable Long id,
                                @CurrentUserId Long userId) {
        registrationService.withdraw(id, userId);
        return ResultVO.success();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> pending(@CurrentUserId Long auditorId,
                               @RequestParam(defaultValue = "college_audit") String node) {
        return ResultVO.success(registrationService.getPendingAudit(auditorId, node));
    }
}