package be.separate.separation;

import be.separate.sql_injection_target.SqlInjectionTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface SeparationRepository extends JpaRepository<Separation, Integer> {

    List<Separation> findByCreatedUser(String userId);
}
