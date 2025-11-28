package com.arn.ycyw.your_car_your_way.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
    @OneToMany(mappedBy = "user")
    private List<Rentals> rentals = new ArrayList<>();
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "username")
    private String username;

}

