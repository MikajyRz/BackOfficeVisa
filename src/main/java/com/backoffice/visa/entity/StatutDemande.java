package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "statut_demande")
public class StatutDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @Column(nullable = false)
    private Integer statut;

    @Column(name = "date_changement_statut")
    private LocalDate dateChangementStatut;

    public StatutDemande() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Demande getDemande() { return demande; }
    public void setDemande(Demande demande) { this.demande = demande; }
    public Integer getStatut() { return statut; }
    public void setStatut(Integer statut) { this.statut = statut; }
    public LocalDate getDateChangementStatut() { return dateChangementStatut; }
    public void setDateChangementStatut(LocalDate dateChangementStatut) { this.dateChangementStatut = dateChangementStatut; }
}
