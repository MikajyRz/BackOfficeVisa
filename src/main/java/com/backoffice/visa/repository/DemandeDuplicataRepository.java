package com.backoffice.visa.repository;

import com.backoffice.visa.entity.DemandeDuplicata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DemandeDuplicataRepository extends JpaRepository<DemandeDuplicata, Long> {
    Optional<DemandeDuplicata> findByDemandeId(Long demandeId);
}
