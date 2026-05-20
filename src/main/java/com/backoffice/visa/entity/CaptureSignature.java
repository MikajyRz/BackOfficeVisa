package com.backoffice.visa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "capture_signature")
public class CaptureSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_demande", nullable = false, unique = true)
    private Demande demande;

    @Column(name = "photo_path", nullable = false)
    private String photoPath;

    @Column(name = "signature_path", nullable = false)
    private String signaturePath;

    @Column(name = "date_capture", nullable = false)
    private LocalDateTime dateCapture;

    public CaptureSignature() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getSignaturePath() { return signaturePath; }
    public void setSignaturePath(String signaturePath) { this.signaturePath = signaturePath; }
    public LocalDateTime getDateCapture() { return dateCapture; }
    public void setDateCapture(LocalDateTime dateCapture) { this.dateCapture = dateCapture; }

    public Long getDemandeId() {
        return demande != null ? demande.getId() : null;
    }
}
