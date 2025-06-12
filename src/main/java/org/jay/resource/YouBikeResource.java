package org.jay.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jay.dto.YouBikeStationDTO;
import org.jay.service.YouBikeService;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;

@Path("/api/youbike")
@Produces(MediaType.APPLICATION_JSON)
public class YouBikeResource {
    private static final Logger LOG = Logger.getLogger(YouBikeResource.class);

    @Inject
    YouBikeService youBikeService;

    @POST
    @Path("/import")
    public Response importData() {
        LOG.info("Received request to import YouBike data.");
        long count = youBikeService.importStations();
        if (count > 0) {
            return Response.ok("{\"message\": \"Successfully imported " + count + " stations.\"}").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to import station data.\"}")
                    .build();
        }
    }

    @GET
    @Path("/all") // 此端點現在從資料庫讀取
    public Response getAllYouBikeStationsFromDb() {
        List<YouBikeStationDTO> stations = youBikeService.getAllStationsFromDb();
        if (stations.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"No YouBike stations data in the database. Please run POST /api/youbike/import first.\"}")
                    .build();
        }
        return Response.ok(stations).build();
    }

    @GET
    @Path("/station/{id}")
    public Response getYouBikeStationById(@PathParam("id") String stationId) {
        if (stationId == null || stationId.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Station ID cannot be empty.\"}")
                    .build();
        }

        Optional<YouBikeStationDTO> stationOpt = youBikeService.getStationById(stationId);

        return stationOpt.map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"message\": \"Station with ID " + stationId + " not found.\"}")).build();
    }
}
