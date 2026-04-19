package com.backoffice.visa.controller;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @PostMapping
    public ResponseEntity<?> creerDemande(@RequestBody DemandeFormDTO form) {
        try {
            Demande demande = demandeService.creerDemande(form);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Demande créée avec succès (brouillon)");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/soumettre")
    public ResponseEntity<?> soumettreDemande(@PathVariable Long id) {
        try {
            Demande demande = demandeService.soumettreDemande(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Demande soumise avec succès");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerDemande(@PathVariable Long id) {
        try {
            Demande demande = demandeService.validerDemande(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Demande validée — Visa et Carte de résident générés");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterDemande(@PathVariable Long id) {
        try {
            Demande demande = demandeService.rejeterDemande(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Demande rejetée");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Demande>> getAllDemandes() {
        return ResponseEntity.ok(demandeService.getAllDemandes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDemande(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(demandeService.getDemandeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
