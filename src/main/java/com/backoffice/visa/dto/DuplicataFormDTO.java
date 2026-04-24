package com.backoffice.visa.dto;

import java.time.LocalDate;

public class DuplicataFormDTO {

    private Long idDemandeOrigine;
    private String motif;
    private LocalDate dateDeclaration;
    private String referenceAncienneCarte;
    private LocalDate dateDelivranceDuplicata;
    private LocalDate dateExpirationDuplicata;

    public DuplicataFormDTO() {}

    public Long getIdDemandeOrigine() { return idDemandeOrigine; }
    public void setIdDemandeOrigine(Long idDemandeOrigine) { this.idDemandeOrigine = idDemandeOrigine; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDate getDateDeclaration() { return dateDeclaration; }
    public void setDateDeclaration(LocalDate dateDeclaration) { this.dateDeclaration = dateDeclaration; }
    public String getReferenceAncienneCarte() { return referenceAncienneCarte; }
    public void setReferenceAncienneCarte(String referenceAncienneCarte) { this.referenceAncienneCarte = referenceAncienneCarte; }
    public LocalDate getDateDelivranceDuplicata() { return dateDelivranceDuplicata; }
    public void setDateDelivranceDuplicata(LocalDate dateDelivranceDuplicata) { this.dateDelivranceDuplicata = dateDelivranceDuplicata; }
    public LocalDate getDateExpirationDuplicata() { return dateExpirationDuplicata; }
    public void setDateExpirationDuplicata(LocalDate dateExpirationDuplicata) { this.dateExpirationDuplicata = dateExpirationDuplicata; }
}
