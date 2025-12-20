package com.rut.booking.search;

import com.rut.booking.models.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String number;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String building;

    @Field(type = FieldType.Integer)
    private Integer floor;

    @Field(type = FieldType.Integer)
    private Integer capacity;

    @Field(type = FieldType.Keyword)
    private String roomType;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String roomTypeDisplayName;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String description;

    @Field(type = FieldType.Boolean)
    private Boolean hasProjector;

    @Field(type = FieldType.Boolean)
    private Boolean hasComputers;

    @Field(type = FieldType.Boolean)
    private Boolean hasWhiteboard;

    @Field(type = FieldType.Double)
    private Double averageRating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String reviews;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String equipmentText;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;
}
