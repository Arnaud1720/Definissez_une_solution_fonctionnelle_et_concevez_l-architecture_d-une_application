package com.arn.ycyw.your_car_your_way.services.impl;
import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.exception.BusinessException;
import com.arn.ycyw.your_car_your_way.mapper.RentalsMapper;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
        rentals.setStatus(Status.BOOKED);
        rentals.setUser(user);
        rentals.setReturnAgency(agency);

        // 5. Sauvegarder
        Rentals saved = rentalRepository.save(rentals);

        // 6. Retourner un DTO
        return rentalsMapper.toDto(saved);
    }


    @Override
    public RentalsDto getRentalById(int id) {
        Rentals rentals = rentalRepository.findById(id).orElseThrow(() -> new RuntimeException("Rental not found"));
        return rentalsMapper.toDto(rentals);
    }

    @Override
    public List<RentalsDto> findAllByUserId(Integer userId) {
        List<Rentals> rentals = rentalRepository.findAllByUser_Id(userId);
        return rentals.stream()
                .map(rentalsMapper::toDto)
                .toList();
    }

    @Override
    public RentalsDto cancelRental(Integer id, Integer currentUserId) {
        Rentals rental = rentalRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (!rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Vous ne pouvez annuler que vos propres réservations");
        }

        int refund = computeRefundPercentage(rental);
        rental.setStatus(Status.CANCELLED);
        rental.setRefundPercentage(refund); // champ à ajouter si tu veux le stocker

        return rentalsMapper.toDto(rentalRepository.save(rental));
    }

    @Override
    public void delete(RentalsDto rentalsDto) {
        rentalRepository.delete(rentalsMapper.toEntity(rentalsDto));
    }

    @Override
    public RentalsDto updateRental(RentalsDto rentalsDto, Integer currentUserId) {
        // 1. On récupère la rental à partir de son ID
        Rentals rental = rentalRepository.findById(rentalsDto.getId())
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // vérifier que c'est bien la réservation du user courant
        if (!rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres réservations");
        }
        checkCanModify(rental);

        // 3. Mettre à jour les champs modifiables
        rental.setCatCar(rentalsDto.getCatCar());
        rental.setDepartureStreet(rentalsDto.getDepartureStreet());
        rental.setPrice(rentalsDto.getPrice());
        rental.setReturningRentals(rentalsDto.getReturningRentals());
        rental.setStatus(rentalsDto.getStatus());

        // 4. Gérer l’agence de retour si l’ID est fourni
        if (rentalsDto.getReturnAgencyId() != null) {
            Agency agency = agencyRepository.findById(rentalsDto.getReturnAgencyId())
                    .orElseThrow(() -> new RuntimeException("Return agency not found"));
            rental.setReturnAgency(agency);
        }

        Rentals saved = rentalRepository.save(rental);
        return rentalsMapper.toDto(saved);
    }

    private void checkCanModify(Rentals rental) {
        LocalDateTime now = LocalDateTime.now(); // ou avec un ZoneId si tu veux être propre
        LocalDateTime start = rental.getStartDate(); // adapte au nom de ton champ

        if (now.isAfter(start.minusHours(48))) {
            throw new BusinessException(
                    "La réservation ne peut plus être modifiée moins de 48h avant le début."
            );
        }
    }

    private int computeRefundPercentage(Rentals rental) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = rental.getStartDate();

        long daysBeforeStart = ChronoUnit.DAYS.between(now.toLocalDate(), start.toLocalDate());

        if (daysBeforeStart < 7) {
            return 25; // 25 % remboursé
        } else {
            return 100; // 100 % (ou autre règle que tu définis)
        }
    }
}