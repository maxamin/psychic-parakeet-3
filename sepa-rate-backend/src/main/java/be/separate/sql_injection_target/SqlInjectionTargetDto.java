package be.separate.sql_injection_target;

import java.io.Serializable;

public class SqlInjectionTargetDto implements Serializable {

    public String id;
    public String value;

    public SqlInjectionTargetDto(){}

    public String getId() {
        return id;
    }

    public SqlInjectionTargetDto withId(String id) {
        this.id = id;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SqlInjectionTargetDto withValue(String value) {
        this.value = value;
        return this;
    }
}
