package org.jay.youbike.model.dto;

import lombok.Data;

@Data
// DTO: 只包含需要暴露給 API 客戶端的欄位
public class YouBikeStationDTO {
    private String stationNo;
    private String stationName;
    private int totalSpaces;
    private int availableBikes;
    private String area;
    private String updateTime;
    private double latitude;
    private double longitude;
    private String address;
    private int availableSpaces;
    private String active;
}
