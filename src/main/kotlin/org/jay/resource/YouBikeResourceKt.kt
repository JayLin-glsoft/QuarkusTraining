package org.jay.resource

import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jay.service.YouBikeServiceKt
import org.jboss.logging.Logger

@Path("/kotlin_api/youbike")
@Produces(MediaType.APPLICATION_JSON)
class YouBikeResourceKt {

    companion object {
        private val LOG: Logger = Logger.getLogger(YouBikeResourceKt::class.java.name)
    }

    @Inject
    private lateinit var youBikeService: YouBikeServiceKt // 使用 lateinit 進行注入

    @GET
    @Path("/all")
    fun getAllYouBikeStations(): Response {
        LOG.info("Received request for /api/youbike/all (Kotlin)")
        val stations = youBikeService.getAllStations()
        return if (stations.isEmpty()) {
            LOG.warn("No YouBike stations data found to return. (Kotlin)")
            Response.status(Response.Status.NOT_FOUND)
                .entity("{\"message\": \"No YouBike stations data available.\"}")
                .build()
        } else {
            LOG.info("Received data: $stations")
            Response.ok(stations).build()
        }
    }

    @GET
    @Path("/station/{id}")
    fun getYouBikeStationById(@PathParam("id") stationId: String): Response {
        LOG.infof("Received request for /api/youbike/station/%s (Kotlin)", stationId)
        if (stationId.isBlank()) { // Kotlin 的 isBlank() 更簡潔
            LOG.warn("Received empty station ID. (Kotlin)")
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Station ID cannot be empty.\"}")
                .build()
        }

        val station = youBikeService.getStationById(stationId)

        return station?.let { // Kotlin 的安全呼叫和 let 函數
            LOG.infof("Station found for ID %s: %s (Kotlin)", stationId, it.stationName)
            Response.ok(it).build()
        } ?: run { // Elvis 運算符和 run
            LOG.warnf("Station with ID %s not found. (Kotlin)", stationId)
            Response.status(Response.Status.NOT_FOUND)
                .entity("{\"message\": \"Station with ID $stationId not found.\"}") // Kotlin 的字串模板
                .build()
        }
    }
}