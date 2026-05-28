package com.edu.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.UserDTO;
import com.edu.enrollment.dto.UserRegisterDTO;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.UserMapper;
import com.edu.enrollment.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        UserEntity user = userMapper.findByUsername(username);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    public UserEntity getById(Long id) {
        return userMapper.selectById(id);
    }

    public Page<UserEntity> getUserList(Integer page, Integer size, String role, String keyword) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq(UserEntity::getRole, role);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(UserEntity::getRealName, keyword)
                    .or()
                    .like(UserEntity::getUsername, keyword);
        }
        wrapper.orderByDesc(UserEntity::getCreateTime);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public void createUser(UserDTO dto) {
        UserEntity existUser = userMapper.findByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setCollegeId(dto.getCollegeId());
        user.setGrade(dto.getGrade());
        user.setGpa(dto.getGpa());
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Transactional
    public void register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        UserEntity existUser = userMapper.findByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setCollegeId(dto.getCollegeId());
        user.setGrade(dto.getGrade());
        user.setGpa(dto.getGpa());
        user.setStatus(1);

        userMapper.insert(user);
    }
}