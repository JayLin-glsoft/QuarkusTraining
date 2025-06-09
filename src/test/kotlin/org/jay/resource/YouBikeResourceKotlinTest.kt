package org.jay.resource

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.hasSize
import org.jay.model.YouBikeStationKt
import org.jay.service.YouBikeServiceKt
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@QuarkusTest
class YouBikeResourceKotlinTest {

    @InjectMock
    lateinit var youBikeService: YouBikeServiceKt

    private lateinit var station1: YouBikeStationKt
    private lateinit var station2: YouBikeStationKt

    @BeforeEach
    fun setUp() {
        station1 = YouBikeStationKt(
            "500101001", "捷運市府站(3號出口)", 30, 10,
            "信義區", "20250606174000", 25.040857, 121.567904,
            "忠孝東路/松仁路(東南側)", 20, "1"
        )
        station2 = YouBikeStationKt(
            "500101002", "捷運國父紀念館站(2號出口)", 40, 5,
            "信義區", "20250606174000", 25.041254, 121.55742,
            "忠孝東路四段/光復南路口", 35, "1"
        )
    }

    @Test
    fun `test get all youbike stations successfully`() {
        Mockito.`when`(youBikeService.getAllStations()).thenReturn(listOf(station1, station2))

        When {
            get("/kotlin_api/youbike/all")
        } Then {
            statusCode(200)
            contentType(ContentType.JSON)
            body("$", hasSize<Any>(2))
            body("[0].stationNo", `is`("500101001"))
            body("[0].stationName", `is`("捷運市府站(3號出口)"))
            body("[1].stationNo", `is`("500101002"))
        }
    }

    @Test
    fun `test get all youbike stations when not found`() {
        Mockito.`when`(youBikeService.getAllStations()).thenReturn(emptyList())

        When {
            get("/kotlin_api/youbike/all")
        } Then {
            statusCode(404)
            body("message", `is`("No YouBike stations data available."))
        }
    }

    @Test
    fun `test get youbike station by id successfully`() {
        val stationId = "500101001"
        Mockito.`when`(youBikeService.getStationById(stationId)).thenReturn(station1)

        When {
            get("/kotlin_api/youbike/station/{id}", stationId)
        } Then {
            statusCode(200)
            contentType(ContentType.JSON)
            body("stationNo", `is`(stationId))
            body("stationName", `is`("捷運市府站(3號出口)"))
        }
    }

    @Test
    fun `test get youbike station by id when not found`() {
        val nonExistentId = "99999"
        Mockito.`when`(youBikeService.getStationById(nonExistentId)).thenReturn(null)

        When {
            get("/kotlin_api/youbike/station/{id}", nonExistentId)
        } Then {
            statusCode(404)
            body("message", `is`("Station with ID $nonExistentId not found."))
        }
    }

    @Test
    fun `test get youbike station by id with blank id`() {
        val blankId = " "

        When {
            get("/kotlin_api/youbike/station/{id}", blankId)
        } Then {
            statusCode(400)
            body("error", `is`("Station ID cannot be empty."))
        }
    }
}