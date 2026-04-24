package com.backoffice.visa.repository;

import com.backoffice.visa.entity.DemandeTransfert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DemandeTransfertRepository extends JpaRepository<DemandeTransfert, Long> {
    Optional<DemandeTransfert> findByDemandeId(Long demandeId);
}
