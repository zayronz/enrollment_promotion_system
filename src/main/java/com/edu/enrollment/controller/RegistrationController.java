package com.edu.enrollment.controller;

import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.service.RegistrationService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * 提交报名
     */
    @PostMapping("/submit")
    public ResultVO submit(@RequestBody RegistrationSubmitDTO dto,
                           @AuthenticationPrincipal Long userId) {
        return ResultVO.success(registrationService.submit(dto, userId));
    }

    /**
     * 我的报名列表
     */
    @GetMapping("/my")
    public ResultVO myRegistrations(@AuthenticationPrincipal Long userId) {
        return ResultVO.success(registrationService.getMyRegistrations(userId));
    }

    /**
     * 报名详情
     */
    @GetMapping("/{id}")
    public ResultVO detail(@PathVariable Long id) {
        return ResultVO.success(registrationService.getDetail(id));
    }

    /**
     * 撤回报名
     */
    @PutMapping("/{id}/withdraw")
    public ResultVO withdraw(@PathVariable Long id,
                             @AuthenticationPrincipal Long userId) {
        registrationService.withdraw(id, userId);
        return ResultVO.success();
    }
}