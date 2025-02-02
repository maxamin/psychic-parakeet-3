package be.separate.separation;

import java.io.Serializable;

public class SeparationDto implements Serializable {

    private int id;
    private String country;
    private String separatedRegionName;
    private String createdUser;
    private String argumentation;

    public int getId() {
        return id;
    }

    public SeparationDto withId(int id) {
        this.id = id;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public SeparationDto withCountry(String country) {
        this.country = country;
        return this;
    }

    public String getSeparatedRegionName() {
        return separatedRegionName;
    }

    public SeparationDto withSeparatedRegionName(String separatedRegionName) {
        this.separatedRegionName = separatedRegionName;
        return this;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public SeparationDto withCreatedUser(String createdUser) {
        this.createdUser = createdUser;
        return this;
    }

    public String getArgumentation() {
        return argumentation;
    }

    public SeparationDto withArgumentation(String argumentation) {
        this.argumentation = argumentation;
        return this;
    }
}
