package com.backoffice.visa.controller;

import com.backoffice.visa.entity.CaptureSignature;
import com.backoffice.visa.service.SignatureService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class SignatureController {

    private final SignatureService signatureService;

    public SignatureController(SignatureService signatureService) {
        this.signatureService = signatureService;
    }

    @GetMapping("/{id}/signature")
    public ResponseEntity<?> getSignature(@PathVariable("id") Long id) {
        return signatureService.getCapture(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/signature")
    public ResponseEntity<?> enregistrerSignature(@PathVariable("id") Long id,
                                                  @RequestParam("photo") MultipartFile photo,
                                                  @RequestParam("signature") MultipartFile signature) {
        try {
            CaptureSignature capture = signatureService.enregistrerCapture(id, photo, signature);
            return ResponseEntity.ok(Map.of(
                    "id", capture.getId(),
                    "demandeId", capture.getDemandeId(),
                    "photoPath", capture.getPhotoPath(),
                    "signaturePath", capture.getSignaturePath(),
                    "dateCapture", capture.getDateCapture(),
                    "message", "Signature terminee"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/signature/fichier/{fileName:.+}")
    public ResponseEntity<Resource> getFichierSignature(@PathVariable("fileName") String fileName) {
        Resource resource = signatureService.chargerFichier(fileName);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(signatureService.mediaType(fileName))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
