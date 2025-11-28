package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.RentalDto;
import com.arn.ycyw.your_car_your_way.mapper.RentalsMapper;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RantalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalsMapper rentalsMapper;


    public RantalServiceImpl(RentalRepository rentalRepository, RentalsMapper rentalsMapper) {
        this.rentalRepository = rentalRepository;
        this.rentalsMapper = rentalsMapper;
    }


    @Override
    public List<RentalDto> findAllRentals() {
        return List.of();
    }

    @Override
    public RentalDto saveRental(RentalDto rentalDto) {
        return null;
    }

    @Override
    public RentalDto updateRental(RentalDto rentalDto) {
        return null;
    }

    @Override
    public RentalDto getRentalById(RentalDto rentalDto) {
        return null;
    }

    @Override
    public void deleteRental(RentalDto rentalDto) {

    }
}
