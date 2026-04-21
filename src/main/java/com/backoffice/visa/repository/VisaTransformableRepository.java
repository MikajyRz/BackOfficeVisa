package com.backoffice.visa.repository;

import com.backoffice.visa.entity.VisaTransformable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Long> {
    Optional<VisaTransformable> findByNumeroReference(String numeroReference);
}
