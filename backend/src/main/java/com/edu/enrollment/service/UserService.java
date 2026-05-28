package com.edu.enrollment.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.ForgotPasswordDTO;
import com.edu.enrollment.dto.PasswordChangeDTO;
import com.edu.enrollment.dto.UserDTO;
import com.edu.enrollment.dto.UserRegisterDTO;
import com.edu.enrollment.dto.UserUpdateDTO;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.UserMapper;
import com.edu.enrollment.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 根据学院ID查询用户列表
     */
    public List<UserEntity> getByCollegeId(Long collegeId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getCollegeId, collegeId);
        return userMapper.selectList(wrapper);
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

    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StrUtil.isNotBlank(dto.getRealName())) {
            user.setRealName(dto.getRealName());
        }
        if (StrUtil.isNotBlank(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (StrUtil.isNotBlank(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StrUtil.isNotBlank(dto.getRole())) {
            user.setRole(dto.getRole());
        }
        if (dto.getCollegeId() != null) {
            user.setCollegeId(dto.getCollegeId());
        }
        if (dto.getGrade() != null) {
            user.setGrade(dto.getGrade());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        userMapper.updateById(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeDTO dto) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 忘记密码 - 通过用户名+邮箱验证身份后重置密码
     */
    @Transactional
    public void forgotPassword(ForgotPasswordDTO dto) {
        // 通过用户名查找用户
        UserEntity user = userMapper.findByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 验证邮箱是否匹配
        if (user.getEmail() == null || !user.getEmail().equals(dto.getEmail())) {
            throw new BusinessException("邮箱验证失败，请检查后重试");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用，无法重置密码");
        }

        // 重置密码
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}