package com.backoffice.visa.controller;

import com.backoffice.visa.repository.*;
import com.backoffice.visa.entity.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/references")
public class ReferenceController {

    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final TypePieceCommuneRepository typePieceCommuneRepository;
    private final TypePieceSpecifiqueRepository typePieceSpecifiqueRepository;

    public ReferenceController(
            NationaliteRepository nationaliteRepository,
            SituationFamilialeRepository situationFamilialeRepository,
            TypeVisaRepository typeVisaRepository,
            TypeDemandeRepository typeDemandeRepository,
            TypePieceCommuneRepository typePieceCommuneRepository,
            TypePieceSpecifiqueRepository typePieceSpecifiqueRepository) {
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.typePieceCommuneRepository = typePieceCommuneRepository;
        this.typePieceSpecifiqueRepository = typePieceSpecifiqueRepository;
    }

    @GetMapping("/nationalites")
    public ResponseEntity<List<Nationalite>> getNationalites() {
        return ResponseEntity.ok(nationaliteRepository.findAll());
    }

    @GetMapping("/situations-familiales")
    public ResponseEntity<List<SituationFamiliale>> getSituationsFamiliales() {
        return ResponseEntity.ok(situationFamilialeRepository.findAll());
    }

    @GetMapping("/types-visa")
    public ResponseEntity<List<TypeVisa>> getTypesVisa() {
        return ResponseEntity.ok(typeVisaRepository.findAll());
    }

    @GetMapping("/types-demande")
    public ResponseEntity<List<TypeDemande>> getTypesDemande() {
        return ResponseEntity.ok(typeDemandeRepository.findAll());
    }

    @GetMapping("/pieces-communes")
    public ResponseEntity<List<TypePieceCommune>> getPiecesCommunes() {
        return ResponseEntity.ok(typePieceCommuneRepository.findAll());
    }

    @GetMapping("/pieces-specifiques/{typeVisaId}")
    public ResponseEntity<List<TypePieceSpecifique>> getPiecesSpecifiques(@PathVariable Long typeVisaId) {
        return ResponseEntity.ok(typePieceSpecifiqueRepository.findByTypeVisaId(typeVisaId));
    }
}
