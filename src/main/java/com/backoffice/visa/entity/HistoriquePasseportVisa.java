package com.backoffice.visa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_passeport_visa")
public class HistoriquePasseportVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable", nullable = false)
    private VisaTransformable visaTransformable;

    @ManyToOne
    @JoinColumn(name = "id_ancien_passeport")
    private Passeport ancienPasseport;

    @ManyToOne
    @JoinColumn(name = "id_nouveau_passeport", nullable = false)
    private Passeport nouveauPasseport;

    @Column(name = "date_transfert", nullable = false)
    private LocalDate dateTransfert;

    public HistoriquePasseportVisa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VisaTransformable getVisaTransformable() { return visaTransformable; }
    public void setVisaTransformable(VisaTransformable visaTransformable) { this.visaTransformable = visaTransformable; }

    public Passeport getAncienPasseport() { return ancienPasseport; }
    public void setAncienPasseport(Passeport ancienPasseport) { this.ancienPasseport = ancienPasseport; }

    public Passeport getNouveauPasseport() { return nouveauPasseport; }
    public void setNouveauPasseport(Passeport nouveauPasseport) { this.nouveauPasseport = nouveauPasseport; }

    public LocalDate getDateTransfert() { return dateTransfert; }
    public void setDateTransfert(LocalDate dateTransfert) { this.dateTransfert = dateTransfert; }
}
