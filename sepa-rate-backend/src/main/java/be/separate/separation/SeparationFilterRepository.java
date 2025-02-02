package be.separate.separation;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
public class SeparationFilterRepository {

    @Inject
    private JdbcTemplate jdbcTemplate;

    public List<SeparationDto> getFilteredSeparationsForUser(String username, String filter) {
        String queryString = "SELECT s.ID as id, s.COUNTRY as country, s.REGION_NAME as separatedRegionName, s.CREATED_USER as createdUser, s.ARGUMENTATION as argumentation " +
                "FROM separation s " +
                "WHERE s.CREATED_USER = '" + username + "'";
        if(filter != null) {
            queryString += "AND s.REGION_NAME LIKE '%" + filter + "%' ";
        }
        List<Map<String, Object>> results = jdbcTemplate.queryForList(queryString);
        return results.stream()
                .map(result -> new SeparationDto()
                        .withId((Integer) result.get("id"))
                        .withCountry((String) result.get("country"))
                        .withSeparatedRegionName((String) result.get("separatedRegionName"))
                        .withCreatedUser((String) result.get("createdUser"))
                        .withArgumentation((String) result.get("argumentation"))
                )
                .collect(Collectors.toList());
    }
}
