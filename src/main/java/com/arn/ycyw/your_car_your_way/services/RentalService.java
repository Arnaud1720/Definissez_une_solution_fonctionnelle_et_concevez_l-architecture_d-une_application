package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;

import java.util.List;

public interface RentalService {
    List<RentalsDto> findall();
    RentalsDto saveRental(RentalsDto rentalsDto);
    void delete(RentalsDto rentalsDto);
    RentalsDto updateRental(RentalsDto rentalsDto);
    RentalsDto getRentalById(int id);
}
