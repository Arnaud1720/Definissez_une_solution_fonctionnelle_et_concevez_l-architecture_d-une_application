package com.arn.ycyw.your_car_your_way.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rentals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cat_car")
    private String catCar;

    @Column(name = "departure_street")
    private LocalDateTime departureStreet;

    @Column
    private Integer price;

    @Column(name = "returning_rentals")
    private String returningRentals;

    @Column
    private byte[] status;

    @ManyToOne
    @JoinColumn(name = "return_agency_id", nullable = false)
    private Agency returnAgency;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

}
