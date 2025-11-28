package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.mapper.RentalsMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RantalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalsMapper rentalsMapper;
    private final UserRepository userRepository;
    private final AgencyRepository agencyRepository;
    public RantalServiceImpl(RentalRepository rentalRepository, RentalsMapper rentalsMapper, UserRepository userRepository, AgencyRepository agencyRepository) {
        this.rentalRepository = rentalRepository;
        this.rentalsMapper = rentalsMapper;
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
    }


    @Override
    public List<RentalsDto> findall() {
        return List.of();
    }

    /**
     *
     * @param rentalsDto
     * @return
     *
     */
    @Override
    public RentalsDto saveRental(RentalsDto rentalsDto) {
        // Pour être sûr qu’on fait une création
        rentalsDto.setId(null);

        // 1. Récupérer le User
        Users user = userRepository.findById(rentalsDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Récupérer l’Agency (il te faut AgencyRepository)
        Agency agency = agencyRepository.findById(rentalsDto.getReturnAgencyId())
                .orElseThrow(() -> new RuntimeException("Agency not found"));

        // 3. Mapper le reste du DTO vers l’entité
        Rentals rentals = rentalsMapper.toEntity(rentalsDto);

        // 4. Rattacher les relations
        rentals.setStatus(Status.NONE);
        rentals.setUser(user);
        rentals.setReturnAgency(agency);

        // 5. Sauvegarder
        Rentals saved = rentalRepository.save(rentals);

        // 6. Retourner un DTO
        return rentalsMapper.toDto(saved);
    }

    @Override
    public RentalsDto updateRental(RentalsDto rentalsDto) {
        return null;
    }

    @Override
    public RentalsDto getRentalById(int id) {
        Rentals rentals = rentalRepository.findById(id).orElseThrow(() -> new RuntimeException("Rental not found"));
        return rentalsMapper.toDto(rentals);
    }

    @Override
    public void delete(RentalsDto rentalsDto) {
        rentalRepository.delete(rentalsMapper.toEntity(rentalsDto));
    }
}
