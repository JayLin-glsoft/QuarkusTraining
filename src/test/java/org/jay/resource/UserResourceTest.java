package org.jay.resource;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import org.jay.user.model.dto.*;
import org.jay.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@QuarkusTest
public class UserResourceTest {

    // 根據您的最新架構，Resource 層只依賴 Service 層，因此我們只 Mock UserService
    @InjectMock
    UserService userService;

    /**
     * 產生用於測試的 JWT Token
     * @param email 使用者 email
     * @param roles 使用者角色
     * @return JWT Token 字串
     */
    private String generateTestToken(String email, String... roles) {
        return Jwt.issuer("https://myapp.com/issuer")
                .upn(email)
                .groups(Set.of(roles))
                .expiresIn(3600)
                .sign();
    }

    // --- 測試註冊 API ---
    @Test
    @DisplayName("POST /register - 應呼叫 service.register 並回傳 token")
    void testRegister() {
        // Arrange: 模擬 UserService 的 register 方法
        TokenResponse mockToken = new TokenResponse("mocked_jwt_token");
        Mockito.when(userService.register(any(RegisterRequest.class))).thenReturn(mockToken);

        RegisterRequest request = new RegisterRequest();
        request.username = "testuser";
        request.password = "password123";
        request.email = "newuser@example.com";

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/users/register")
                .then()
                .statusCode(201)
                .body("token", is("mocked_jwt_token"));
    }

    // --- 測試登入 API ---
    @Test
    @DisplayName("POST /login - 應呼叫 service.login 並回傳 token")
    void testLogin() {
        // Arrange: 模擬 UserService 的 login 方法
        TokenResponse mockToken = new TokenResponse("login_success_token");
        Mockito.when(userService.login(any(LoginRequest.class))).thenReturn(mockToken);

        LoginRequest request = new LoginRequest();
        request.email = "test@example.com";
        request.password = "password123";

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/users/login")
                .then()
                .statusCode(200)
                .body("token", is("login_success_token"));
    }

    // --- 測試取得個人資料 API ---
    @Test
    @DisplayName("GET /me - 應呼叫 service.getCurrentUser 並回傳使用者資料")
    void testGetMe() {
        // Arrange
        String userToken = generateTestToken("test@example.com", "user");
        UserProfileResponse mockResponse = new UserProfileResponse();
        mockResponse.setId(1L);
        mockResponse.setUsername("testuser");
        mockResponse.setEmail("test@example.com");
        mockResponse.setCreatedTime(Instant.now());

        Mockito.when(userService.getCurrentUser()).thenReturn(mockResponse);

        // Act & Assert
        given()
                .auth().oauth2(userToken)
                .when().get("/api/users/me")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("username", is("testuser"))
                .body("email", is("test@example.com"))
                .body("createdTime", notNullValue());
    }

    // --- 測試更新 API ---
    @Test
    @DisplayName("PUT /{id} - 應呼叫 service.updateUser 並回傳更新後的使用者資料")
    void testUpdateUser() {
        // Arrange
        String userToken = generateTestToken("test@example.com", "user");
        UserProfileResponse mockResponse = new UserProfileResponse();
        mockResponse.setId(1L);
        mockResponse.setUsername("updatedUser");
        mockResponse.setEmail("test@example.com");

        // 模擬 updateUser 服務
        Mockito.when(userService.updateUser(anyLong(), any(UserUpdateRequest.class))).thenReturn(mockResponse);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updatedUser");

        // Act & Assert
        given()
                .auth().oauth2(userToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().put("/api/users/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("username", is("updatedUser"))
                .body("email", is("test@example.com"));
    }

    // --- 測試刪除 API ---
    @Test
    @DisplayName("DELETE /{id} - 應呼叫 service.deleteUser 並回傳 204")
    void testDeleteUser() {
        // Arrange
        String userToken = generateTestToken("test@example.com", "user");
        // 對於回傳 void 的方法，使用 doNothing()
        Mockito.doNothing().when(userService).deleteUser(anyLong());

        // Act & Assert
        given()
                .auth().oauth2(userToken)
                .when().delete("/api/users/1")
                .then()
                .statusCode(204); // 驗證 204 No Content
    }

    // --- 測試未授權 ---
    @Test
    @DisplayName("GET /me - 未帶 Token 應回傳 401")
    void testGetMe_Unauthorized() {
        Mockito.when(userService.getCurrentUser()).thenThrow(new AuthenticationFailedException());
        // Act & Assert
        given()
                .when().get("/api/users/me")
                .then()
                .statusCode(401);
    }
}