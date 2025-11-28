package com.arn.ycyw.your_car_your_way.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "agencys")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Agency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String city;

    @OneToMany(mappedBy = "returnAgency")
    private List<Rentals> rentals = new ArrayList<>();
}
