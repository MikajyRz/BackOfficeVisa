package com.backoffice.visa.controller;

import com.backoffice.visa.entity.PieceDemande;
import com.backoffice.visa.entity.PieceDemandeSpecifique;
import com.backoffice.visa.repository.PieceDemandeRepository;
import com.backoffice.visa.repository.PieceDemandeSpecifiqueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pieces")
public class PieceUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final PieceDemandeRepository pieceDemandeRepository;
    private final PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository;

    public PieceUploadController(PieceDemandeRepository pieceDemandeRepository,
                                  PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository) {
        this.pieceDemandeRepository = pieceDemandeRepository;
        this.pieceDemandeSpecifiqueRepository = pieceDemandeSpecifiqueRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload : " + uploadDir, e);
        }
    }

    /**
     * Upload d'un fichier pour une pièce commune
     */
    @PostMapping("/communes/{pieceId}/upload")
    public ResponseEntity<?> uploadPieceCommune(@PathVariable("pieceId") Long pieceId,
                                                 @RequestParam("file") MultipartFile file) {
        PieceDemande piece = pieceDemandeRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce introuvable"));

        String fileName = saveFile(file, "commune", pieceId);
        piece.setFichierPath(fileName);
        piece.setPresente(true);
        pieceDemandeRepository.save(piece);

        return ResponseEntity.ok(Map.of(
                "message", "Fichier uploadé avec succès",
                "fichierPath", fileName
        ));
    }

    /**
     * Upload d'un fichier pour une pièce spécifique
     */
    @PostMapping("/specifiques/{pieceId}/upload")
    public ResponseEntity<?> uploadPieceSpecifique(@PathVariable("pieceId") Long pieceId,
                                                    @RequestParam("file") MultipartFile file) {
        PieceDemandeSpecifique piece = pieceDemandeSpecifiqueRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce spécifique introuvable"));

        String fileName = saveFile(file, "specifique", pieceId);
        piece.setFichierPath(fileName);
        piece.setPresente(true);
        pieceDemandeSpecifiqueRepository.save(piece);

        return ResponseEntity.ok(Map.of(
                "message", "Fichier uploadé avec succès",
                "fichierPath", fileName
        ));
    }

    /**
     * Servir un fichier uploadé
     */
    @GetMapping("/fichier/{fileName:.+}")
    public ResponseEntity<Resource> getFichier(@PathVariable("fileName") String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprimer un fichier uploadé pour une pièce commune
     */
    @DeleteMapping("/communes/{pieceId}/fichier")
    public ResponseEntity<?> deletePieceCommuneFichier(@PathVariable("pieceId") Long pieceId) {
        PieceDemande piece = pieceDemandeRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce introuvable"));

        if (piece.getFichierPath() != null) {
            deleteFile(piece.getFichierPath());
            piece.setFichierPath(null);
            pieceDemandeRepository.save(piece);
        }

        return ResponseEntity.ok(Map.of("message", "Fichier supprimé"));
    }

    /**
     * Supprimer un fichier uploadé pour une pièce spécifique
     */
    @DeleteMapping("/specifiques/{pieceId}/fichier")
    public ResponseEntity<?> deletePieceSpecifiqueFichier(@PathVariable("pieceId") Long pieceId) {
        PieceDemandeSpecifique piece = pieceDemandeSpecifiqueRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce spécifique introuvable"));

        if (piece.getFichierPath() != null) {
            deleteFile(piece.getFichierPath());
            piece.setFichierPath(null);
            pieceDemandeSpecifiqueRepository.save(piece);
        }

        return ResponseEntity.ok(Map.of("message", "Fichier supprimé"));
    }

    private String saveFile(MultipartFile file, String type, Long pieceId) {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = type + "_" + pieceId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        try {
            Path targetPath = Paths.get(uploadDir).resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }

    private void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Ignorer l'erreur de suppression
        }
    }
}
