package org.jay.youbike.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 代表一個站點異動事件的 DTO，將在 Kafka 中傳遞
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationChangeEvent {
    private String stationNo;
    private String stationName;
    private int availableBikesBefore;
    private int availableBikesAfter;
    private int changeDelta; // 變動量 (正數表示增加，負數表示減少)
    private LocalDateTime changeTime;
}
