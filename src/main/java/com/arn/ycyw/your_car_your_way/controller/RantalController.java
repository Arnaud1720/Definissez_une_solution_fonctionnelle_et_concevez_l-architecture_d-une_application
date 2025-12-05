package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.security.UsersDetailsAdapter;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/rantals")
public class RantalController {
    private final RentalService rentalService;

    public RantalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String,Object>> saveRantal(@Valid @RequestBody RentalsDto rentalsDto) {
        RentalsDto savedRentalsDto = rentalService.saveRental(rentalsDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedRentalsDto.toString())
                .toUri();
        Map<String,Object> body = new HashMap<>();
        body.put("message", "User created !");
        body.put("user :", savedRentalsDto);

        return ResponseEntity
                .created(location)
                .body(body);

    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalsDto> getRantalById(@PathVariable("id") int id) {
        RentalsDto rentalsDto = rentalService.getRentalById(id);
        return ResponseEntity.ok(rentalsDto);
    }

    @PutMapping("/rentals/{id}")
    public ResponseEntity<RentalsDto> updateRental(
            @PathVariable Integer id,
            @RequestBody RentalsDto dto,
            @AuthenticationPrincipal UsersDetailsAdapter principal) {
        // on force l'id depuis l'URL
        dto.setId(id);

        Integer currentUserId = principal.getUser().getId();

        RentalsDto updated = rentalService.updateRental(dto, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/rentals")
    public ResponseEntity<?> getMyRentals(
            @AuthenticationPrincipal UsersDetailsAdapter principal) {

        Integer currentUserId = principal.getUser().getId();
        return ResponseEntity.ok(rentalService.findAllByUserId(currentUserId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RentalsDto> cancelRental(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsersDetailsAdapter principal) {

        Integer currentUserId = principal.getUser().getId();
        RentalsDto cancelled = rentalService.cancelRental(id, currentUserId);
        return ResponseEntity.ok(cancelled);
    }
}
