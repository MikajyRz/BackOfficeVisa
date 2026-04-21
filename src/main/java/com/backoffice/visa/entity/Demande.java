package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable")
    private VisaTransformable visaTransformable;

    @Column(name = "date_demande", nullable = false)
    private LocalDate dateDemande;

    @Column(name = "id_statut", nullable = false)
    private Integer statut = 1; // 1=Dossier créé, 2=Dossier scanné

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;

    @ManyToOne
    @JoinColumn(name = "id_type_demande", nullable = false)
    private TypeDemande typeDemande;

    @Column(name = "date_traitement")
    private LocalDate dateTraitement;

    public Demande() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public VisaTransformable getVisaTransformable() { return visaTransformable; }
    public void setVisaTransformable(VisaTransformable visaTransformable) { this.visaTransformable = visaTransformable; }
    public LocalDate getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDate dateDemande) { this.dateDemande = dateDemande; }
    public Integer getStatut() { return statut; }
    public void setStatut(Integer statut) { this.statut = statut; }
    public Demandeur getDemandeur() { return demandeur; }
    public void setDemandeur(Demandeur demandeur) { this.demandeur = demandeur; }
    public TypeVisa getTypeVisa() { return typeVisa; }
    public void setTypeVisa(TypeVisa typeVisa) { this.typeVisa = typeVisa; }
    public TypeDemande getTypeDemande() { return typeDemande; }
    public void setTypeDemande(TypeDemande typeDemande) { this.typeDemande = typeDemande; }
    public LocalDate getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDate dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getStatutLibelle() {
        return switch (statut) {
            case 1 -> "Dossier créé";
            case 2 -> "Dossier scanné";
            default -> "Inconnu";
        };
    }
}
