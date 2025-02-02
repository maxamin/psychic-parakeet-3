package be.separate.sql_injection_target;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SqlInjectionTargetController {

    @Autowired
    private SqlInjectionTargetRepository sqlInjectionTargetRepository;


    @GetMapping(path = "/sitarget")
    public @ResponseBody
    List<SqlInjectionTargetDto> getAllSqlInjectionTargets() {
        return sqlInjectionTargetRepository.findAll().stream()
                .map(sqlInjectionTarget -> new SqlInjectionTargetDto().withValue(sqlInjectionTarget.getSomeValue()).withId(sqlInjectionTarget.getId()))
                .collect(Collectors.toList());
    }
}
