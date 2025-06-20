package org.jay.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import org.jay.user.model.dto.LoginRequest;
import org.jay.user.model.dto.RegisterRequest;
import org.jay.user.model.entity.User;
import org.jay.user.repository.UserRepository;
import org.jay.user.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
public class UserResourceTest {

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    TokenService tokenService;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        Mockito.reset(userRepository, tokenService);

        testUser = new User();
        testUser.id = 1L;
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setAdmin(false);
        testUser.setCreatedTime(Instant.now());

        testAdmin = new User();
        testAdmin.id = 2L;
        testAdmin.setUsername("adminuser");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword("adminpass");
        testAdmin.setAdmin(true);
        testAdmin.setCreatedTime(Instant.now());
    }

    // --- 測試註冊 API (JSON Body) ---
    @Test
    void testRegister_Success() {
        // Arrange
        Mockito.when(userRepository.findByEmail(anyString())).thenReturn(null);
        Mockito.when(userRepository.add(anyString(), anyString(), anyString(), anyBoolean())).thenReturn(testUser);
        Mockito.when(tokenService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");

        RegisterRequest request = new RegisterRequest();
        request.username = "testuser";
        request.password = "password123";
        request.email = "newuser@example.com";

        // Act & Assert
        given()
                .contentType(ContentType.JSON) // 指定 Content-Type 為 JSON
                .body(request) // 將 DTO 物件作為 body
                .when().post("/api/users/register")
                .then()
                .statusCode(201)
                .body("token", is("mocked_jwt_token"));

        Mockito.verify(userRepository, Mockito.times(1)).add("testuser", "password123", "newuser@example.com", false);
    }

    @Test
    void testRegister_UserAlreadyExists() {
        // Arrange
        Mockito.when(userRepository.findByEmail("existing@example.com")).thenReturn(testUser);

        RegisterRequest request = new RegisterRequest();
        request.username = "anotheruser";
        request.password = "password123";
        request.email = "existing@example.com";

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/users/register")
                .then()
                .statusCode(400)
                .body("message", is("This user already exists"));
    }

    // --- 測試登入 API (JSON Body) ---
    @Test
    void testLogin_Success() {
        // Arrange
        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        Mockito.when(tokenService.generateToken(testUser)).thenReturn("login_success_token");

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

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        LoginRequest request = new LoginRequest();
        request.email = "test@example.com";
        request.password = "wrongpassword";

        // Act & Assert
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/users/login")
                .then()
                .statusCode(400)
                .body("message", is("Invalid username or password"));
    }

    // --- 測試需驗證的 API (與前一版相同，因為它們本來就是正確的格式) ---

    private String generateTestToken(String email, String... roles) {
        return Jwt.issuer("https://myapp.com/issuer")
                .upn(email)
                .groups(Set.of(roles))
                .sign();
    }

    @Test
    void testGetMe_Success() {
        // Arrange
        String userToken = generateTestToken("test@example.com", "user");
        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        given()
                .auth().oauth2(userToken)
                .when().get("/api/users/me")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("username", is("testuser"))
                .body("email", is("test@example.com"));
    }

    @Test
    void testGetMe_Unauthorized() {
        // Act & Assert
        given()
                .when().get("/api/users/me")
                .then()
                .statusCode(401);
    }

    @Test
    void testUpdateUser_UserUpdatesSelf_Success() {
        // Arrange
        String userToken = generateTestToken("test@example.com", "user");
        Mockito.when(userRepository.findById(1L)).thenReturn(testUser);

        // 準備 request body (使用原始的 RegisterRequest 結構)
        RegisterRequest request = new RegisterRequest();
        request.username = "updatedUser";
        request.email = "updated@example.com";
        request.password = "newPassword123";

        // Act & Assert
        given()
                .auth().oauth2(userToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().put("/api/users/1")
                .then()
                .statusCode(200)
                .body("message", is("User updated successfully"));

        Mockito.verify(userRepository).persist(any(User.class));
    }

    @Test
    void testUpdateUser_AdminUpdatesOther_Success() {
        // Arrange
        String adminToken = generateTestToken("admin@example.com", "admin", "user");
        Mockito.when(userRepository.findById(1L)).thenReturn(testUser);

        RegisterRequest request = new RegisterRequest();
        request.username = "updatedByAdmin";
        request.email = "updatedByAdmin@example.com";
        request.password = ""; // 密碼留空，不更新

        // Act & Assert
        given()
                .auth().oauth2(adminToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().put("/api/users/1")
                .then()
                .statusCode(200);
    }

    @Test
    void testUpdateUser_UserUpdatesOther_Forbidden() {
        // Arrange
        String attackerToken = generateTestToken("attacker@example.com", "user");
        Mockito.when(userRepository.findById(1L)).thenReturn(testUser);

        RegisterRequest request = new RegisterRequest(); // body 內容不重要

        // Act & Assert
        given()
                .auth().oauth2(attackerToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().put("/api/users/1")
                .then()
                .statusCode(403);
    }
}

