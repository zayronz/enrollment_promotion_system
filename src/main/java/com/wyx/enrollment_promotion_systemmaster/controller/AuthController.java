package com.wyx.enrollment_promotion_systemmaster.controller;

import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.dto.LoginDTO;
import com.wyx.enrollment_promotion_systemmaster.dto.LoginVO;
import com.wyx.enrollment_promotion_systemmaster.dto.UserDTO;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return ApiResponse.success(loginVO);
    }

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@RequestBody User user) {
        User newUser = userService.register(user);
        return ApiResponse.success(userService.convertToUserDTO(newUser));
    }

    @GetMapping("/userinfo")
    public ApiResponse<UserDTO> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByUsername(userDetails.getUsername());
        return ApiResponse.success(userService.convertToUserDTO(user));
    }

    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        User user = userService.getByUsername(userDetails.getUsername());
        userService.updatePassword(user.getId(), oldPassword, newPassword);
        return ApiResponse.success();
    }
}
