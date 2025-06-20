package org.jay.youbike.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jay.youbike.model.entity.YouBikeStationEntity;

import java.util.List;

@RegisterRestClient(configKey = "you-bike-api")
public interface YouBikeApiClient {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<YouBikeStationEntity> getYouBikeList();
}
