package com.arn.ycyw.your_car_your_way.dto;

import com.arn.ycyw.your_car_your_way.entity.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalsDto {
    @Schema(description = "ID généré par le serveur", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
    private String catCar;
    private LocalDateTime departureStreet;
    private Integer price;
    private String returningRentals;
    private Status status;
    private Integer returnAgencyId;
    private Integer userId;
    private LocalDateTime startDate;
    private int RefundPercentage;

}
