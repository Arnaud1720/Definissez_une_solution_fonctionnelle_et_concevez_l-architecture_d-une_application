package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.RentalDto;

import java.util.List;

public interface RentalService {
    List<RentalDto> findAllRentals();
    RentalDto saveRental(RentalDto rentalDto);
    void deleteRental(RentalDto rentalDto);
    RentalDto updateRental(RentalDto rentalDto);
    RentalDto getRentalById(RentalDto rentalDto);
}
