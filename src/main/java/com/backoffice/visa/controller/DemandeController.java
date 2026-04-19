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
            response.put("message", "Demande créée — Statut : " + demande.getStatutLibelle());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/scanner")
    public ResponseEntity<?> scannerDossier(@PathVariable Long id) {
        try {
            Demande demande = demandeService.scannerDossier(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Dossier scanné — le dossier ne peut plus être modifié");
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
