package org.jay.user.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jay.user.model.dto.TokenResponse;
import org.jay.user.model.dto.UserProfileResponse;
import org.jay.user.model.entity.User;
import org.jay.user.model.dto.LoginRequest;
import org.jay.user.model.dto.RegisterRequest;
import org.jay.user.model.mapper.UserMapper;
import org.jay.user.repository.UserRepository;
import org.jay.user.service.TokenService;

import static io.quarkus.arc.ComponentsProvider.LOG;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    TokenService tokenService;

    @Inject
    JsonWebToken jwt; // 注入已驗證的 JWT

    @Inject // 注入 Repository
    UserRepository userRepository;

    @Inject
    UserMapper userMapper;

    // --- API Endpoints ---

    @POST
    @Path("/register")
    @PermitAll // 允許所有人存取
    @Transactional
    public Response register(@Valid RegisterRequest request) {
        LOG.infof("Jay Test: register user %s", request.username);
        if (userRepository.findByEmail(request.email) != null) {
            throw new IllegalArgumentException("This user already exists");
        }
        User user = userRepository.add(request.username, request.password, request.email, false);
        String token = tokenService.generateToken(user);
        // 回傳 201 Created 並附上 token
        return Response.status(Response.Status.CREATED).entity(new TokenResponse(token)).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.email);
        if (user != null && user.checkPassword(request.password)) {
            // 密碼正確，簽發 JWT
            // 簡單起見，我們給所有使用者 'user' 角色
            String token = tokenService.generateToken(user);
            return Response.ok(new TokenResponse(token)).build();
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    @GET
    @Path("/me")
    @RolesAllowed({"user", "admin"}) // 允許 'user' 和 'admin' 角色存取
    public Response getMe() {
        // 從已驗證的 JWT 中獲取使用者名稱
        String email = jwt.getName();
        User user = userRepository.findByEmail(email);
        // 使用 Mapper 將 Entity 轉換為安全的 DTO
        UserProfileResponse responseDto = userMapper.toUserProfileResponse(user);
        return Response.ok(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"user", "admin"}) // 允許 'user' 和 'admin' 角色存取
    @Transactional
    public Response updateUser(@PathParam("id") Long id, RegisterRequest request) {
        User userToUpdate = userRepository.findById(id);
        if (userToUpdate == null) {
            throw new NotFoundException("User not found");
        }

        // 檢查是否有 admin 權限
        boolean isAdmin = jwt.getGroups().contains("admin");
        // 檢查是否為自己
        boolean isSelf = jwt.getName().equals(userToUpdate.getEmail());

        // 如果不是管理者，也不是本人，就拋出權限不足的例外
        if (!isAdmin && !isSelf) {
            throw new ForbiddenException("You can only update your own profile.");
        }

        userToUpdate.setUsername(request.username);
        userToUpdate.setEmail(request.email);
        if (request.password != null && !request.password.isEmpty()) {
            userToUpdate.setPassword(request.password);
        }
        userRepository.persist(userToUpdate);
        return Response.ok("{\"message\":\"User updated successfully\"}").build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"user", "admin"}) // 允許 'user' 和 'admin' 角色存取
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        User userToDelete = userRepository.findById(id);
        if (userToDelete == null) {
            throw new NotFoundException("User not found");
        }

        // 檢查是否有 admin 權限
        boolean isAdmin = jwt.getGroups().contains("admin");
        // 檢查是否為自己
        boolean isSelf = jwt.getName().equals(userToDelete.getEmail());

        // 如果不是管理者，也不是本人，就拋出權限不足的例外
        if (!isAdmin && !isSelf) {
            throw new ForbiddenException("You can only update your own profile.");
        }

        userRepository.delete(userToDelete);
        return Response.noContent().build(); // 204 No Content
    }
}
