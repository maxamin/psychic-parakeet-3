package be.separate.user;

import javax.persistence.*;

@Entity
@Table(name = "user", schema = "SEPA_RATE")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "COUNTRY")
    private String country;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "PASSWORD")
    private String password;

    private User(){}

    private User(UserBuilder builder) {
        country=builder.country;
        username=builder.username;
        password=builder.password;
    }

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static class UserBuilder {
        private String country;
        private String username;
        private String password;

        private UserBuilder(){}

        public static UserBuilder user(){
            return new UserBuilder();
        }

        public User build() {
            return new User(this);
        }

        public UserBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }
    }

}
