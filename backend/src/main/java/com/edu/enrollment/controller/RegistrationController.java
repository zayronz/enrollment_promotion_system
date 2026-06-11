package com.edu.enrollment.controller;

import com.edu.enrollment.dto.RegistrationSubmitDTO;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.RegistrationService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/status")
    public ResultVO<?> status(@RequestParam Long activityId,
                              @CurrentUserId Long userId) {
        return ResultVO.success(registrationService.getRegistrationStatus(activityId, userId));
    }

    @GetMapping("/school-suggestions")
    public ResultVO<List<String>> schoolSuggestions(@RequestParam(required = false) Long activityId,
                                                    @RequestParam String keyword) {
        return ResultVO.success(registrationService.suggestSchools(activityId, keyword));
    }

    @GetMapping("/my")
    public ResultVO<?> my(@CurrentUserId Long userId,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        return ResultVO.success(registrationService.getMyRegistrations(userId, page, size));
    }

    @GetMapping("/my-teams")
    public ResultVO<?> myTeams(@CurrentUserId Long userId) {
        return ResultVO.success(registrationService.getMyTeams(userId));
    }

    @GetMapping("/available-teams")
    public ResultVO<?> availableTeams(@CurrentUserId Long userId) {
        return ResultVO.success(registrationService.getAvailableTeams(userId));
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

    @PutMapping("/{id}/exit-team")
    public ResultVO<?> exitTeam(@PathVariable Long id,
                                @CurrentUserId Long userId) {
        registrationService.exitTeam(id, userId);
        return ResultVO.success();
    }

    @PutMapping("/{id}/join-team")
    public ResultVO<?> joinTeam(@PathVariable Long id,
                                @RequestBody Map<String, String> body,
                                @CurrentUserId Long userId) {
        registrationService.joinTeam(id, body.get("groupName"), userId);
        return ResultVO.success();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('COLLEGE', 'SCHOOL')")
    public ResultVO<?> pending(@CurrentUserId Long auditorId,
                               @RequestParam(defaultValue = "college_audit") String node,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long activityId,
                               @RequestParam(required = false) Long collegeId) {
        return ResultVO.success(registrationService.getPendingAudit(auditorId, node, page, size, keyword, activityId, collegeId));
    }
}
