package be.separate.separation;

import java.io.Serializable;

public class FilterDto implements Serializable {

    private String filter;

    public String getFilter() {
        return filter;
    }

    public FilterDto withFilter(String filter) {
        this.filter = filter;
        return this;
    }
}
