package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto save(UserDto userDto);
    List<UserDto> findAll();
    UserDto findById(int id);
    void deleteById(int id);
    UserDto update(UserDto userDto);
}
