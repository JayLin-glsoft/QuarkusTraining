package main.java.org.jay.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import main.java.org.jay.model.YouBikeStation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class YouBikeService {

    private static final Logger LOG = Logger.getLogger(YouBikeService.class.getName());

    @ConfigProperty(name = "youbike.api.url")
    String youbikeApiUrl;

    private List<YouBikeStation> fetchAllStationsFromExternalApi() {
        try (Client client = ClientBuilder.newClient()) {
            LOG.infof("Fetching YouBike data from URL: %s", youbikeApiUrl);
            Response response = client.target(youbikeApiUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                YouBikeStation[] stationsArray = response.readEntity(YouBikeStation[].class);
                LOG.infof("Successfully fetched %d stations.", stationsArray.length);
                return Arrays.asList(stationsArray);
            } else {
                LOG.errorf("Error fetching YouBike data: Status %d - %s", response.getStatus(), response.readEntity(String.class));
                return Collections.emptyList();
            }
        } catch (Exception e) {
            LOG.error("Exception while fetching YouBike data", e);
            return Collections.emptyList();
        }
    }

    public List<YouBikeStation> getAllStations() {
        return fetchAllStationsFromExternalApi();
    }

    public Optional<YouBikeStation> getStationById(String stationId) {
        List<YouBikeStation> stations = fetchAllStationsFromExternalApi();
        return stations.stream()
                .filter(station -> station.stationNo() != null && station.stationNo().equals(stationId))
                .findFirst();
    }
}
