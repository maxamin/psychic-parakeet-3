package be.separate.user;

import java.io.Serializable;

public class UserDto implements Serializable {

    private String country;
    private String username;
    private String password;

    public String getCountry() {
        return country;
    }

    public UserDto withCountry(String country) {
        this.country = country;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDto withUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserDto withPassword(String password) {
        this.password = password;
        return this;
    }
}
