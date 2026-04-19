package com.backoffice.visa.repository;

import com.backoffice.visa.entity.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Long> {
    List<StatutDemande> findByDemandeIdOrderByDateChangementStatutDesc(Long demandeId);
}
