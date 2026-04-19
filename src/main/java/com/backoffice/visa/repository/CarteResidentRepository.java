package com.backoffice.visa.repository;

import com.backoffice.visa.entity.CarteResident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarteResidentRepository extends JpaRepository<CarteResident, Long> {
    Optional<CarteResident> findByDemandeId(Long demandeId);
}
