package org.jay.youbike.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jay.youbike.model.dto.YouBikeStationDTO;
import org.jay.youbike.service.YouBikeService;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/youbike")
@Produces(MediaType.APPLICATION_JSON)
public class YouBikeResource {
    private static final Logger LOG = Logger.getLogger(YouBikeResource.class);

    @Inject
    YouBikeService youBikeService;

    @POST
    @Path("/import")
    @Produces(MediaType.APPLICATION_JSON)
    public void importData() {
        LOG.info("Received request to import YouBike data.");
        youBikeService.importStations();
    }

    @GET
    @Path("/all") // 此端點現在從資料庫讀取
    @Produces(MediaType.APPLICATION_JSON)
    public List<YouBikeStationDTO> getAllYouBikeStationsFromDb() {
        return youBikeService.getAllStationsFromDb();
    }

    @GET
    @Path("/station/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public YouBikeStationDTO getYouBikeStationById(@PathParam("id") String stationId) {
        // *** 新增的防呆機制 ***
        if (stationId == null || stationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be empty.");
        }
        return youBikeService.getStationById(stationId);
    }
}
