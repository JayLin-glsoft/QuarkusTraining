package org.jay.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jay.client.YouBikeApiClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jay.dto.YouBikeStationDTO;
import org.jay.entity.YouBikeStationEntity;
import org.jay.mapper.YouBikeStationMapper;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class YouBikeService {
    private static final Logger LOG = Logger.getLogger(YouBikeService.class.getName());
    private static final String CACHE_KEY_PREFIX = "station:";
    private static final long CACHE_TTL_SECONDS = 3600; // 快取 1 小時

    @Inject
    @RestClient
    YouBikeApiClient youBikeApiClient;

    @Inject
    YouBikeStationMapper stationMapper;

    @Inject
    RedisDataSource redisDataSource;

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
    public Optional<YouBikeStationDTO> getStationById(String stationId) {
        String cacheKey = CACHE_KEY_PREFIX + stationId;

        // 1. 從 Redis 快取查詢
        try {
            ValueCommands<String, YouBikeStationEntity> stationCache = redisDataSource.value(YouBikeStationEntity.class);
            YouBikeStationEntity cachedStation = stationCache.get(cacheKey);
            if (cachedStation != null) {
                LOG.infof("Cache HIT for station ID: %s", stationId);
                return Optional.of(stationMapper.toDto(cachedStation));
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error accessing Redis cache for key: %s", cacheKey);
        }

        LOG.infof("Cache MISS for station ID: %s. Fetching from DB.", stationId);

        // 2. 快取未命中，從 MongoDB 查詢
        YouBikeStationEntity stationFromDb = YouBikeStationEntity.findById(stationId);

        if (stationFromDb != null) {
            // 3. 將從 DB 查到的資料寫入快取，並設定 TTL
            try {
                ValueCommands<String, YouBikeStationEntity> stationCache = redisDataSource.value(YouBikeStationEntity.class);
                stationFromDb.setLastUpdate(Instant.now().toString());
                stationCache.setex(cacheKey, CACHE_TTL_SECONDS, stationFromDb);
                LOG.infof("Station ID %s data stored in cache.", stationId);
            } catch (Exception e) {
                LOG.errorf(e, "Error writing to Redis cache for key: %s", cacheKey);
            }
            return Optional.of(stationMapper.toDto(stationFromDb));
        }

        LOG.warnf("Station ID %s not found in DB.", stationId);
        return Optional.empty();
    }

    /**
     * 從外部 API 匯入資料到 MongoDB
     */
    public long importStations() {
        try {
            LOG.infof("Fetching YouBike data from URL: %s", apiClientUrl);
            List<YouBikeStationEntity> youBikeList = youBikeApiClient.getYouBikeList();

            // 使用 Panache 的 persist 方法儲存資料
            YouBikeStationEntity.persistOrUpdate(youBikeList);

            LOG.infof("Successfully imported %d stations into MongoDB.", youBikeList.size());
            return youBikeList.size();
        } catch (Exception e) {
            LOG.error("Exception during data import", e);
            return 0;
        }
    }
}
