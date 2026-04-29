package com.backoffice.visa.repository;

import com.backoffice.visa.entity.HistoriquePasseportVisa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriquePasseportVisaRepository extends JpaRepository<HistoriquePasseportVisa, Long> {
    List<HistoriquePasseportVisa> findByVisaTransformableId(Long visaId);
}
