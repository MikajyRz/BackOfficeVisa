package com.backoffice.visa.controller;

import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.Demandeur;
import com.backoffice.visa.repository.DemandeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final DemandeRepository demandeRepository;

    public PublicController(DemandeRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
    }

    /**
     * Endpoint public accessible sans authentification.
     * Appelé par suivi.html après scan du QR code.
     * Référence format : DOS-00003
     */
    @GetMapping("/suivi/{reference}")
    public ResponseEntity<?> suiviDossier(@PathVariable("reference") String reference) {
        try {
            // Parser la référence "DOS-00003" → id = 3
            if (!reference.startsWith("DOS-")) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Référence invalide"));
            }
            long id;
            try {
                id = Long.parseLong(reference.substring(4));
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Référence invalide"));
            }

            Demande demande = demandeRepository.findById(id).orElse(null);
            if (demande == null) {
                return ResponseEntity.status(404).body(Map.of("erreur", "Dossier introuvable"));
            }

            Demandeur dem = demande.getDemandeur();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("reference", reference);
            result.put("statut", demande.getStatut());
            result.put("statutLibelle", demande.getStatutLibelle());
            result.put("dateDemande", demande.getDateDemande() != null ? demande.getDateDemande().toString() : null);
            result.put("dateTraitement", demande.getDateTraitement() != null ? demande.getDateTraitement().toString() : null);

            if (dem != null) {
                result.put("nom", dem.getNom());
                result.put("prenom", dem.getPrenom());
            }

            if (demande.getTypeVisa() != null) {
                result.put("typeVisa", demande.getTypeVisa().getLibelle());
            }
            if (demande.getTypeDemande() != null) {
                result.put("typeDemande", demande.getTypeDemande().getLibelle());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erreur", "Erreur serveur"));
        }
    }
}
