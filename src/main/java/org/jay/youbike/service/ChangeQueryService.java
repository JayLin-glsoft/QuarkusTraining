package org.jay.youbike.service;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jay.youbike.model.mapper.ChangeLogMapper;
import org.jay.youbike.model.dto.StationChangeEvent;
import org.jay.youbike.model.entity.StationChangeLog;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class ChangeQueryService {

    private static final Logger LOG = Logger.getLogger(ChangeQueryService.class);
    private static final String REDIS_RECENT_CHANGES_KEY = "recent_changes";
    private static final int MAX_RECENT_CHANGES = 10; // 定義常數方便管理

    private final ListCommands<String, StationChangeEvent> recentChangesCache;

    @Inject
    ChangeLogMapper changeLogMapper; // 注入新的 Mapper

    @Inject
    public ChangeQueryService(RedisDataSource redisDataSource) {
        this.recentChangesCache = redisDataSource.list(StationChangeEvent.class);
    }

    public List<StationChangeEvent> getRecentChanges() {
        // 1. 先從 Redis 讀取
        try {
            List<StationChangeEvent> cachedChanges = recentChangesCache.lrange(REDIS_RECENT_CHANGES_KEY, 0, -1);
            if (cachedChanges != null && !cachedChanges.isEmpty()) {
                LOG.info("Cache HIT for recent changes. Returning from Redis.");
                return cachedChanges;
            }
        } catch (Exception e) {
            LOG.error("Failed to read recent changes from Redis", e);
        }

        // 2. 如果 Redis 沒有，則從 MySQL 讀取
        LOG.info("Cache MISS for recent changes. Fetching from MySQL.");
        List<StationChangeLog> logsFromDb =
                StationChangeLog.find("",Sort.by("changeTime").descending())
                        .page(Page.ofSize(MAX_RECENT_CHANGES))
                        .list();
        // 使用 Mapper 將 DB Entity 列表轉換為 DTO 列表
        return changeLogMapper.toDtoList(logsFromDb);
    }
}
