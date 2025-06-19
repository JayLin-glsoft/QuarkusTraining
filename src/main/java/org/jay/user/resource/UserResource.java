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

import java.util.Set;

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
        if (userRepository.findByUsername(request.username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = userRepository.add(request.username, request.password, request.email);
        String token = tokenService.generateToken(user.username, Set.of("user"));
        // 回傳 201 Created 並附上 token
        return Response.status(Response.Status.CREATED).entity(new TokenResponse(token)).build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        User user = userRepository.findByUsername(request.username);
        if (user != null && user.checkPassword(request.password)) {
            // 密碼正確，簽發 JWT
            // 簡單起見，我們給所有使用者 'user' 角色
            String token = tokenService.generateToken(user.username, Set.of("user"));
            return Response.ok(new TokenResponse(token)).build();
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    @GET
    @Path("/me")
    @RolesAllowed("user") // 只有擁有 'user' 角色的 JWT 才能存取
    public Response getMe() {
        // 從已驗證的 JWT 中獲取使用者名稱
        String username = jwt.getName();
        User user = userRepository.findByUsername(username);
        // 使用 Mapper 將 Entity 轉換為安全的 DTO
        UserProfileResponse responseDto = userMapper.toUserProfileResponse(user);
        return Response.ok(responseDto).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("user")
    @Transactional
    public Response updateUser(@PathParam("id") Long id, RegisterRequest request) {
        String currentUsername = jwt.getName(); // 獲取當前 token 的使用者
        User userToUpdate = userRepository.findById(id);

        if (userToUpdate == null) {
            throw new NotFoundException("User not found");
        }

        // 簡單的權限檢查：確保使用者只能修改自己的資料
        if (!userToUpdate.username.equals(currentUsername)) {
            throw new IllegalArgumentException("You can only update your own profile");
        }

        userToUpdate.email = request.email;
        if (request.password != null && !request.password.isEmpty()) {
            userToUpdate.setPassword(request.password);
        }
        userRepository.persist(userToUpdate);
        return Response.ok("{\"message\":\"User updated successfully\"}").build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("user")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id) {
        String currentUsername = jwt.getName();
        User userToDelete = userRepository.findById(id);

        if (userToDelete == null) {
            throw new NotFoundException("User not found");
        }

        if (!userToDelete.username.equals(currentUsername)) {
            throw new IllegalArgumentException("You can only delete your own profile");
        }

        userRepository.delete(userToDelete);
        return Response.noContent().build(); // 204 No Content
    }
}
