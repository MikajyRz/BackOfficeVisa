package com.backoffice.visa.repository;

import com.backoffice.visa.entity.Visa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VisaRepository extends JpaRepository<Visa, Long> {
    Optional<Visa> findByDemandeId(Long demandeId);
}
