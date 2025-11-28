package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.dto.RentalsDto;
import com.arn.ycyw.your_car_your_way.services.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
}
