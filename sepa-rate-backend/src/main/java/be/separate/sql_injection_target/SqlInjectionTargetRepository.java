package be.separate.sql_injection_target;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SqlInjectionTargetRepository extends JpaRepository<SqlInjectionTarget, String> {

}
