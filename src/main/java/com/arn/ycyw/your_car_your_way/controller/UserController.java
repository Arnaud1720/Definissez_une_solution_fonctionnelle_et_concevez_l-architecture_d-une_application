package com.arn.ycyw.your_car_your_way.controller;
import com.arn.ycyw.your_car_your_way.dto.UserDto;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.security.UsersDetailsAdapter;
import com.arn.ycyw.your_car_your_way.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UsersDetailsAdapter principal) {
        //On recupere l'entité Users depuis l'adapter
        Integer id = principal.getUser().getId();
        // on réutilise le service métier qui retoune un UserDto
        UserDto userDto = userService.findById(id);
        //et on enveloppe dans un ResponseEntity
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDto> update( @AuthenticationPrincipal UsersDetailsAdapter principal,
                                           @RequestBody UserDto userDto) {
        Integer currentUserId = principal.getUser().getId();
        userDto.setId(currentUserId);
        UserDto updated = userService.update(userDto);
        return ResponseEntity.ok(updated);

    }


}