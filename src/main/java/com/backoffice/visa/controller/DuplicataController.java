package com.backoffice.visa.controller;

import com.backoffice.visa.dto.DuplicataFormDTO;
import com.backoffice.visa.entity.CarteResident;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.DemandeDuplicata;
import com.backoffice.visa.service.DuplicataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cartes-resident/duplicata")
public class DuplicataController {

    private final DuplicataService duplicataService;

    public DuplicataController(DuplicataService duplicataService) {
        this.duplicataService = duplicataService;
    }

    @GetMapping("/recherche")
    public ResponseEntity<?> rechercher(@RequestParam String critere) {
        Optional<Demande> demande = duplicataService.rechercherDemandeEligible(critere);
        if (demande.isPresent()) {
            return ResponseEntity.ok(demande.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> creer(@RequestBody DuplicataFormDTO form) {
        try {
            DemandeDuplicata duplicata = duplicataService.creerDemandeDuplicata(form);
            return ResponseEntity.ok(duplicata);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> valider(@PathVariable Long id) {
        try {
            duplicataService.validerDuplicata(id);
            return ResponseEntity.ok(Map.of("message", "Demande de duplicata validée"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeter(@PathVariable Long id) {
        try {
            duplicataService.rejeterDuplicata(id);
            return ResponseEntity.ok(Map.of("message", "Demande de duplicata rejetée"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/emettre")
    public ResponseEntity<?> emettre(@PathVariable Long id) {
        try {
            CarteResident carte = duplicataService.emettreDuplicata(id);
            Map<String, Object> response = new HashMap<>();
            response.put("reference", carte.getReference());
            response.put("dateDebut", carte.getDateDebut());
            response.put("dateFin", carte.getDateFin());
            response.put("message", "Duplicata émis avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
