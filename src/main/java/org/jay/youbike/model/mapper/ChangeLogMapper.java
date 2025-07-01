package org.jay.youbike.model.mapper;

import org.jay.youbike.model.dto.StationChangeEvent;
import org.jay.youbike.model.entity.StationChangeLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface ChangeLogMapper {
    // 因為 DTO 和 Entity 的欄位名稱和類型都相同，MapStruct 會自動對應

    /**
     * 將 StationChangeEvent DTO 轉換為 StationChangeLog Entity。
     */
    StationChangeLog toEntity(StationChangeEvent event);

    /**
     * 將 StationChangeLog Entity 轉換為 StationChangeEvent DTO。
     */
    StationChangeEvent toDto(StationChangeLog log);

    /**
     * 將 StationChangeLog Entity 列表轉換為 StationChangeEvent DTO 列表。
     */
    List<StationChangeEvent> toDtoList(List<StationChangeLog> logs);
}
