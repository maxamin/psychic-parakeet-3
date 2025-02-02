package be.separate.sql_injection_target;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SQL_INJECTION_TARGET", schema = "SEPA_RATE")
public class SqlInjectionTarget {

    @Id
    private String id;
    @Column(name = "SOME_VALUE")
    private String someValue;

    public SqlInjectionTarget(){}

    public String getId() {
        return id;
    }

    public String getSomeValue() {
        return someValue;
    }
}
