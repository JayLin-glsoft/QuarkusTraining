package org.jay.youbike.model.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 用於儲存到 MySQL 的異動歷史紀錄 Entity
@Entity
@Table(name = "station_change_logs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class StationChangeLog extends PanacheEntity {
    public String stationNo;
    public String stationName;
    public int availableBikesBefore;
    public int availableBikesAfter;
    public int changeDelta;
    public LocalDateTime changeTime;
}
