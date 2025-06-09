package org.jay.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jay.model.YouBikeStation;
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

    @GET
    @Path("/all")
    public Response getAllYouBikeStations() {
        LOG.info("Received request for /api/youbike/all");
        List<YouBikeStation> stations = youBikeService.getAllStations();
        if (stations.isEmpty()) {
            LOG.warn("No YouBike stations data found to return.");
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"No YouBike stations data available.\"}")
                    .build();
        }
        return Response.ok(stations).build();
    }

    @GET
    @Path("/station/{id}")
    public Response getYouBikeStationById(@PathParam("id") String stationId) {
        LOG.infof("Received request for /api/youbike/station/%s", stationId);
        if (stationId == null || stationId.trim().isEmpty()) {
            LOG.warn("Received empty station ID.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Station ID cannot be empty.\"}")
                    .build();
        }

        Optional<YouBikeStation> stationOpt = youBikeService.getStationById(stationId);

        return stationOpt.map(station -> {
            LOG.infof("Station found for ID %s: %s", stationId, station.stationName());
            return Response.ok(station).build();
        }).orElseGet(() -> {
            LOG.warnf("Station with ID %s not found.", stationId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\": \"Station with ID " + stationId + " not found.\"}")
                    .build();
        });
    }
}
