package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.dto.UserDto;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.mapper.UserMapper;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto save(UserDto userDto) {
        LocalDateTime now = LocalDateTime.now();
        userDto.setId(null);
        Users entity = userMapper.toEntity(userDto);

        if (entity.getId() == null) {
            entity.setCreationDate(now);
        }
        boolean emailExist = userRepository.findByEmail(userDto.getEmail()).isPresent();
        if (emailExist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
            entity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Users saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }
    @Override
    public List<UserDto> findAll() {
        List<Users> users = userRepository.findAll();
        return users.stream().map(userMapper::toDto).collect(toList());
    }

    @Override
    public UserDto findById(int id) {
        Users user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return userMapper.toDto(user);
    }

    @Override
    public void deleteById(int id) {
        userRepository.deleteById(id);
    }


}
