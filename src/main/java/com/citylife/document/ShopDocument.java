package com.citylife.document;

import com.citylife.entity.Shop;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(indexName = "citylife_shops")
public class ShopDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Long)
    private Long typeId;

    @Field(type = FieldType.Keyword, index = false)
    private String images;

    @Field(type = FieldType.Text)
    private String area;

    @Field(type = FieldType.Text)
    private String address;

    @Field(type = FieldType.Double)
    private Double x;

    @Field(type = FieldType.Double)
    private Double y;

    @GeoPointField
    private GeoPoint location;

    @Field(type = FieldType.Long)
    private Long avgPrice;

    @Field(type = FieldType.Integer)
    private Integer sold;

    @Field(type = FieldType.Integer)
    private Integer comments;

    @Field(type = FieldType.Integer)
    private Integer score;

    @Field(type = FieldType.Keyword)
    private String openHours;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updateTime;

    public ShopDocument(Shop shop) {
        this.id = shop.getId();
        this.name = shop.getName();
        this.typeId = shop.getTypeId();
        this.images = shop.getImages();
        this.area = shop.getArea();
        this.address = shop.getAddress();
        this.x = shop.getX();
        this.y = shop.getY();
        if (shop.getX() != null && shop.getY() != null) {
            this.location = new GeoPoint(shop.getY(), shop.getX());
        }
        this.avgPrice = shop.getAvgPrice();
        this.sold = shop.getSold();
        this.comments = shop.getComments();
        this.score = shop.getScore();
        this.openHours = shop.getOpenHours();
        this.createTime = shop.getCreateTime();
        this.updateTime = shop.getUpdateTime();
    }
}
