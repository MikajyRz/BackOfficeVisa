package com.backoffice.visa.controller;

import com.backoffice.visa.dto.TransfertFormDTO;
import com.backoffice.visa.entity.CarteResident;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.repository.CarteResidentRepository;
import com.backoffice.visa.service.TransfertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cartes-resident/transfert")
public class TransfertController {

    private final TransfertService transfertService;
    private final CarteResidentRepository carteResidentRepository;

    public TransfertController(TransfertService transfertService, CarteResidentRepository carteResidentRepository) {
        this.transfertService = transfertService;
        this.carteResidentRepository = carteResidentRepository;
    }

    @GetMapping("/recherche")
    public ResponseEntity<?> rechercher(@RequestParam("critere") String critere) {
        Optional<Demande> demandeOpt = transfertService.rechercherDemandeEligible(critere);
        if (demandeOpt.isPresent()) {
            Demande d = demandeOpt.get();
            Optional<CarteResident> carteOpt = carteResidentRepository.findByDemandeId(d.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("demande", d);
            result.put("carte", carteOpt.orElse(null));
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> creer(@RequestBody TransfertFormDTO form) {
        try {
            return ResponseEntity.ok(transfertService.creerDemandeTransfert(form));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> valider(@PathVariable("id") Long id) {
        transfertService.validerTransfert(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeter(@PathVariable("id") Long id) {
        transfertService.rejeterTransfert(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/emettre")
    public ResponseEntity<?> emettre(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(transfertService.emettreTransfert(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<?> getByDemandeId(@PathVariable("demandeId") Long demandeId) {
        return transfertService.findByDemandeId(demandeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
