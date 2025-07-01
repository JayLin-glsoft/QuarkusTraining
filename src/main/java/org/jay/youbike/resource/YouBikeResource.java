package org.jay.youbike.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jay.youbike.model.dto.StationChangeEvent;
import org.jay.youbike.model.dto.YouBikeStationDTO;
import org.jay.youbike.service.ChangeQueryService;
import org.jay.youbike.service.YouBikeService;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@Path("/api/youbike")
@Produces(MediaType.APPLICATION_JSON)
public class YouBikeResource {
    private static final Logger LOG = Logger.getLogger(YouBikeResource.class);

    @Inject
    YouBikeService youBikeService;

    @Inject
    ChangeQueryService changeQueryService;

    @POST
    @Path("/import")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public void importData() {
        LOG.info("Received request to import YouBike data.");
        youBikeService.importStations();
    }

    @GET
    @Path("/all") // 此端點現在從資料庫讀取
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public List<YouBikeStationDTO> getAllYouBikeStationsFromDb() {
        return youBikeService.getAllStationsFromDb();
    }

    @GET
    @Path("/station/{id}")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public YouBikeStationDTO getYouBikeStationById(@PathParam("id") String stationId) {
        // *** 新增的防呆機制 ***
        if (stationId == null || stationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be empty.");
        }
        return youBikeService.getStationById(stationId);
    }

    @GET
    @Path("/recent")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecentChanges() {
        LOG.info("Received request for recent YouBike changes.");
        List<StationChangeEvent> recentChanges = changeQueryService.getRecentChanges();
        Map<String, Object> response = Map.of(
                "count", recentChanges.size(),
                "changes", recentChanges
        );
        return Response.ok(response).build();
    }
}
