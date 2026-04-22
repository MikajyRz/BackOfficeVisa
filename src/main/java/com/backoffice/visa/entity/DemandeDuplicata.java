package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande_duplicata")
public class DemandeDuplicata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_carte_origine", nullable = false)
    private CarteResident carteOrigine;

    @Column(nullable = false, length = 100)
    private String motif;

    @Column(name = "date_declaration")
    private LocalDate dateDeclaration;

    @Column(name = "reference_ancienne_carte")
    private String referenceAncienneCarte;

    @Column(name = "date_delivrance_duplicata")
    private LocalDate dateDelivranceDuplicata;

    @Column(name = "date_expiration_duplicata")
    private LocalDate dateExpirationDuplicata;

    public DemandeDuplicata() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }
    public CarteResident getCarteOrigine() { return carteOrigine; }
    public void setCarteOrigine(CarteResident carteOrigine) { this.carteOrigine = carteOrigine; }
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
