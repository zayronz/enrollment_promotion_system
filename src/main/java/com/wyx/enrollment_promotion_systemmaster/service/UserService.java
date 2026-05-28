package com.wyx.enrollment_promotion_systemmaster.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.dto.LoginDTO;
import com.wyx.enrollment_promotion_systemmaster.dto.LoginVO;
import com.wyx.enrollment_promotion_systemmaster.dto.UserDTO;
import com.wyx.enrollment_promotion_systemmaster.entity.User;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.UserMapper;
import com.wyx.enrollment_promotion_systemmaster.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginVO login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            User user = getByUsername(loginDTO.getUsername());
            if (user == null) {
                throw new BusinessException("User not found");
            }

            String token = jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());

            LoginVO loginVO = new LoginVO();
            loginVO.setToken(token);
            loginVO.setUser(convertToUserDTO(user));

            return loginVO;
        } catch (Exception e) {
            throw new BusinessException("Invalid username or password");
        }
    }

    public User register(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("Username already exists");
        }

        if (user.getRole() == null) {
            user.setRole(1);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
        return user;
    }

    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    public UserDTO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }
        return convertToUserDTO(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
