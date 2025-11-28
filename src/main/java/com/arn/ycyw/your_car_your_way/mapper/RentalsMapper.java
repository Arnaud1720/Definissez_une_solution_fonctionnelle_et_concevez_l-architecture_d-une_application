package com.arn.ycyw.your_car_your_way.mapper;

import com.arn.ycyw.your_car_your_way.dto.RentalDto;
import com.arn.ycyw.your_car_your_way.dto.UserDto;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RentalsMapper {
    RentalDto toDto(Rentals rentals);
    Rentals toEntity(RentalDto dto);

}
