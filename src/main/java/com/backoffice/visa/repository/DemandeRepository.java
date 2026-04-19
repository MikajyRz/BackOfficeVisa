package com.backoffice.visa.repository;

import com.backoffice.visa.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByDemandeurId(Long demandeurId);
}
