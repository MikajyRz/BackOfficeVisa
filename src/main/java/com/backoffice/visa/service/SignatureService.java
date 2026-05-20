package com.backoffice.visa.service;

import com.backoffice.visa.entity.CaptureSignature;
import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.StatutDemande;
import com.backoffice.visa.repository.CaptureSignatureRepository;
import com.backoffice.visa.repository.DemandeRepository;
import com.backoffice.visa.repository.StatutDemandeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SignatureService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final CaptureSignatureRepository captureSignatureRepository;
    private final DemandeRepository demandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;

    public SignatureService(CaptureSignatureRepository captureSignatureRepository,
                            DemandeRepository demandeRepository,
                            StatutDemandeRepository statutDemandeRepository) {
        this.captureSignatureRepository = captureSignatureRepository;
        this.demandeRepository = demandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de creer le dossier d'upload : " + uploadDir, e);
        }
    }

    public Optional<CaptureSignature> getCapture(Long demandeId) {
        return captureSignatureRepository.findByDemandeId(demandeId);
    }

    public boolean captureExiste(Long demandeId) {
        return captureSignatureRepository.existsByDemandeId(demandeId);
    }

    @Transactional
    public CaptureSignature enregistrerCapture(Long demandeId, MultipartFile photo, MultipartFile signature) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        int nouveauStatut = statutSignatureTerminee(demande.getStatut());
        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("La photo est obligatoire");
        }
        if (signature == null || signature.isEmpty()) {
            throw new RuntimeException("La signature est obligatoire");
        }

        CaptureSignature capture = captureSignatureRepository.findByDemandeId(demandeId)
                .orElseGet(CaptureSignature::new);

        if (capture.getId() != null) {
            supprimerFichier(capture.getPhotoPath());
            supprimerFichier(capture.getSignaturePath());
        }

        capture.setDemande(demande);
        capture.setPhotoPath(sauvegarderFichier(photo, "photo", demandeId));
        capture.setSignaturePath(sauvegarderFichier(signature, "signature", demandeId));
        capture.setDateCapture(LocalDateTime.now());
        capture = captureSignatureRepository.save(capture);

        demande.setStatut(nouveauStatut);
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, nouveauStatut);

        return capture;
    }

    public Resource chargerFichier(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Fichier introuvable", e);
        }
    }

    public MediaType mediaType(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            String contentType = Files.probeContentType(filePath);
            return MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream");
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private int statutSignatureTerminee(Integer statutActuel) {
        if (statutActuel == Demande.STATUT_CREATION) {
            return Demande.STATUT_SIGNATURE_TERMINEE;
        }
        if (statutActuel == Demande.STATUT_SIGNATURE_TERMINEE) {
            return Demande.STATUT_SIGNATURE_TERMINEE;
        }
        if (statutActuel == Demande.STATUT_DUPLICATA_DEMANDE) {
            return Demande.STATUT_DUPLICATA_SIGNATURE_TERMINEE;
        }
        if (statutActuel == Demande.STATUT_DUPLICATA_SIGNATURE_TERMINEE) {
            return Demande.STATUT_DUPLICATA_SIGNATURE_TERMINEE;
        }
        if (statutActuel == Demande.STATUT_TRANSFERT_DEMANDE) {
            return Demande.STATUT_TRANSFERT_SIGNATURE_TERMINEE;
        }
        if (statutActuel == Demande.STATUT_TRANSFERT_SIGNATURE_TERMINEE) {
            return Demande.STATUT_TRANSFERT_SIGNATURE_TERMINEE;
        }
        throw new RuntimeException("La signature n'est pas autorisee depuis le statut actuel : " + statutActuel);
    }

    private String sauvegarderFichier(MultipartFile file, String type, Long demandeId) {
        String extension = ".png";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = type + "_" + demandeId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        try {
            Path targetPath = Paths.get(uploadDir).resolve(fileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier", e);
        }
    }

    private void supprimerFichier(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(fileName).normalize());
        } catch (IOException ignored) {
        }
    }

    private void enregistrerChangementStatut(Demande demande, int statut) {
        StatutDemande sd = new StatutDemande();
        sd.setDemande(demande);
        sd.setStatut(statut);
        sd.setDateChangementStatut(LocalDate.now());
        statutDemandeRepository.save(sd);
    }
}
