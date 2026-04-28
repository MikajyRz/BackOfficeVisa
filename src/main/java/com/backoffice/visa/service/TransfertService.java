package com.backoffice.visa.service;

import com.backoffice.visa.dto.TransfertFormDTO;
import com.backoffice.visa.entity.*;
import com.backoffice.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransfertService {

    private final DemandeRepository demandeRepository;
    private final DemandeTransfertRepository demandeTransfertRepository;
    private final CarteResidentRepository carteResidentRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final PasseportRepository passeportRepository;
    private final VisaTransformableRepository visaTransformableRepository;

    public TransfertService(DemandeRepository demandeRepository,
                           DemandeTransfertRepository demandeTransfertRepository,
                           CarteResidentRepository carteResidentRepository,
                           TypeDemandeRepository typeDemandeRepository,
                           StatutDemandeRepository statutDemandeRepository,
                           PasseportRepository passeportRepository,
                           VisaTransformableRepository visaTransformableRepository) {
        this.demandeRepository = demandeRepository;
        this.demandeTransfertRepository = demandeTransfertRepository;
        this.carteResidentRepository = carteResidentRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.passeportRepository = passeportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
    }

    public Optional<Demande> rechercherDemandeEligible(String critere) {
        Optional<Demande> demandeOpt = Optional.empty();
        
        try {
            Long id = Long.parseLong(critere);
            demandeOpt = demandeRepository.findById(id);
        } catch (NumberFormatException e) {}

        if (demandeOpt.isEmpty()) {
            demandeOpt = carteResidentRepository.findAll().stream()
                    .filter(c -> critere.equalsIgnoreCase(c.getReference()))
                    .map(CarteResident::getDemande)
                    .findFirst();
        }

        if (demandeOpt.isEmpty()) {
            demandeOpt = passeportRepository.findAll().stream()
                    .filter(p -> critere.equalsIgnoreCase(p.getNumeroPasseport()))
                    .flatMap(p -> demandeRepository.findByDemandeurId(p.getDemandeur().getId()).stream())
                    .filter(d -> d.getStatut() == Demande.STATUT_TERMINE)
                    // On s'assure que la demande trouvée est bien celle liée au passeport recherché
                    .filter(d -> d.getVisaTransformable() != null && 
                                 d.getVisaTransformable().getPasseport() != null && 
                                 critere.equalsIgnoreCase(d.getVisaTransformable().getPasseport().getNumeroPasseport()))
                    .sorted((d1, d2) -> d2.getId().compareTo(d1.getId()))
                    .findFirst();
        }

        return demandeOpt.filter(this::estEligibleAuTransfert);
    }

    public Optional<DemandeTransfert> findByDemandeId(Long demandeId) {
        return demandeTransfertRepository.findByDemandeId(demandeId);
    }

    private boolean estEligibleAuTransfert(Demande d) {
        if (d.getStatut() != Demande.STATUT_TERMINE) return false;
        Optional<CarteResident> carteOpt = carteResidentRepository.findByDemandeId(d.getId());
        if (carteOpt.isEmpty()) return false;
        if (carteOpt.get().getDateFin().isBefore(LocalDate.now())) return false;
        return true;
    }

    @Transactional
    public DemandeTransfert creerDemandeTransfert(TransfertFormDTO form) {
        Demande demandeOrigine = demandeRepository.findById(form.getIdDemandeOrigine())
                .orElseThrow(() -> new RuntimeException("Demande d'origine introuvable"));

        if (!estEligibleAuTransfert(demandeOrigine)) {
            throw new RuntimeException("Dossier non éligible au transfert");
        }

        CarteResident carteOrigine = carteResidentRepository.findByDemandeId(demandeOrigine.getId())
                .orElseThrow(() -> new RuntimeException("Carte d'origine introuvable"));

        // Créer nouvelle demande
        Demande demandeTransfert = new Demande();
        demandeTransfert.setDemandeur(demandeOrigine.getDemandeur());
        demandeTransfert.setTypeVisa(demandeOrigine.getTypeVisa());
        TypeDemande typeTransfert = typeDemandeRepository.findById(3L) // On suppose 3 pour Transfert
                .orElseGet(() -> typeDemandeRepository.findAll().stream()
                        .filter(t -> t.getLibelle().contains("Transfert"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Type de demande 'Transfert' introuvable")));
        
        demandeTransfert.setTypeDemande(typeTransfert);
        demandeTransfert.setDateDemande(LocalDate.now());
        demandeTransfert.setStatut(Demande.STATUT_TRANSFERT_DEMANDE);
        demandeTransfert.setVisaTransformable(demandeOrigine.getVisaTransformable());
        demandeTransfert = demandeRepository.save(demandeTransfert);

        enregistrerChangementStatut(demandeTransfert, Demande.STATUT_TRANSFERT_DEMANDE);

        // Détails transfert
        DemandeTransfert detail = new DemandeTransfert();
        detail.setDemande(demandeTransfert);
        detail.setCarteOrigine(carteOrigine);
        detail.setAncienNumeroPasseport(demandeOrigine.getVisaTransformable().getPasseport().getNumeroPasseport());
        detail.setNouveauNumeroPasseport(form.getNouveauNumeroPasseport());
        detail.setNouveauPaysDelivrance(form.getNouveauPaysDelivrance());
        detail.setNouvelleDateDelivrance(form.getNouvelleDateDelivrance());
        detail.setNouvelleDateExpiration(form.getNouvelleDateExpiration());
        
        return demandeTransfertRepository.save(detail);
    }

    @Transactional
    public void validerTransfert(Long id) {
        Demande d = demandeRepository.findById(id).orElseThrow();
        d.setStatut(Demande.STATUT_TRANSFERT_VALIDE);
        demandeRepository.save(d);
        enregistrerChangementStatut(d, Demande.STATUT_TRANSFERT_VALIDE);
    }

    @Transactional
    public void rejeterTransfert(Long id) {
        Demande d = demandeRepository.findById(id).orElseThrow();
        d.setStatut(Demande.STATUT_TRANSFERT_REJETE);
        demandeRepository.save(d);
        enregistrerChangementStatut(d, Demande.STATUT_TRANSFERT_REJETE);
    }

    @Transactional
    public CarteResident emettreTransfert(Long id) {
        Demande demande = demandeRepository.findById(id).orElseThrow();
        DemandeTransfert detail = demandeTransfertRepository.findByDemandeId(id).orElseThrow();
        
        // 1. Créer/Mettre à jour le passeport du demandeur
        Passeport nouveauPass = new Passeport();
        nouveauPass.setDemandeur(demande.getDemandeur());
        nouveauPass.setNumeroPasseport(detail.getNouveauNumeroPasseport());
        nouveauPass.setPaysDelivrance(detail.getNouveauPaysDelivrance());
        nouveauPass.setDateDelivrance(detail.getNouvelleDateDelivrance());
        nouveauPass.setDateExpiration(detail.getNouvelleDateExpiration());
        nouveauPass = passeportRepository.save(nouveauPass);

        // 2. Créer un nouveau VisaTransformable (pour garder l'historique du passeport d'origine)
        VisaTransformable ancienVisa = demande.getVisaTransformable();
        VisaTransformable nouveauVisa = new VisaTransformable();
        nouveauVisa.setDemandeur(demande.getDemandeur());
        nouveauVisa.setPasseport(nouveauPass);
        nouveauVisa.setNumeroReference(ancienVisa.getNumeroReference());
        nouveauVisa.setLieu(ancienVisa.getLieu());
        nouveauVisa.setDateDebut(ancienVisa.getDateDebut());
        nouveauVisa.setDateFin(ancienVisa.getDateFin());
        nouveauVisa = visaTransformableRepository.save(nouveauVisa);

        // Mettre à jour la demande pour pointer vers le nouveau visa
        demande.setVisaTransformable(nouveauVisa);

        // 3. Émettre nouvelle carte
        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDemande(demande);
        nouvelleCarte.setPasseport(nouveauPass);
        nouvelleCarte.setDateDebut(LocalDate.now());
        nouvelleCarte.setDateFin(detail.getCarteOrigine().getDateFin()); // On garde la même fin
        nouvelleCarte.setReference("CR-TRF-" + demande.getId() + "-" + LocalDate.now().getYear());
        nouvelleCarte = carteResidentRepository.save(nouvelleCarte);

        demande.setStatut(Demande.STATUT_TRANSFERT_EMIS);
        demande.setDateTraitement(LocalDate.now());
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, Demande.STATUT_TRANSFERT_EMIS);

        return nouvelleCarte;
    }

    private void enregistrerChangementStatut(Demande d, int statut) {
        StatutDemande sd = new StatutDemande();
        sd.setDemande(d);
        sd.setStatut(statut);
        sd.setDateChangementStatut(LocalDate.now());
        statutDemandeRepository.save(sd);
    }
}
