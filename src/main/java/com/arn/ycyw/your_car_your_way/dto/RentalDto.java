package com.arn.ycyw.your_car_your_way.dto;

import com.arn.ycyw.your_car_your_way.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalDto {
    private Integer id;
    private String catCar;
    private LocalDateTime departureStreet;
    private Integer price;
    private String returningRentals;
    private Status status;
    private Integer returnAgencyId;
    private Integer userId;
}
