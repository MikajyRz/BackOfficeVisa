package com.backoffice.visa.dto;

import java.time.LocalDate;

public class TransfertFormDTO {

    private Long idDemandeOrigine;
    private String nouveauNumeroPasseport;
    private String nouveauPaysDelivrance;
    private LocalDate nouvelleDateDelivrance;
    private LocalDate nouvelleDateExpiration;

    public TransfertFormDTO() {}

    public Long getIdDemandeOrigine() { return idDemandeOrigine; }
    public void setIdDemandeOrigine(Long idDemandeOrigine) { this.idDemandeOrigine = idDemandeOrigine; }
    public String getNouveauNumeroPasseport() { return nouveauNumeroPasseport; }
    public void setNouveauNumeroPasseport(String nouveauNumeroPasseport) { this.nouveauNumeroPasseport = nouveauNumeroPasseport; }
    public String getNouveauPaysDelivrance() { return nouveauPaysDelivrance; }
    public void setNouveauPaysDelivrance(String nouveauPaysDelivrance) { this.nouveauPaysDelivrance = nouveauPaysDelivrance; }
    public LocalDate getNouvelleDateDelivrance() { return nouvelleDateDelivrance; }
    public void setNouvelleDateDelivrance(LocalDate nouvelleDateDelivrance) { this.nouvelleDateDelivrance = nouvelleDateDelivrance; }
    public LocalDate getNouvelleDateExpiration() { return nouvelleDateExpiration; }
    public void setNouvelleDateExpiration(LocalDate nouvelleDateExpiration) { this.nouvelleDateExpiration = nouvelleDateExpiration; }
}
