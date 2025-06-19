package org.jay.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.jay.youbike.model.dto.YouBikeStationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jay.youbike.service.YouBikeService;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class YouBikeResourceTest {

    // 使用 @InjectMock 來注入一個 Mock 的 YouBikeService
    // Quarkus 會自動處理 Mockito 的設定
    @InjectMock
    YouBikeService youBikeService;

    private YouBikeStationDTO stationDto1;
    private YouBikeStationDTO stationDto2;

    @BeforeEach
    void setUp() {
        // 準備測試用的假 DTO (Data Transfer Object) 資料
        // 這對應第二週的 API 回應結構
        stationDto1 = new YouBikeStationDTO();
        stationDto1.setStationNo("500101001");
        stationDto1.setStationName("YouBike2.0_捷運科技大樓站");
        stationDto1.setArea("大安區");

        stationDto2 = new YouBikeStationDTO();
        stationDto2.setStationNo("500101002");
        stationDto2.setStationName("YouBike2.0_復興南路二段273號前");
        stationDto2.setArea("大安區");
    }

    @Test
    void testImportEndpoint_Success() {
        // Arrange: 模擬當 youBikeService.importStations() 被呼叫時，什麼都不做 (因為是 void 方法)
        Mockito.doNothing().when(youBikeService).importStations();

        // Act & Assert
        given()
                .when().post("/api/youbike/import")
                .then()
                .statusCode(204); // 驗證 void 方法成功時回傳 204 No Content

        // Verify: 確認 service 的 importStations 方法確實被呼叫了一次
        Mockito.verify(youBikeService, Mockito.times(1)).importStations();
    }

    @Test
    void testImportEndpoint_Failure() {
        // Arrange: 模擬當 youBikeService.importStations() 被呼叫時，拋出一個執行時例外
        String errorMessage = "Simulated database connection error";
        Mockito.doThrow(new RuntimeException(errorMessage))
                .when(youBikeService)
                .importStations();

        // Act & Assert: 驗證 ExceptionHandler 是否正確處理例外
        given()
                .when().post("/api/youbike/import")
                .then()
                .statusCode(400) // 根據您的 ExceptionHandler.java，應回傳 400
                .contentType(ContentType.JSON) // ExceptionHandler 回傳的是純文字
                .body("message", is(errorMessage)); // 驗證回應內容就是我們的例外訊息
    }

    @Test
    void testGetAllStationsFromDb_Success() {
        // Arrange: 模擬當呼叫 youBikeService.getAllStationsFromDb() 時，回傳準備好的假資料
        Mockito.when(youBikeService.getAllStationsFromDb()).thenReturn(List.of(stationDto1, stationDto2));

        // Act & Assert
        given()
                .when().get("/api/youbike/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2)) // 驗證回傳的陣列大小為 2
                .body("[0].stationNo", is("500101001"))
                .body("[0].stationName", is("YouBike2.0_捷運科技大樓站"))
                .body("[1].stationNo", is("500101002"));
    }

    @Test
    void testGetAllStationsFromDb_Empty() {
        // Arrange: 模擬當呼叫 youBikeService.getAllStationsFromDb() 時，回傳空集合
        Mockito.when(youBikeService.getAllStationsFromDb()).thenReturn(Collections.emptyList());

        // Act & Assert
        given()
                .when().get("/api/youbike/all")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void testGetStationById_Success() {
        // Arrange: 模擬當呼叫 youBikeService.getStationById() 且 ID 為 "500101001" 時，回傳 stationDto1
        Mockito.when(youBikeService.getStationById("500101001")).thenReturn(stationDto1);

        // Act & Assert
        given()
                .pathParam("id", "500101001")
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("stationNo", is("500101001"))
                .body("stationName", is("YouBike2.0_捷運科技大樓站"));
    }

    @Test
    void testGetStationById_NotFound() {
        // Arrange: 模擬當呼叫 youBikeService.getStationById() 且 ID 為 "99999" 時，回傳 NoSuchElementException
        String nonExistentId = "99999";
        Mockito.when(youBikeService.getStationById(nonExistentId))
                .thenThrow(new NoSuchElementException("YouBike station not found with id: " + nonExistentId));

        // Act & Assert
        given()
                .pathParam("id", nonExistentId)
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", is("YouBike station not found with id: " + nonExistentId));
    }

    @Test
    void testGetStationById_BadRequest() {
        // 準備一個空白的 ID
        String blankId = " ";

        // 這裡不需要 Mockito，因為請求會在進入 Service 層之前就被 JAX-RS 驗證或您的 Resource 邏輯攔截

        // Act & Assert
        given()
                .pathParam("id", blankId)
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("message", is("Station ID cannot be empty."));
    }
}
