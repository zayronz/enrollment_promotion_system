package com.edu.enrollment.controller;

import com.edu.enrollment.dto.LoginDTO;
import com.edu.enrollment.dto.UserDTO;
import com.edu.enrollment.service.UserService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/info")
    public ResultVO<?> info(@AuthenticationPrincipal Long userId) {
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
}