package com.backoffice.visa.service;

import com.backoffice.visa.dto.DuplicataFormDTO;
import com.backoffice.visa.entity.*;
import com.backoffice.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class DuplicataService {

    private final DemandeRepository demandeRepository;
    private final CarteResidentRepository carteResidentRepository;
    private final DemandeDuplicataRepository demandeDuplicataRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final TypeDemandeRepository typeDemandeRepository;

    public DuplicataService(
            DemandeRepository demandeRepository,
            CarteResidentRepository carteResidentRepository,
            DemandeDuplicataRepository demandeDuplicataRepository,
            StatutDemandeRepository statutDemandeRepository,
            TypeDemandeRepository typeDemandeRepository) {
        this.demandeRepository = demandeRepository;
        this.carteResidentRepository = carteResidentRepository;
        this.demandeDuplicataRepository = demandeDuplicataRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.typeDemandeRepository = typeDemandeRepository;
    }

    /**
     * Recherche une demande éligible au duplicata par numéro de passeport ou référence carte
     */
    public Optional<Demande> rechercherDemandeEligible(String critere) {
        // Recherche par ID de demande d'origine pour simplifier dans un premier temps, 
        // ou on pourrait chercher par numéro de passeport via Demandeur.
        // Ici on va chercher par ID de demande ou par référence de carte.
        
        // Tentative par ID
        try {
            Long id = Long.parseLong(critere);
            return demandeRepository.findById(id)
                    .filter(Demande::estDossierApprouve);
        } catch (NumberFormatException e) {
            // Tentative par référence de carte
            return carteResidentRepository.findAll().stream()
                    .filter(c -> critere.equalsIgnoreCase(c.getReference()))
                    .map(CarteResident::getDemande)
                    .filter(Demande::estDossierApprouve)
                    .findFirst();
        }
    }

    /**
     * Crée une demande de duplicata
     */
    @Transactional
    public DemandeDuplicata creerDemandeDuplicata(DuplicataFormDTO form) {
        Demande demandeOrigine = demandeRepository.findById(form.getIdDemandeOrigine())
                .orElseThrow(() -> new RuntimeException("Demande d'origine introuvable"));

        if (!demandeOrigine.estDossierApprouve()) {
            throw new RuntimeException("La demande d'origine doit être approuvée");
        }

        CarteResident carteOrigine = carteResidentRepository.findByDemandeId(demandeOrigine.getId())
                .orElseThrow(() -> new RuntimeException("Carte de résident d'origine introuvable"));

        // Créer une nouvelle Demande pour le duplicata
        Demande demandeDuplicata = new Demande();
        demandeDuplicata.setDemandeur(demandeOrigine.getDemandeur());
        demandeDuplicata.setTypeVisa(demandeOrigine.getTypeVisa());
        
        // Type de demande = Duplicata (id=2 dans data.sql)
        TypeDemande typeDuplicata = typeDemandeRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Type de demande 'Duplicata' introuvable"));
        demandeDuplicata.setTypeDemande(typeDuplicata);
        
        demandeDuplicata.setDateDemande(LocalDate.now());
        demandeDuplicata.setStatut(Demande.STATUT_DUPLICATA_DEMANDE);
        demandeDuplicata.setVisaTransformable(demandeOrigine.getVisaTransformable());
        demandeDuplicata = demandeRepository.save(demandeDuplicata);

        // Enregistrer l'historique de statut
        enregistrerChangementStatut(demandeDuplicata, Demande.STATUT_DUPLICATA_DEMANDE);

        // Créer l'entité de détail Duplicata
        DemandeDuplicata detail = new DemandeDuplicata();
        detail.setDemande(demandeDuplicata);
        detail.setCarteOrigine(carteOrigine);
        detail.setMotif(form.getMotif());
        detail.setDateDeclaration(form.getDateDeclaration());
        detail.setReferenceAncienneCarte(carteOrigine.getReference());
        detail.setDateDelivranceDuplicata(form.getDateDelivranceDuplicata() != null ? form.getDateDelivranceDuplicata() : LocalDate.now());
        
        // Date expiration : par défaut celle de la carte d'origine
        detail.setDateExpirationDuplicata(form.getDateExpirationDuplicata() != null ? form.getDateExpirationDuplicata() : carteOrigine.getDateFin());
        
        return demandeDuplicataRepository.save(detail);
    }

    @Transactional
    public void validerDuplicata(Long duplicataDemandeId) {
        Demande demande = demandeRepository.findById(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));
        
        if (demande.getStatut() != Demande.STATUT_DUPLICATA_DEMANDE) {
            throw new RuntimeException("Statut invalide pour validation");
        }
        
        demande.setStatut(Demande.STATUT_DUPLICATA_VALIDE);
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, Demande.STATUT_DUPLICATA_VALIDE);
    }

    @Transactional
    public void rejeterDuplicata(Long duplicataDemandeId) {
        Demande demande = demandeRepository.findById(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));
        
        if (demande.getStatut() != Demande.STATUT_DUPLICATA_DEMANDE) {
            throw new RuntimeException("Statut invalide pour rejet");
        }
        
        demande.setStatut(Demande.STATUT_DUPLICATA_REJETE);
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, Demande.STATUT_DUPLICATA_REJETE);
    }

    @Transactional
    public CarteResident emettreDuplicata(Long duplicataDemandeId) {
        Demande demande = demandeRepository.findById(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));
        
        if (demande.getStatut() != Demande.STATUT_DUPLICATA_VALIDE) {
            throw new RuntimeException("La demande doit être validée avant émission");
        }

        DemandeDuplicata detail = demandeDuplicataRepository.findByDemandeId(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Détails du duplicata introuvables"));

        // Créer la nouvelle carte (duplicata)
        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDemande(demande);
        nouvelleCarte.setPasseport(demande.getVisaTransformable().getPasseport());
        nouvelleCarte.setDateDebut(detail.getDateDelivranceDuplicata());
        nouvelleCarte.setDateFin(detail.getDateExpirationDuplicata());
        nouvelleCarte.setReference("CR-DUP-" + demande.getId() + "-" + LocalDate.now().getYear());
        
        nouvelleCarte = carteResidentRepository.save(nouvelleCarte);

        // Mettre à jour le statut de la demande
        demande.setStatut(Demande.STATUT_DUPLICATA_EMIS);
        demande.setDateTraitement(LocalDate.now());
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, Demande.STATUT_DUPLICATA_EMIS);

        return nouvelleCarte;
    }

    private void enregistrerChangementStatut(Demande demande, int statut) {
        StatutDemande sd = new StatutDemande();
        sd.setDemande(demande);
        sd.setStatut(statut);
        sd.setDateChangementStatut(LocalDate.now());
        statutDemandeRepository.save(sd);
    }
}
