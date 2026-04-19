package com.backoffice.visa.dto;

import java.time.LocalDate;
import java.util.List;

public class DemandeFormDTO {

    // --- Informations personnelles ---
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String telephone;
    private String email;
    private String adresse;
    private Long idSituationFamiliale;
    private Long idNationalite;

    // --- Informations du passeport ---
    private String numeroPasseport;
    private LocalDate dateDelivrancePasseport;
    private LocalDate dateExpirationPasseport;
    private String paysDelivrance;

    // --- Informations de la demande ---
    private Long idTypeVisa;
    private Long idTypeDemande;

    // --- Pièces justificatives cochées ---
    private List<Long> piecesCommunesPresentes;
    private List<Long> piecesSpecifiquesPresentes;

    public DemandeFormDTO() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public Long getIdSituationFamiliale() { return idSituationFamiliale; }
    public void setIdSituationFamiliale(Long idSituationFamiliale) { this.idSituationFamiliale = idSituationFamiliale; }
    public Long getIdNationalite() { return idNationalite; }
    public void setIdNationalite(Long idNationalite) { this.idNationalite = idNationalite; }
    public String getNumeroPasseport() { return numeroPasseport; }
    public void setNumeroPasseport(String numeroPasseport) { this.numeroPasseport = numeroPasseport; }
    public LocalDate getDateDelivrancePasseport() { return dateDelivrancePasseport; }
    public void setDateDelivrancePasseport(LocalDate dateDelivrancePasseport) { this.dateDelivrancePasseport = dateDelivrancePasseport; }
    public LocalDate getDateExpirationPasseport() { return dateExpirationPasseport; }
    public void setDateExpirationPasseport(LocalDate dateExpirationPasseport) { this.dateExpirationPasseport = dateExpirationPasseport; }
    public String getPaysDelivrance() { return paysDelivrance; }
    public void setPaysDelivrance(String paysDelivrance) { this.paysDelivrance = paysDelivrance; }
    public Long getIdTypeVisa() { return idTypeVisa; }
    public void setIdTypeVisa(Long idTypeVisa) { this.idTypeVisa = idTypeVisa; }
    public Long getIdTypeDemande() { return idTypeDemande; }
    public void setIdTypeDemande(Long idTypeDemande) { this.idTypeDemande = idTypeDemande; }
    public List<Long> getPiecesCommunesPresentes() { return piecesCommunesPresentes; }
    public void setPiecesCommunesPresentes(List<Long> piecesCommunesPresentes) { this.piecesCommunesPresentes = piecesCommunesPresentes; }
    public List<Long> getPiecesSpecifiquesPresentes() { return piecesSpecifiquesPresentes; }
    public void setPiecesSpecifiquesPresentes(List<Long> piecesSpecifiquesPresentes) { this.piecesSpecifiquesPresentes = piecesSpecifiquesPresentes; }
}
