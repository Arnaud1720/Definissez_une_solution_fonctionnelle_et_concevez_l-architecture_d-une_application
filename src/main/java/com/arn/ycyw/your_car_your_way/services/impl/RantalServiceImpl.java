package com.arn.ycyw.your_car_your_way.services.impl;
import com.arn.ycyw.your_car_your_way.dto.AgencyDto;
import com.arn.ycyw.your_car_your_way.dto.RentalResponseDto;
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

    public RantalServiceImpl(RentalRepository rentalRepository, RentalsMapper rentalsMapper,
                             UserRepository userRepository, AgencyRepository agencyRepository) {
        this.rentalRepository = rentalRepository;
        this.rentalsMapper = rentalsMapper;
        this.userRepository = userRepository;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public List<RentalsDto> findall() {
        return rentalRepository.findAll().stream()
                .map(rentalsMapper::toDto)
                .toList();
    }

    @Override
    public RentalsDto saveRental(RentalsDto rentalsDto) {
        // Pour être sûr qu'on fait une création
        rentalsDto.setId(null);

        // 1. Récupérer le User
        Users user = userRepository.findById(rentalsDto.getUserId())
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        // 2. Récupérer l'agence de départ
        Agency departureAgency = agencyRepository.findById(rentalsDto.getDepartureAgencyId())
                .orElseThrow(() -> new BusinessException("Agence de départ non trouvée"));

        // 3. Récupérer l'agence de retour
        Agency returnAgency = agencyRepository.findById(rentalsDto.getReturnAgencyId())
                .orElseThrow(() -> new BusinessException("Agence de retour non trouvée"));

        // 4. Mapper le reste du DTO vers l'entité
        Rentals rentals = rentalsMapper.toEntity(rentalsDto);

        // 5. Rattacher les relations
        rentals.setStatus(Status.BOOKED);
        rentals.setUser(user);
        rentals.setDepartureAgency(departureAgency);
        rentals.setReturnAgency(returnAgency);

        // 6. Sauvegarder
        Rentals saved = rentalRepository.save(rentals);

        // 7. Retourner un DTO
        return rentalsMapper.toDto(saved);
    }

    @Override
    public RentalsDto getRentalById(int id) {
        Rentals rentals = rentalRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location non trouvée"));
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
        rental.setRefundPercentage(refund);

        return rentalsMapper.toDto(rentalRepository.save(rental));
    }

    /**
     * Récupère toutes les réservations d'un utilisateur avec les agences complètes
     * C'est cette méthode que le frontend utilise pour afficher les réservations
     */
    @Override
    public List<RentalResponseDto> findAllByUserIdWithAgencies(Integer userId) {
        List<Rentals> rentals = rentalRepository.findAllByUser_Id(userId);
        return rentals.stream()
                .map(this::toResponseDto)
                .toList();
    }

    /**
     * Annule une réservation et retourne la réponse avec les agences complètes
     */
    @Override
    public RentalResponseDto cancelRentalWithAgencies(Integer id, Integer currentUserId) {
        Rentals rental = rentalRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Location introuvable"));

        if (!rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Vous ne pouvez annuler que vos propres réservations");
        }

        int refund = computeRefundPercentage(rental);
        rental.setStatus(Status.CANCELLED);
        rental.setRefundPercentage(refund);

        Rentals saved = rentalRepository.save(rental);
        return toResponseDto(saved);
    }

    /**
     * Convertit une entité Rentals en RentalResponseDto avec les agences complètes
     */
    private RentalResponseDto toResponseDto(Rentals rental) {
        RentalResponseDto dto = new RentalResponseDto();
        dto.setId(rental.getId());
        dto.setCatCar(rental.getCatCar());
        dto.setStartDate(rental.getStartDate());
        dto.setEndDate(rental.getEndDate());
        dto.setPrice(rental.getPrice());
        dto.setStatus(rental.getStatus());
        dto.setRefundPercentage(rental.getRefundPercentage());

        // Mapper les agences complètes
        if (rental.getDepartureAgency() != null) {
            dto.setDepartureAgency(toAgencyDto(rental.getDepartureAgency()));
        }
        if (rental.getReturnAgency() != null) {
            dto.setReturnAgency(toAgencyDto(rental.getReturnAgency()));
        }

        return dto;
    }

    /**
     * Convertit une Agency en AgencyDto
     */
    private AgencyDto toAgencyDto(Agency agency) {
        AgencyDto dto = new AgencyDto();
        dto.setId(agency.getId());
        dto.setName(agency.getName());
        dto.setAddress(agency.getAddress());
        dto.setCity(agency.getCity());
        dto.setCountry(agency.getCountry());
        dto.setPostalCode(agency.getPostalCode());
        dto.setPhone(agency.getPhone());
        dto.setEmail(agency.getEmail());
        return dto;
    }

    @Override
    public void delete(RentalsDto rentalsDto) {
        rentalRepository.delete(rentalsMapper.toEntity(rentalsDto));
    }

    @Override
    public RentalsDto updateRental(RentalsDto rentalsDto, Integer currentUserId) {
        // 1. On récupère la rental à partir de son ID
        Rentals rental = rentalRepository.findById(rentalsDto.getId())
                .orElseThrow(() -> new BusinessException("Location non trouvée"));

        // vérifier que c'est bien la réservation du user courant
        if (!rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres réservations");
        }
        checkCanModify(rental);

        // 3. Mettre à jour les champs modifiables
        rental.setCatCar(rentalsDto.getCatCar());
        rental.setStartDate(rentalsDto.getStartDate());
        rental.setEndDate(rentalsDto.getEndDate());
        rental.setPrice(rentalsDto.getPrice());

        // 4. Gérer l'agence de départ si l'ID est fourni
        if (rentalsDto.getDepartureAgencyId() != null) {
            Agency departureAgency = agencyRepository.findById(rentalsDto.getDepartureAgencyId())
                    .orElseThrow(() -> new BusinessException("Agence de départ non trouvée"));
            rental.setDepartureAgency(departureAgency);
        }

        // 5. Gérer l'agence de retour si l'ID est fourni
        if (rentalsDto.getReturnAgencyId() != null) {
            Agency returnAgency = agencyRepository.findById(rentalsDto.getReturnAgencyId())
                    .orElseThrow(() -> new BusinessException("Agence de retour non trouvée"));
            rental.setReturnAgency(returnAgency);
        }

        Rentals saved = rentalRepository.save(rental);
        return rentalsMapper.toDto(saved);
    }

    private void checkCanModify(Rentals rental) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = rental.getStartDate();

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
            return 100; // 100 % remboursé
        }
    }


}
