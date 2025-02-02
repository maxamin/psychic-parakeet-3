package be.separate.separation;

import javax.persistence.*;

@Entity
@Table(name = "SEPARATION", schema = "SEPA_RATE")
public class Separation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @Column(name = "COUNTRY")
    private String country;
    @Column(name = "REGION_NAME")
    private String separatedRegionName;
    @Column(name = "CREATED_USER")
    private String createdUser;
    @Column(name = "ARGUMENTATION")
    private String argumentation;

    public Separation(SeparationBuilder builder) {
        country=builder.country;
        separatedRegionName=builder.separatedRegionName;
        createdUser=builder.createdUser;
        argumentation=builder.argumentation;
    }

    private Separation(){}

    public int getId() {
        return id;
    }

    public String getCountry() {
        return country;
    }

    public String getSeparatedRegionName() {
        return separatedRegionName;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public String getArgumentation() {
        return argumentation;
    }

    public static class SeparationBuilder {

        private String country;
        private String separatedRegionName;
        private String createdUser;
        private String argumentation;

        private SeparationBuilder(){}

        public static SeparationBuilder separation() {
            return new SeparationBuilder();
        }

        public SeparationBuilder withCountry(String country) {
            this.country = country;
            return this;
        }

        public SeparationBuilder withSeparatedRegionName(String separatedRegionName) {
            this.separatedRegionName = separatedRegionName;
            return this;
        }

        public SeparationBuilder withCreatedUser(String createdUser) {
            this.createdUser = createdUser;
            return this;
        }

        public SeparationBuilder withArgumentation(String argumentation) {
            this.argumentation = argumentation;
            return this;
        }

        public Separation build(){
            return new Separation(this);
        }
    }
}
