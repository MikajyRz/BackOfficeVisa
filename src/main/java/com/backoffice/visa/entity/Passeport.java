package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "passeport")
public class Passeport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @Column(name = "numero_passeport", nullable = false, unique = true, length = 50)
    private String numeroPasseport;

    @Column(name = "date_delivrance", nullable = false)
    private LocalDate dateDelivrance;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @Column(name = "pays_delivrance", length = 100)
    private String paysDelivrance;

    public Passeport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demandeur getDemandeur() { return demandeur; }
    public void setDemandeur(Demandeur demandeur) { this.demandeur = demandeur; }
    public String getNumeroPasseport() { return numeroPasseport; }
    public void setNumeroPasseport(String numeroPasseport) { this.numeroPasseport = numeroPasseport; }
    public LocalDate getDateDelivrance() { return dateDelivrance; }
    public void setDateDelivrance(LocalDate dateDelivrance) { this.dateDelivrance = dateDelivrance; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getPaysDelivrance() { return paysDelivrance; }
    public void setPaysDelivrance(String paysDelivrance) { this.paysDelivrance = paysDelivrance; }
}
