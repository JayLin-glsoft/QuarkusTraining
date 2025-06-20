package org.jay.youbike.service;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jay.youbike.client.YouBikeApiClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jay.youbike.model.dto.YouBikeStationDTO;
import org.jay.youbike.model.entity.YouBikeStationEntity;
import org.jay.youbike.model.mapper.YouBikeStationMapper;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.NoSuchElementException;

@ApplicationScoped
public class YouBikeService {
    private static final Logger LOG = Logger.getLogger(YouBikeService.class.getName());

    @Inject
    @RestClient
    YouBikeApiClient youBikeApiClient;

    @Inject
    YouBikeStationMapper stationMapper;

    @ConfigProperty(name = "quarkus.rest-client.you-bike-api.url")
    String apiClientUrl;

    /**
     * 查詢所有站點，從 MongoDB 讀取
     */
    public List<YouBikeStationDTO> getAllStationsFromDb() {
        List<YouBikeStationEntity> entities = YouBikeStationEntity.listAll();
        return stationMapper.toDtoList(entities);
    }

    /**
     * 查詢指定 ID 的站點，實作 Cache-Aside 模式
     */
    @CacheResult(cacheName = "stations")
    public YouBikeStationDTO getStationById(String stationId) {
        YouBikeStationEntity stationFromDb = YouBikeStationEntity.findById(stationId);
        if (stationFromDb != null) {
            LOG.infof("Found station ID %s in DB.", stationId);
            return stationMapper.toDto(stationFromDb);
        }
        throw new NoSuchElementException("YouBike station not found with id: " + stationId);
    }

    /**
     * 從外部 API 匯入資料到 MongoDB
     */
    @Scheduled(identity = "import-stations-task", every = "5m", delayed = "5s")
    @CacheInvalidateAll(cacheName = "stations")
    public void importStations() {
        LOG.info("Starting import of YouBike stations from API: " + apiClientUrl);
        List<YouBikeStationEntity> youBikeList = youBikeApiClient.getYouBikeList();
        // 使用 Panache 的 persist 方法儲存資料
        YouBikeStationEntity.persistOrUpdate(youBikeList);
    }
}
