package org.jay.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jay.client.YouBikeApiClient;
import org.jay.model.YouBikeStation;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class YouBikeService {

    private static final Logger LOG = Logger.getLogger(YouBikeService.class.getName());

    @Inject
    @RestClient
    YouBikeApiClient youBikeApiClient;

    @ConfigProperty(name = "quarkus.rest-client.you-bike-api.url")
    String apiClientUrl;

    private List<YouBikeStation> fetchAllStationsFromExternalApi() {
        try {
            LOG.infof("Fetching YouBike data from URL: %s", apiClientUrl);
            return youBikeApiClient.getYouBikeList();
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
