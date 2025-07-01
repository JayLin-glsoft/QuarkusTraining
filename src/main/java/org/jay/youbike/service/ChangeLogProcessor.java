package org.jay.youbike.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jay.youbike.model.mapper.ChangeLogMapper;
import org.jay.youbike.model.dto.StationChangeEvent;
import org.jay.youbike.model.entity.StationChangeLog;
import org.jboss.logging.Logger;

// 功能二：異動訊息消費與通知
@ApplicationScoped
public class ChangeLogProcessor {

    private static final Logger LOG = Logger.getLogger(ChangeLogProcessor.class);
    private static final String REDIS_RECENT_CHANGES_KEY = "recent_changes";
    private static final int MAX_RECENT_CHANGES = 10; // 在 Redis 中只保留最近 10 筆

    private final ListCommands<String, StationChangeEvent> recentChangesCache;

    @Inject
    ChangeLogMapper changeLogMapper; // 注入新的 Mapper

    @Inject
    public ChangeLogProcessor(RedisDataSource redisDataSource) {
        // 創建一個針對 List 操作的 Redis 命令介面
        this.recentChangesCache = redisDataSource.list(StationChangeEvent.class);
    }

    // 監聽 Kafka 的 'station-changes-in' channel
    @Incoming("station-changes-in")
    @Transactional // 因為需要寫入 MySQL，所以需要交易
    public void processChange(StationChangeEvent event) {
        LOG.infof("Processing change event for station: %s", event.getStationNo());

        // 1. 使用 Mapper 將 DTO 轉換為 Entity
        StationChangeLog logEntry = changeLogMapper.toEntity(event);
        logEntry.persist();
        LOG.infof("Saved change log to MySQL with ID: %d", logEntry.id);

        // 2. 將最新異動資訊也寫入 Redis 快取 (使用 List)
        try {
            // 從列表左側推入新紀錄
            recentChangesCache.lpush(REDIS_RECENT_CHANGES_KEY, event);
            // 修剪列表，只保留最新的 MAX_RECENT_CHANGES 筆紀錄
            recentChangesCache.ltrim(REDIS_RECENT_CHANGES_KEY, 0, MAX_RECENT_CHANGES - 1);
            LOG.infof("Pushed recent change for station %s to Redis.", event.getStationNo());
        } catch (Exception e) {
            LOG.error("Failed to update Redis cache for recent changes", e);
        }
    }
}
