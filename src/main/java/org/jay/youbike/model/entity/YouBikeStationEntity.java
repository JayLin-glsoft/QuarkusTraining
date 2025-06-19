package org.jay.youbike.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonId;

@Data
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "youbike_stations") // 指定 MongoDB collection 名稱
@JsonIgnoreProperties(ignoreUnknown = true)
public class YouBikeStationEntity extends PanacheMongoEntityBase {
    @BsonId
    @JsonProperty("sno") // 站點代號
    private String stationNo;

    @JsonProperty("sna") // 站點名稱(中文)
    private String stationName;

    @JsonProperty("snaen") // 站點名稱(英文)
    private String stationNameEn;

    @JsonProperty("total") // 站點總停車格數
    private int totalSpaces;

    @JsonProperty("available_rent_bikes") // 目前可借車輛數
    private int availableBikes;

    @JsonProperty("sarea") // 站點區域(中文)
    private String area;

    @JsonProperty("sareaen") // 站點區域(英文)
    private String areaEn;

    @JsonProperty("updateTime") // 資料更新時間 (格式範例: 20231120150514)
    private String updateTime;

    @JsonProperty("latitude") // 緯度
    private double latitude;

    @JsonProperty("longitude") // 經度
    private double longitude;

    @JsonProperty("ar") // 地址(中文)
    private String address;

    @JsonProperty("aren") // 地址(英文)
    private String addressEn;

    @JsonProperty("available_return_bikes") // 目前空位數
    private int availableSpaces;

    @JsonProperty("act") // 全站禁用狀態 (0:禁用; 1:啟用)
    private String active;

    @JsonProperty("lastUpdate") // 最後更新時間 (格式範例: 2023-11-20T15:05:14Z)
    private String lastUpdate; // 使用 ISO 8601 格式的時間戳記
}
