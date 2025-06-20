package org.jay.youbike.model.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import org.jay.youbike.model.dto.YouBikeStationDTO;
import org.jay.youbike.model.entity.YouBikeStationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@ApplicationScoped // 讓 MapStruct 產生的實作可以被 CDI 注入
@Mapper(componentModel = "cdi") // 讓 Quarkus 可以注入這個 Mapper
public interface YouBikeStationMapper {

    // 將 Entity 轉換為 DTO
    YouBikeStationDTO toDto(YouBikeStationEntity entity);

    // 將 Entity 列表轉換為 DTO 列表
    List<YouBikeStationDTO> toDtoList(List<YouBikeStationEntity> entities);
}
