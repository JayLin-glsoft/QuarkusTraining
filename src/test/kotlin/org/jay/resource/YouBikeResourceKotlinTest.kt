package org.jay.resource

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import main.kotlin.org.jay.model.YouBikeStation
import main.kotlin.org.jay.service.YouBikeService
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@QuarkusTest
class YouBikeResourceKotlinTest {

    @InjectMock
    lateinit var youBikeService: YouBikeService

    private lateinit var station1: YouBikeStation
    private lateinit var station2: YouBikeStation

    @BeforeEach
    fun setUp() {
        station1 = YouBikeStation(
            stationNo = "500101001", stationName = "捷運市府站(3號出口)", totalSpaces = 30, availableBikes = 10,
            area = "信義區", updateTime = "20250606174000", latitude = 25.040857, longitude = 121.567904,
            address = "忠孝東路/松仁路(東南側)", availableSpaces = 20, active = "1"
        )
        station2 = YouBikeStation(
            stationNo = "500101002", stationName = "捷運國父紀念館站(2號出口)", totalSpaces = 40, availableBikes = 5,
            area = "信義區", updateTime = "20250606174000", latitude = 25.041254, longitude = 121.55742,
            address = "忠孝東路四段/光復南路口", availableSpaces = 35, active = "1"
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
            body("[0].sno", `is`("500101001"))
            body("[0].sna", `is`("捷運市府站(3號出口)"))
            body("[1].sno", `is`("500101002"))
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
            body("sno", `is`(stationId))
            body("sna", `is`("捷運市府站(3號出口)"))
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