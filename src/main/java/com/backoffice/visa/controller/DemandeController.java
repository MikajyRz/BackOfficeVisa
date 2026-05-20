package com.backoffice.visa.controller;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.Demandeur;
import com.backoffice.visa.entity.PieceDemande;
import com.backoffice.visa.entity.PieceDemandeSpecifique;
import com.backoffice.visa.entity.VisaTransformable;
import com.backoffice.visa.repository.DemandeurRepository;
import com.backoffice.visa.repository.PieceDemandeRepository;
import com.backoffice.visa.repository.PieceDemandeSpecifiqueRepository;
import com.backoffice.visa.repository.VisaTransformableRepository;
import com.backoffice.visa.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private final DemandeurRepository demandeurRepository;

    public DemandeController(DemandeService demandeService, VisaTransformableRepository visaTransformableRepository,
                             PieceDemandeRepository pieceDemandeRepository, PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository,
                             DemandeurRepository demandeurRepository) {
        this.demandeService = demandeService;
        this.visaTransformableRepository = visaTransformableRepository;
        this.pieceDemandeRepository = pieceDemandeRepository;
        this.pieceDemandeSpecifiqueRepository = pieceDemandeSpecifiqueRepository;
        this.demandeurRepository = demandeurRepository;
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

    @PostMapping("/termines")
    public ResponseEntity<?> creerDossierTermine(@RequestBody DemandeFormDTO form) {
        try {
            Demande demande = demandeService.creerDossierTermine(form);
            return ResponseEntity.ok(demande);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/scanner")
    public ResponseEntity<?> scannerDossier(@PathVariable("id") Long id) {
        try {
            Demande demande = demandeService.scannerDossier(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Le scan du dossier est terminé.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<?> terminerDossier(@PathVariable("id") Long id) {
        try {
            Demande demande = demandeService.terminerDossier(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", demande.getId());
            response.put("statut", demande.getStatutLibelle());
            response.put("message", "Dossier terminé — Carte de résident générée.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Demande>> searchDemandes(@RequestParam(value = "q", required = false) String query) {
        return ResponseEntity.ok(demandeService.searchDemandes(query));
    }

    @GetMapping("/{id}/historiques")
    public ResponseEntity<?> getHistoriques(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(demandeService.getHistorique(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Demande>> getAllDemandes() {
        return ResponseEntity.ok(demandeService.getAllDemandes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDemande(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(demandeService.getDemandeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pieces")
    public ResponseEntity<List<PieceDemande>> getPieces(@PathVariable("id") Long id) {
        demandeService.initialiserPiecesPourUpload(id);
        return ResponseEntity.ok(pieceDemandeRepository.findByDemandeId(id));
    }

    @GetMapping("/{id}/pieces-specifiques")
    public ResponseEntity<List<PieceDemandeSpecifique>> getPiecesSpecifiques(@PathVariable("id") Long id) {
        demandeService.initialiserPiecesPourUpload(id);
        return ResponseEntity.ok(pieceDemandeSpecifiqueRepository.findByDemandeId(id));
    }

    @GetMapping("/verifier-passeport/{numero}")
    public ResponseEntity<Map<String, Boolean>> verifierNumeroPasseport(@PathVariable("numero") String numero) {
        boolean existe = demandeService.numeroPasseportDejaUtilise(numero);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    @GetMapping("/verifier-numero-visa/{numero}")
    public ResponseEntity<Map<String, Boolean>> verifierNumeroVisa(@PathVariable("numero") String numero) {
        boolean existe = demandeService.numeroVisaDejaUtilise(numero);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Mise à jour des informations du demandeur.
     * Autorisée uniquement si le dossier est au statut PHOTO_PRISE (2).
     */
    @PutMapping("/{id}/demandeur")
    public ResponseEntity<?> mettreAJourDemandeur(@PathVariable("id") Long id,
                                                   @RequestBody Map<String, String> body) {
        try {
            Demande demande = demandeService.getDemandeById(id);
            if (demande.getStatut() != Demande.STATUT_PHOTO_PRISE) {
                return ResponseEntity.badRequest().body(Map.of("error",
                        "La modification est possible uniquement au statut Photo prise."));
            }
            Demandeur dem = demande.getDemandeur();
            if (body.containsKey("nom")) dem.setNom(body.get("nom"));
            if (body.containsKey("prenom")) dem.setPrenom(body.get("prenom"));
            if (body.containsKey("dateNaissance") && body.get("dateNaissance") != null && !body.get("dateNaissance").isBlank())
                dem.setDateNaissance(LocalDate.parse(body.get("dateNaissance")));
            if (body.containsKey("lieuNaissance")) dem.setLieuNaissance(body.get("lieuNaissance"));
            if (body.containsKey("telephone")) dem.setTelephone(body.get("telephone"));
            if (body.containsKey("email")) dem.setEmail(body.get("email"));
            if (body.containsKey("adresse")) dem.setAdresse(body.get("adresse"));
            demandeurRepository.save(dem);
            return ResponseEntity.ok(Map.of("message", "Informations mises à jour."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/visa-reference/{numero}")
    public ResponseEntity<?> verifierReference(@PathVariable("numero") String numero) {
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
