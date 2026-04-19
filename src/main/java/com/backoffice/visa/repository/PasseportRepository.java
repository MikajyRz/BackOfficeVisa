package com.backoffice.visa.repository;

import com.backoffice.visa.entity.Passeport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasseportRepository extends JpaRepository<Passeport, Long> {
    Optional<Passeport> findByDemandeurId(Long demandeurId);
}
