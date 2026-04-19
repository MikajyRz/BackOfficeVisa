package com.backoffice.visa.repository;

import com.backoffice.visa.entity.TypePieceSpecifique;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TypePieceSpecifiqueRepository extends JpaRepository<TypePieceSpecifique, Long> {
    List<TypePieceSpecifique> findByTypeVisaId(Long typeVisaId);
}
