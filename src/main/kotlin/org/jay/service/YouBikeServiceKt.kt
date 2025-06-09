package org.jay.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.client.ClientBuilder
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jay.model.YouBikeStationKt
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger

@ApplicationScoped
class YouBikeServiceKt {

    companion object {
        private val LOG: Logger = Logger.getLogger(YouBikeServiceKt::class.java)
    }

    @ConfigProperty(name = "youbike.api.url")
    lateinit var youbikeApiUrl: String // lateinit 允許非 null 型別延遲初始化

    private fun fetchAllStationsFromExternalApi(): List<YouBikeStationKt> {
        if (!::youbikeApiUrl.isInitialized) {
            LOG.error("YouBike API URL is not initialized.")
            return emptyList()
        }
        val client = ClientBuilder.newClient()
        return try {
            LOG.infof("Fetching YouBike data from URL: %s", youbikeApiUrl)
            val response = client.target(youbikeApiUrl)
                .request(MediaType.APPLICATION_JSON)
                .get()

            if (response.statusInfo.family == Response.Status.Family.SUCCESSFUL) {
                val stationsArray = response.readEntity(Array<YouBikeStationKt>::class.java)
                LOG.infof("Successfully fetched %d stations.", stationsArray.size)
                stationsArray.toList()
            } else {
                LOG.errorf("Error fetching YouBike data: Status %d - %s", response.status, response.readEntity(String::class.java))
                emptyList()
            }
        } catch (e: Exception) {
            LOG.error("Exception while fetching YouBike data", e)
            emptyList()
        } finally {
            client.close()
        }
    }

    fun getAllStations(): List<YouBikeStationKt> {
        return fetchAllStationsFromExternalApi()
    }

    fun getStationById(stationId: String): YouBikeStationKt? {
        val stations = fetchAllStationsFromExternalApi()
        return stations.find { it.stationNo == stationId }
    }
}