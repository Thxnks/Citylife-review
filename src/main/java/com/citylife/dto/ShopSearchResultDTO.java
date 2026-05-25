package com.citylife.dto;

import com.citylife.document.ShopDocument;
import lombok.Data;

@Data
public class ShopSearchResultDTO {
    private Long id;
    private String name;
    private String highlightedName;
    private Long typeId;
    private String images;
    private String area;
    private String highlightedArea;
    private String address;
    private String highlightedAddress;
    private Double x;
    private Double y;
    private Long avgPrice;
    private Integer sold;
    private Integer comments;
    private Integer score;
    private String openHours;
    private Double distance;

    public static ShopSearchResultDTO from(ShopDocument document) {
        ShopSearchResultDTO dto = new ShopSearchResultDTO();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setTypeId(document.getTypeId());
        dto.setImages(document.getImages());
        dto.setArea(document.getArea());
        dto.setAddress(document.getAddress());
        dto.setX(document.getX());
        dto.setY(document.getY());
        dto.setAvgPrice(document.getAvgPrice());
        dto.setSold(document.getSold());
        dto.setComments(document.getComments());
        dto.setScore(document.getScore());
        dto.setOpenHours(document.getOpenHours());
        return dto;
    }
}
