package org.jay.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.jay.service.YouBikeService;
import org.jay.model.YouBikeStation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class YouBikeResourceTest {

    @InjectMock
    YouBikeService youBikeService;

    private YouBikeStation station1;
    private YouBikeStation station2;

    @BeforeEach
    void setUp() {
        // 準備測試用的假資料
        station1 = new YouBikeStation("500101001", "捷運市府站(3號出口)", 30, 10, "信義區",
                "20250606174000", 25.040857, 121.567904, "忠孝東路/松仁路(東南側)", 20, "1");
        station2 = new YouBikeStation("500101002", "捷運國父紀念館站(2號出口)", 40, 5, "信義區",
                "20250606174000", 25.041254, 121.55742, "忠孝東路四段/光復南路口", 35, "1");
    }

    @Test
    void testGetAllYouBikeStations_Success() {
        // Arrange: 當呼叫 youBikeService.getAllStations() 時，回傳準備好的假資料
        Mockito.when(youBikeService.getAllStations()).thenReturn(List.of(station1, station2));

        // Act & Assert
        given()
                .when().get("/api/youbike/all")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2)) // 驗證回傳的陣列大小為 2
                .body("[0].sno", is("500101001"))
                .body("[0].sna", is("捷運市府站(3號出口)"))
                .body("[1].sno", is("500101002"));
    }

    @Test
    void testGetAllYouBikeStations_NotFound() {
        // Arrange: 當呼叫 youBikeService.getAllStations() 時，回傳空集合
        Mockito.when(youBikeService.getAllStations()).thenReturn(Collections.emptyList());

        // Act & Assert
        given()
                .when().get("/api/youbike/all")
                .then()
                .statusCode(404) // 根據您的實作，應回傳 404
                .body("message", is("No YouBike stations data available."));
    }

    @Test
    void testGetYouBikeStationById_Success() {
        // Arrange: 當呼叫 youBikeService.getStationById() 且 ID 為 "500101001" 時，回傳 station1
        Mockito.when(youBikeService.getStationById("500101001")).thenReturn(Optional.of(station1));

        // Act & Assert
        given()
                .pathParam("id", "500101001")
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("sno", is("500101001"))
                .body("sna", is("捷運市府站(3號出口)"));
    }

    @Test
    void testGetYouBikeStationById_NotFound() {
        // Arrange: 當呼叫 youBikeService.getStationById() 且 ID 為 "99999" 時，回傳空的 Optional
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
    void testGetYouBikeStationById_BadRequest() {
        // Arrange: 準備一個空白的 ID
        String blankId = " ";

        // Act & Assert
        given()
                .pathParam("id", blankId)
                .when().get("/api/youbike/station/{id}")
                .then()
                .statusCode(400) // 根據您的實作，應回傳 400
                .body("error", is("Station ID cannot be empty."));
    }
}
