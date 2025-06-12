package org.jay.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.jay.dto.YouBikeStationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jay.service.YouBikeService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
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
        // Arrange: 模擬當 youBikeService.importStations() 被呼叫時，回傳成功匯入 50 筆
        Mockito.when(youBikeService.importStations()).thenReturn(50L);

        // Act & Assert
        given()
                .when().post("/api/youbike/import")
                .then()
                .statusCode(200)
                .body("message", containsString("Successfully imported 50 stations"));
    }

    @Test
    void testImportEndpoint_Failure() {
        // Arrange: 模擬當 youBikeService.importStations() 被呼叫時，回傳 0 (表示失敗)
        Mockito.when(youBikeService.importStations()).thenReturn(0L);

        // Act & Assert
        given()
                .when().post("/api/youbike/import")
                .then()
                .statusCode(500)
                .body("error", containsString("Failed to import station data"));
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
                .statusCode(404)
                .body("message", containsString("No YouBike stations data in the database"));
    }

    @Test
    void testGetStationById_Success() {
        // Arrange: 模擬當呼叫 youBikeService.getStationById() 且 ID 為 "500101001" 時，回傳 stationDto1
        Mockito.when(youBikeService.getStationById("500101001")).thenReturn(Optional.of(stationDto1));

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
        // Arrange: 模擬當呼叫 youBikeService.getStationById() 且 ID 為 "99999" 時，回傳空的 Optional
        String nonExistentId = "99999";
        Mockito.when(youBikeService.getStationById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        given()
                .pathParam("id", nonExistentId)
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(404)
                .body("message", is("Station with ID " + nonExistentId + " not found."));
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
                .body("error", is("Station ID cannot be empty."));
    }
}
