package org.agro.service.impl;

import org.agro.dto.*;
import org.agro.entity.NotificationSetting;
import org.agro.entity.User;
import org.agro.repository.NotificationSettingRepository;
import org.agro.repository.UserRepository;
import org.agro.security.JwtUtils;
import org.agro.security.UserDetailsImpl;
import org.agro.service.EmailService;
import org.agro.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // 用于存储密码重置验证码
    private final Map<String, String> passwordResetCodes = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public User registerUser(String username, String password, String email, String nickname, String phone) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setNickname(nickname != null ? nickname : username);
        user.setPhone(phone);
        user.setRole("USER"); // 默认角色为普通用户
        user.setStatus(true); // 默认状态为启用

        // 保存用户
        user = userRepository.save(user);

        // 创建用户通知设置
        NotificationSetting notificationSetting = new NotificationSetting();
        notificationSetting.setUser(user);
        notificationSetting.setEmailNotify(true);
        notificationSetting.setSystemNotify(true);
        notificationSettingRepository.save(notificationSetting);

        return user;
    }

    @Override
    public AuthResponse loginUser(String username, String password) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 构建并返回认证响应
        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getNickname(),
                userDetails.getAvatar(),
                userDetails.getAuthorities().iterator().next().getAuthority()
        );
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest updateRequest) {
        User user = findById(userId);

        // 如果要更新邮箱，检查邮箱是否已被其他用户使用
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
            user.setEmail(updateRequest.getEmail());
        }

        // 更新其他字段
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }

        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }

        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = findById(userId);
        user.setAvatar(avatarUrl);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, PasswordChangeRequest passwordChangeRequest) {
        User user = findById(userId);

        // 验证旧密码，直接比较明文密码
        if (!passwordChangeRequest.getOldPassword().equals(user.getPassword())) {
            return false;
        }

        // 更新密码，直接存储明文密码
        user.setPassword(passwordChangeRequest.getNewPassword());
        userRepository.save(user);

        return true;
    }

    @Override
    public void sendPasswordResetCode(String email) {
        // 验证邮箱是否存在
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            // 为了安全考虑，即使邮箱不存在也不告诉用户，只是不发送验证码
            logger.info("尝试重置密码的邮箱不存在: {}", email);
            return;
        }

        // 生成6位数字验证码
        String verificationCode = generateVerificationCode();

        // 存储验证码 (实际应用中应该使用Redis等存储，并设置过期时间)
        passwordResetCodes.put(email, verificationCode);

        // 发送验证码到邮箱
        emailService.sendPasswordResetVerificationCode(email, verificationCode);
    }

    @Override
    public boolean sendForgotPasswordCode(ForgotPasswordRequest forgotPasswordRequest) {
        String username = forgotPasswordRequest.getUsername();
        String email = forgotPasswordRequest.getEmail();

        // 验证用户名是否存在
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            logger.info("尝试找回密码的用户名不存在: {}", username);
            return false;
        }

        User user = optionalUser.get();

        // 验证用户名和邮箱是否匹配
        if (!user.getEmail().equals(email)) {
            logger.info("用户名和邮箱不匹配: username={}, email={}", username, email);
            return false;
        }

        // 生成6位数字验证码
        String verificationCode = generateVerificationCode();

        // 存储验证码 (实际应用中应该使用Redis等存储，并设置过期时间)
        passwordResetCodes.put(email, verificationCode);

        // 发送验证码到邮箱
        emailService.sendPasswordResetVerificationCode(email, verificationCode);

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(PasswordResetRequest passwordResetRequest) {
        String email = passwordResetRequest.getEmail();
        String verificationCode = passwordResetRequest.getVerificationCode();

        // 验证验证码
        String storedCode = passwordResetCodes.get(email);
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            return false;
        }

        // 找到用户并更新密码
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 直接存储明文密码
        user.setPassword(passwordResetRequest.getNewPassword());
        userRepository.save(user);

        // 删除已使用的验证码
        passwordResetCodes.remove(email);

        return true;
    }

    @Override
    @Transactional
    public boolean toggleUserStatus(Long userId) {
        User user = findById(userId);

        // 切换用户状态
        user.setStatus(!user.getStatus());
        userRepository.save(user);

        return true;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }

    @Override
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 生成100000-999999之间的随机数
        return String.valueOf(code);
    }
}
