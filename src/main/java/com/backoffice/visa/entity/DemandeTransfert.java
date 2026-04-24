package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande_transfert")
public class DemandeTransfert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_carte_origine", nullable = false)
    private CarteResident carteOrigine;

    @Column(name = "ancien_numero_passeport", nullable = false, length = 50)
    private String ancienNumeroPasseport;

    @Column(name = "nouveau_numero_passeport", nullable = false, length = 50)
    private String nouveauNumeroPasseport;

    @Column(name = "nouveau_pays_delivrance", length = 100)
    private String nouveauPaysDelivrance;

    @Column(name = "nouvelle_date_delivrance")
    private LocalDate nouvelleDateDelivrance;

    @Column(name = "nouvelle_date_expiration")
    private LocalDate nouvelleDateExpiration;

    public DemandeTransfert() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }
    public CarteResident getCarteOrigine() { return carteOrigine; }
    public void setCarteOrigine(CarteResident carteOrigine) { this.carteOrigine = carteOrigine; }
    public String getAncienNumeroPasseport() { return ancienNumeroPasseport; }
    public void setAncienNumeroPasseport(String ancienNumeroPasseport) { this.ancienNumeroPasseport = ancienNumeroPasseport; }
    public String getNouveauNumeroPasseport() { return nouveauNumeroPasseport; }
    public void setNouveauNumeroPasseport(String nouveauNumeroPasseport) { this.nouveauNumeroPasseport = nouveauNumeroPasseport; }
    public String getNouveauPaysDelivrance() { return nouveauPaysDelivrance; }
    public void setNouveauPaysDelivrance(String nouveauPaysDelivrance) { this.nouveauPaysDelivrance = nouveauPaysDelivrance; }
    public LocalDate getNouvelleDateDelivrance() { return nouvelleDateDelivrance; }
    public void setNouvelleDateDelivrance(LocalDate nouvelleDateDelivrance) { this.nouvelleDateDelivrance = nouvelleDateDelivrance; }
    public LocalDate getNouvelleDateExpiration() { return nouvelleDateExpiration; }
    public void setNouvelleDateExpiration(LocalDate nouvelleDateExpiration) { this.nouvelleDateExpiration = nouvelleDateExpiration; }
}
