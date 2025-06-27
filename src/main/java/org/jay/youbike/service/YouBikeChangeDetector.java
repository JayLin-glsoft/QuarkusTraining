package org.jay.youbike.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jay.youbike.client.YouBikeApiClient;
import org.jay.youbike.model.dto.StationChangeEvent;
import org.jay.youbike.model.entity.YouBikeStationEntity; // 重用第二週的 MongoDB Entity
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// 功能一：站點異動偵測
@ApplicationScoped
public class YouBikeChangeDetector {

    private static final Logger LOG = Logger.getLogger(YouBikeChangeDetector.class);

    @Inject
    @RestClient
    YouBikeApiClient youBikeApiClient;

    @Inject
    @Channel("station-changes-out") // 注入 Kafka Producer Channel
    Emitter<StationChangeEvent> changeEmitter;

    @ConfigProperty(name = "quarkus.rest-client.you-bike-api.url")
    String apiClientUrl;


    // 每分鐘執行一次，initialDelay 設為 10 秒是為了等應用程式啟動完成
    @Scheduled(every = "60s", delayed = "10s")
    public void detectChanges() {
        LOG.info("Starting YouBike change detection task...");

        // 1. 從 MongoDB 獲取現有資料
        List<YouBikeStationEntity> existingStations = YouBikeStationEntity.listAll();
        Map<String, YouBikeStationEntity> existingStationsMap = existingStations.stream()
                .collect(Collectors.toMap(YouBikeStationEntity::getStationNo, Function.identity()));

        // 2. 從外部 API 獲取最新資料
        Client client = ClientBuilder.newClient();
        try {
            LOG.info("Starting import of YouBike stations from API: " + apiClientUrl);
            List<YouBikeStationEntity> youBikeList = youBikeApiClient.getYouBikeList();

            // 3. 比對資料並發送異動事件
            for (YouBikeStationEntity latest : youBikeList) {
                YouBikeStationEntity existing = existingStationsMap.get(latest.getStationNo());
                // 只有當站點已存在，且可借車輛數不同時，才視為異動
                if (existing != null && existing.getAvailableBikes() != latest.getAvailableBikes()) {
                    int delta = latest.getAvailableBikes() - existing.getAvailableBikes();
                    LOG.infof("Change detected for station %s (%s): %d -> %d, delta: %d",
                            latest.getStationNo(), latest.getStationName(), existing.getAvailableBikes(), latest.getAvailableBikes(), delta);

                    // 建立事件物件
                    StationChangeEvent event = new StationChangeEvent(
                            latest.getStationNo(),
                            latest.getStationName(),
                            existing.getAvailableBikes(),
                            latest.getAvailableBikes(),
                            delta,
                            LocalDateTime.now()
                    );

                    // 發送到 Kafka
                    changeEmitter.send(event).toCompletableFuture().exceptionally(ex -> {
                        LOG.errorf("Failed to send change event to Kafka for station %s: %s", latest.getStationNo(), ex.getMessage());
                        return null;
                    });
                }
            }

            // 4. 將最新資料更新回 MongoDB，作為下一次比對的基準
            YouBikeStationEntity.persistOrUpdate(youBikeList);
            LOG.info("Updated MongoDB with latest station data.");

        } catch (Exception e) {
            LOG.error("Error during change detection task", e);
        } finally {
            client.close();
        }
    }
}