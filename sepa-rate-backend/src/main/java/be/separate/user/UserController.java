package be.separate.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static be.separate.user.User.UserBuilder.user;

@RestController
public class UserController {

    @Autowired
    private UserRepository repository;

    @PostMapping(path = "/login")
    public @ResponseBody UserDto login(@RequestBody UserDto userDto, HttpServletRequest request) {
        return repository.findByUsernameAndPassword(userDto.getUsername(), userDto.getPassword())
                .map(this::mapUserToDto)
                .orElse(null);
    }

    @PostMapping(path = "/user")
    public @ResponseBody UserDto createUser(@RequestBody UserDto userDto) {
        return mapUserToDto(repository.save(mapDtoToUser(userDto)));
    }

    private UserDto mapUserToDto(User user) {
        return new UserDto()
                .withCountry(user.getCountry())
                .withPassword(user.getPassword())
                .withUsername(user.getUsername());
    }

    private User mapDtoToUser(UserDto userDto) {
        return user()
                .withUsername(userDto.getUsername())
                .withPassword(userDto.getPassword())
                .withCountry(userDto.getCountry())
                .build();
    }
}
