package com.backoffice.visa.repository;

import com.backoffice.visa.entity.TypeDemande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeDemandeRepository extends JpaRepository<TypeDemande, Long> {
	Optional<TypeDemande> findByLibelleIgnoreCase(String libelle);
}
