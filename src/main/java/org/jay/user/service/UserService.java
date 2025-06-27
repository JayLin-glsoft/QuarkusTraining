package org.jay.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jay.user.model.dto.*;
import org.jay.user.model.entity.User;
import org.jay.user.model.mapper.UserMapper;
import org.jay.user.repository.UserRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserService {
    private static final Logger LOG = Logger.getLogger(UserService.class.getName());

    @Inject
    UserRepository userRepository;

    @Inject
    TokenService tokenService;

    @Inject
    UserMapper userMapper;

    @Inject
    JsonWebToken jwt; // 注入已驗證的 JWT，用於權限檢查

    /**
     * 註冊新使用者並回傳 Token
     * @param request 註冊請求
     * @return 包含 Token 的回應 DTO
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email) != null) {
            throw new IllegalArgumentException("This user already exists");
        }
        User user = userRepository.add(request.username, request.password, request.email, false);
        String token = tokenService.generateToken(user);
        return new TokenResponse(token);
    }

    /**
     * 使用者登入並回傳 Token
     * @param request 登入請求
     * @return 包含 Token 的回應 DTO
     */
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email);
        if (user != null && user.checkPassword(request.password)) {
            String token = tokenService.generateToken(user);
            return new TokenResponse(token);
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    /**
     * 獲取當前登入者的個人資料
     * @return 使用者資料 DTO
     */
    public UserProfileResponse getCurrentUser() {
        String email = jwt.getName();
        LOG.infof("Current user email: %s", email);
        if (email == null || email.isBlank()) {
            throw new ForbiddenException("You must be logged in to view your profile.");
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return userMapper.toUserProfileResponse(user);
    }

    /**
     * 更新使用者資料
     * @param id 要更新的使用者 ID
     * @param request 更新請求
     * @return 更新後的使用者資料 DTO
     */
    @Transactional
    public UserProfileResponse updateUser(Long id, UserUpdateRequest request) {
        String currentUserEmail = jwt.getName();
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new ForbiddenException("You must be logged in to update your profile.");
        }

        User userToUpdate = userRepository.findById(id);
        if (userToUpdate == null) {
            throw new NotFoundException("User not found");
        }

        // 權限檢查
        boolean isAdmin = jwt.getGroups().contains("admin");
        boolean isSelf = currentUserEmail.equals(userToUpdate.getEmail());
        if (!isAdmin && !isSelf) {
            throw new ForbiddenException("You can only update your own profile.");
        }

        // 執行更新
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            userToUpdate.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            userToUpdate.setPassword(request.getPassword());
        }

        userRepository.persist(userToUpdate);
        return userMapper.toUserProfileResponse(userToUpdate);
    }

    /**
     * 刪除使用者
     * @param id 要刪除的使用者 ID
     */
    @Transactional
    public void deleteUser(Long id) {
        String currentUserEmail = jwt.getName();
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new ForbiddenException("You must be logged in to delete a profile.");
        }

        User userToDelete = userRepository.findById(id);
        if (userToDelete == null) {
            throw new NotFoundException("User not found");
        }

        // 權限檢查
        boolean isAdmin = jwt.getGroups().contains("admin");
        boolean isSelf = currentUserEmail.equals(userToDelete.getEmail());
        if (!isAdmin && !isSelf) {
            throw new ForbiddenException("You can only delete your own profile.");
        }

        userRepository.delete(userToDelete);
    }
}
