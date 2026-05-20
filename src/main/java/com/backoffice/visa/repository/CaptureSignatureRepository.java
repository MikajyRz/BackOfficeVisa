package com.backoffice.visa.repository;

import com.backoffice.visa.entity.CaptureSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaptureSignatureRepository extends JpaRepository<CaptureSignature, Long> {
    @Query("SELECT c FROM CaptureSignature c WHERE c.demande.id = :demandeId")
    Optional<CaptureSignature> findByDemandeId(@Param("demandeId") Long demandeId);

    @Query("SELECT COUNT(c) > 0 FROM CaptureSignature c WHERE c.demande.id = :demandeId")
    boolean existsByDemandeId(@Param("demandeId") Long demandeId);
}
