package main.kotlin.org.jay.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class YouBikeStation(
    @JsonProperty("sno") // 站點代號
    val stationNo: String?,

    @JsonProperty("sna") // 站點名稱(中文)
    val stationName: String?,

    @JsonProperty("tot") // 站點總停車格數
    val totalSpaces: Int?,

    @JsonProperty("sbi") // 目前可借車輛數
    val availableBikes: Int?,

    @JsonProperty("sarea") // 站點區域(中文)
    val area: String?,

    @JsonProperty("mday") // 資料更新時間 (格式範例: 20231120150514)
    val updateTime: String?,

    @JsonProperty("lat") // 緯度
    val latitude: Double?,

    @JsonProperty("lng") // 經度
    val longitude: Double?,

    @JsonProperty("ar") // 地址(中文)
    val address: String?,

    @JsonProperty("bemp") // 目前空位數
    val availableSpaces: Int?,

    @JsonProperty("act") // 全站禁用狀態 (0:禁用, 1:啟用)
    val active: String?
)
