package com.backoffice.visa.repository;

import com.backoffice.visa.entity.Demandeur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeurRepository extends JpaRepository<Demandeur, Long> {
}
