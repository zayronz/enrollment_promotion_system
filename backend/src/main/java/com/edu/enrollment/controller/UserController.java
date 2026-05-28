package com.edu.enrollment.controller;

import com.edu.enrollment.dto.*;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.UserService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResultVO<String> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        return ResultVO.success(token);
    }

    @PostMapping("/register")
    public ResultVO<?> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return ResultVO.success();
    }

    @GetMapping("/info")
    public ResultVO<?> info(@CurrentUserId Long userId) {
        return ResultVO.success(userService.getById(userId));
    }

    @GetMapping("/list")
    public ResultVO<?> list(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String keyword) {
        return ResultVO.success(userService.getUserList(page, size, role, keyword));
    }

    @PostMapping("/create")
    public ResultVO<?> create(@Valid @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return ResultVO.success();
    }

    @PutMapping("/{id}")
    public ResultVO<?> update(@PathVariable Long id,
                              @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return ResultVO.success();
    }

    @DeleteMapping("/{id}")
    public ResultVO<?> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResultVO.success();
    }

    @PutMapping("/password")
    public ResultVO<?> changePassword(@Valid @RequestBody PasswordChangeDTO dto,
                                      @CurrentUserId Long userId) {
        userService.changePassword(userId, dto);
        return ResultVO.success();
    }

    @PostMapping("/forgot-password")
    public ResultVO<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        userService.forgotPassword(dto);
        return ResultVO.success("密码重置成功，请使用新密码登录");
    }
}