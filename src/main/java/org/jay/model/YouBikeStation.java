package main.java.org.jay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouBikeStation(
        @JsonProperty("sno") // 站點代號
        String stationNo,

        @JsonProperty("sna") // 站點名稱(中文)
        String stationName,

        @JsonProperty("tot") // 站點總停車格數
        int totalSpaces,

        @JsonProperty("sbi") // 目前可借車輛數
        int availableBikes,

        @JsonProperty("sarea") // 站點區域(中文)
        String area,

        @JsonProperty("mday") // 資料更新時間 (格式範例: 20231120150514)
        String updateTime,

        @JsonProperty("lat") // 緯度
        double latitude,

        @JsonProperty("lng") // 經度
        double longitude,

        @JsonProperty("ar") // 地址(中文)
        String address,

        @JsonProperty("bemp") // 目前空位數
        int availableSpaces,

        @JsonProperty("act") // 全站禁用狀態 (0:禁用, 1:啟用)
        String active
) {
}