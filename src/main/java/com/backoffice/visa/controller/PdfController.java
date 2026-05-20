package com.backoffice.visa.controller;

import com.backoffice.visa.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandes")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/{id}/accuse-reception")
    public ResponseEntity<?> accuseReception(@PathVariable("id") Long id) {
        try {
            byte[] pdf = pdfService.genererAccuseReception(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"accuse-reception-" + id + ".pdf\"")
                    .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la génération du PDF.");
        }
    }

    @GetMapping("/{id}/apercu-pieces")
    public ResponseEntity<?> apercuPieces(@PathVariable("id") Long id) {
        try {
            byte[] pdf = pdfService.genererApercuPieces(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"pieces-" + id + ".pdf\"")
                    .body(pdf);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la génération de l'aperçu.");
        }
    }
}
