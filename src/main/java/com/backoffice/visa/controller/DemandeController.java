package com.backoffice.visa.controller;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.PieceDemande;
import com.backoffice.visa.entity.PieceDemandeSpecifique;
import com.backoffice.visa.entity.VisaTransformable;
import com.backoffice.visa.repository.PieceDemandeRepository;
import com.backoffice.visa.repository.PieceDemandeSpecifiqueRepository;
import com.backoffice.visa.repository.VisaTransformableRepository;
import com.backoffice.visa.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final VisaTransformableRepository visaTransformableRepository;
    private final PieceDemandeRepository pieceDemandeRepository;
    private final PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository;

    public DemandeController(DemandeService demandeService, VisaTransformableRepository visaTransformableRepository,
                             PieceDemandeRepository pieceDemandeRepository, PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository) {
        this.demandeService = demandeService;
        this.visaTransformableRepository = visaTransformableRepository;
        this.pieceDemandeRepository = pieceDemandeRepository;
        this.pieceDemandeSpecifiqueRepository = pieceDemandeSpecifiqueRepository;
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

    @GetMapping("/{id}/pieces")
    public ResponseEntity<List<PieceDemande>> getPieces(@PathVariable Long id) {
        return ResponseEntity.ok(pieceDemandeRepository.findByDemandeId(id));
    }

    @GetMapping("/{id}/pieces-specifiques")
    public ResponseEntity<List<PieceDemandeSpecifique>> getPiecesSpecifiques(@PathVariable Long id) {
        return ResponseEntity.ok(pieceDemandeSpecifiqueRepository.findByDemandeId(id));
    }

    @GetMapping("/visa-reference/{numero}")
    public ResponseEntity<?> verifierReference(@PathVariable String numero) {
        Optional<VisaTransformable> vt = visaTransformableRepository.findByNumeroReference(numero);
        if (vt.isPresent()) {
            VisaTransformable v = vt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("existe", true);
            response.put("lieu", v.getLieu());
            response.put("dateDebut", v.getDateDebut());
            response.put("dateFin", v.getDateFin());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(Map.of("existe", false));
        }
    }
}
