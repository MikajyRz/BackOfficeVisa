package com.backoffice.visa.repository;

import com.backoffice.visa.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByDemandeurId(Long demandeurId);
    boolean existsByVisaTransformableNumeroReference(String numeroReference);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM Demande d WHERE CAST(d.id AS string) = :query OR d.visaTransformable.passeport.numeroPasseport = :query ORDER BY d.id DESC")
    List<Demande> searchByIdOrPasseport(@org.springframework.data.repository.query.Param("query") String query);
}
