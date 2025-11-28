package com.arn.ycyw.your_car_your_way.mapper;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalsMapper {
    RentalsDto toDto(Rentals rentals);
    Rentals toEntity(RentalsDto dto);

}
