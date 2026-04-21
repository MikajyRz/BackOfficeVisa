package com.backoffice.visa.repository;

import com.backoffice.visa.entity.PieceDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PieceDemandeRepository extends JpaRepository<PieceDemande, Long> {
    List<PieceDemande> findByDemandeId(Long demandeId);
}
