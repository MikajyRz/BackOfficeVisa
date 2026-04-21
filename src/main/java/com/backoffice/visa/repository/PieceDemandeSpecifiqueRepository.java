package com.backoffice.visa.repository;

import com.backoffice.visa.entity.PieceDemandeSpecifique;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PieceDemandeSpecifiqueRepository extends JpaRepository<PieceDemandeSpecifique, Long> {
    List<PieceDemandeSpecifique> findByDemandeId(Long demandeId);
}
