package org.jay.entity;

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
    String stationNo;

    @JsonProperty("sna") // 站點名稱(中文)
    String stationName;

    @JsonProperty("snaen") // 站點名稱(英文)
    String stationNameEn;

    @JsonProperty("total") // 站點總停車格數
    int totalSpaces;

    @JsonProperty("available_rent_bikes") // 目前可借車輛數
    int availableBikes;

    @JsonProperty("sarea") // 站點區域(中文)
    String area;

    @JsonProperty("sareaen") // 站點區域(英文)
    String areaEn;

    @JsonProperty("updateTime") // 資料更新時間 (格式範例: 20231120150514)
    String updateTime;

    @JsonProperty("latitude") // 緯度
    double latitude;

    @JsonProperty("longitude") // 經度
    double longitude;

    @JsonProperty("ar") // 地址(中文)
    String address;

    @JsonProperty("aren") // 地址(英文)
    String addressEn;

    @JsonProperty("available_return_bikes") // 目前空位數
    int availableSpaces;

    @JsonProperty("act") // 全站禁用狀態 (0:禁用; 1:啟用)
    String active;
}
