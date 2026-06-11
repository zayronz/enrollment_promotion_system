package com.edu.enrollment.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.ForgotPasswordCodeDTO;
import com.edu.enrollment.dto.ForgotPasswordDTO;
import com.edu.enrollment.dto.ForgotPasswordVerifyDTO;
import com.edu.enrollment.dto.IdentityPasswordResetDTO;
import com.edu.enrollment.dto.PasswordChangeDTO;
import com.edu.enrollment.dto.UserDTO;
import com.edu.enrollment.dto.UserRegisterDTO;
import com.edu.enrollment.dto.UserUpdateDTO;
import com.edu.enrollment.entity.CollegeEntity;
import com.edu.enrollment.entity.UserEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.CollegeMapper;
import com.edu.enrollment.mapper.UserMapper;
import com.edu.enrollment.utils.JwtUtil;
import com.edu.enrollment.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final CollegeMapper collegeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final long RESET_CODE_VALID_MINUTES = 10L;
    private static final Map<String, ResetCodeInfo> RESET_CODE_CACHE = new ConcurrentHashMap<>();

    private static class ResetCodeInfo {
        private final String code;
        private final LocalDateTime expireTime;

        private ResetCodeInfo(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }

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
     * 获取用户信息（含学院名称和头像）
     */
    public UserVO getUserInfo(Long id) {
        UserEntity user = userMapper.selectUserById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setCollegeId(user.getCollegeId());
        vo.setGrade(user.getGrade());
        vo.setGpa(user.getGpa());
        vo.setStatus(user.getStatus());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());
        // 查询学院名称
        if (user.getCollegeId() != null) {
            vo.setCollegeName(resolveCollegeName(user.getCollegeId()));
        }
        return vo;
    }

    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 使用原生SQL更新头像，避免MyBatis-Plus的exist=false问题
        userMapper.updateAvatar(userId, avatarUrl);
    }

    /**
     * 根据学院ID查询用户列表
     */
    public List<UserEntity> getByCollegeId(Long collegeId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getCollegeId, collegeId);
        return userMapper.selectList(wrapper);
    }

    public Page<UserVO> getUserList(Integer page, Integer size, String role, String keyword) {
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

        Page<UserEntity> entityPage = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<UserVO> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    private UserVO toUserVO(UserEntity user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setCollegeId(user.getCollegeId());
        vo.setGrade(user.getGrade());
        vo.setGpa(user.getGpa());
        vo.setStatus(user.getStatus());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());

        if (user.getCollegeId() != null) {
            vo.setCollegeName(resolveCollegeName(user.getCollegeId()));
        }
        return vo;
    }

    private String resolveCollegeName(Long collegeId) {
        CollegeEntity college = collegeMapper.selectById(collegeId);
        if (college != null) {
            return college.getName();
        }

        // 兼容历史数据：部分旧数据的 collegeId 可能保存的是学院管理员用户ID
        UserEntity collegeUser = userMapper.selectById(collegeId);
        if (collegeUser != null) {
            return collegeUser.getRealName();
        }
        return null;
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
        user.setCollegeId(resolveCollegeId(dto.getCollegeId(), dto.getCollegeName()));
        user.setGrade(dto.getGrade());
        user.setGpa(dto.getGpa());
        user.setStatus(1);

        userMapper.insert(user);
    }

    private Long resolveCollegeId(Long collegeId, String collegeName) {
        if (collegeId != null) {
            return collegeId;
        }
        if (StrUtil.isBlank(collegeName)) {
            throw new BusinessException("学院不能为空");
        }

        String name = collegeName.trim();
        CollegeEntity existCollege = collegeMapper.selectOne(
                new LambdaQueryWrapper<CollegeEntity>()
                        .eq(CollegeEntity::getName, name)
                        .last("LIMIT 1")
        );
        if (existCollege != null) {
            return existCollege.getId();
        }

        CollegeEntity college = new CollegeEntity();
        college.setName(name);
        college.setCode("C" + System.currentTimeMillis());
        collegeMapper.insert(college);
        return college.getId();
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
        if (dto.getGpa() != null) {
            user.setGpa(dto.getGpa());
        }
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        userMapper.updateById(user);

        // 显式更新学生学业信息，避免通用更新策略或字段类型转换导致绩点未持久化
        if ("STUDENT".equals(user.getRole()) && (dto.getGrade() != null || dto.getGpa() != null)) {
            userMapper.updateStudentAcademic(user.getId(), user.getGrade(), user.getGpa());
        }
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
     * 忘记密码 - 发送邮箱验证码
     */
    public void sendForgotPasswordCode(ForgotPasswordCodeDTO dto) {
        UserEntity user = userMapper.findByUsername(dto.getUsername());
        validateResetUser(user, dto.getEmail(), true);

        String code = String.format("%06d", new Random().nextInt(1000000));
        String cacheKey = buildResetCodeKey(dto.getUsername(), dto.getEmail());
        RESET_CODE_CACHE.put(cacheKey, new ResetCodeInfo(code, LocalDateTime.now().plusMinutes(RESET_CODE_VALID_MINUTES)));

        if (StrUtil.isBlank(mailFrom) || StrUtil.isBlank(mailPassword)) {
            log.warn("系统未完整配置发件邮箱，已启用本地开发验证码。账号：{}，邮箱：{}，验证码：{}，有效期：{}分钟",
                    dto.getUsername(), dto.getEmail(), code, RESET_CODE_VALID_MINUTES);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(dto.getEmail());
        message.setSubject("招生宣传报名系统密码重置验证码");
        message.setText("您的密码重置验证码为：" + code + "\n\n验证码有效期为 "
                + RESET_CODE_VALID_MINUTES + " 分钟。如非本人操作，请忽略本邮件。");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            RESET_CODE_CACHE.remove(cacheKey);
            throw new BusinessException("验证码发送失败，请检查邮箱配置或稍后重试", e);
        }
    }

    /**
     * 忘记密码 - 仅校验邮箱验证码，校验成功后前端再进入设置新密码步骤
     */
    public void verifyForgotPasswordCode(ForgotPasswordVerifyDTO dto) {
        UserEntity user = userMapper.findByUsername(dto.getUsername());
        validateResetUser(user, dto.getEmail(), false);
        validateResetCode(dto.getUsername(), dto.getEmail(), dto.getCode(), false);
    }

    /**
     * 忘记密码 - 校验邮箱验证码后重置密码
     */
    @Transactional
    public void forgotPassword(ForgotPasswordDTO dto) {
        UserEntity user = userMapper.findByUsername(dto.getUsername());
        validateResetUser(user, dto.getEmail(), false);
        validateResetCode(dto.getUsername(), dto.getEmail(), dto.getCode(), true);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 本地演示用：模拟统一身份认证平台根据注册邮箱或手机号重置密码。
     * 正式部署时应跳转学校统一身份认证平台，由认证平台完成密码找回。
     */
    @Transactional
    public void resetPasswordByIdentityMock(IdentityPasswordResetDTO dto) {
        String account = dto.getAccount() == null ? "" : dto.getAccount().trim();
        String newPassword = dto.getNewPassword() == null ? "" : dto.getNewPassword();

        if (newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        UserEntity user = userMapper.findByEmailOrPhone(account);
        if (user == null) {
            throw new BusinessException("未找到绑定该邮箱或手机号的账号");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("该账号已被禁用，无法重置密码");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private void validateResetUser(UserEntity user, String email, boolean forSend) {
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        if (user.getEmail() == null || !user.getEmail().equals(email)) {
            throw new BusinessException("邮箱与账号绑定信息不匹配");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(forSend ? "该账号已被禁用，无法发送验证码" : "该账号已被禁用，无法重置密码");
        }
    }

    private void validateResetCode(String username, String email, String code, boolean removeAfterSuccess) {
        String cacheKey = buildResetCodeKey(username, email);
        ResetCodeInfo codeInfo = RESET_CODE_CACHE.get(cacheKey);
        if (codeInfo == null) {
            throw new BusinessException("请先获取邮箱验证码");
        }
        if (LocalDateTime.now().isAfter(codeInfo.expireTime)) {
            RESET_CODE_CACHE.remove(cacheKey);
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!codeInfo.code.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        if (removeAfterSuccess) {
            RESET_CODE_CACHE.remove(cacheKey);
        }
    }

    private String buildResetCodeKey(String username, String email) {
        return username.trim().toLowerCase() + ":" + email.trim().toLowerCase();
    }
}
