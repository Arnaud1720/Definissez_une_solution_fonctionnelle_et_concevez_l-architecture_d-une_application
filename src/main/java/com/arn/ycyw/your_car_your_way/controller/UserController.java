package com.arn.ycyw.your_car_your_way.controller;
import com.arn.ycyw.your_car_your_way.dto.UserDto;
import com.arn.ycyw.your_car_your_way.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/save")
    public ResponseEntity<Map<String,Object>> save(@Valid @RequestBody UserDto userDto) {
        UserDto savedUser = userService.save(userDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.toString())
                .toUri();

        Map<String,Object> body = new HashMap<>();
        body.put("message", "User created !");
        body.put("user :", savedUser);

        return ResponseEntity
                .created(location)
                .body(body);
    }
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto>  userDtoList = userService.findAll();
        return ResponseEntity.ok(userDtoList);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable("id") int id) {
        UserDto userDto = userService.findById(id);
        return ResponseEntity.ok(userDto);
    }
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") int id) {
        userService.deleteById(id);
    }
}