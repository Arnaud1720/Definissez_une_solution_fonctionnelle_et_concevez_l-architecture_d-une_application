package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Rentals;

import java.util.List;

public interface RentalService {
    List<RentalsDto> findall();
    RentalsDto saveRental(RentalsDto rentalsDto);
    void delete(RentalsDto rentalsDto);
    RentalsDto updateRental(RentalsDto rentalsDto, Integer currentUserId);
    RentalsDto getRentalById(int id);
    List<RentalsDto> findAllByUserId(Integer userId);
    RentalsDto cancelRental(Integer id, Integer currentUserId);
}
