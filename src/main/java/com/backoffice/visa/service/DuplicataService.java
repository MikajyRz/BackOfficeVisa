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
    private final DemandeService demandeService;
    private final com.backoffice.visa.repository.PasseportRepository passeportRepository;

    public DuplicataService(
            DemandeRepository demandeRepository,
            CarteResidentRepository carteResidentRepository,
            DemandeDuplicataRepository demandeDuplicataRepository,
            StatutDemandeRepository statutDemandeRepository,
            TypeDemandeRepository typeDemandeRepository,
            DemandeService demandeService,
            com.backoffice.visa.repository.PasseportRepository passeportRepository) {
        this.demandeRepository = demandeRepository;
        this.carteResidentRepository = carteResidentRepository;
        this.demandeDuplicataRepository = demandeDuplicataRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.demandeService = demandeService;
        this.passeportRepository = passeportRepository;
    }

    /**
     * Recherche une demande éligible au duplicata par numéro de passeport ou référence carte
     */
    public Optional<Demande> rechercherDemandeEligible(String critere) {
        Optional<Demande> demandeOpt;
        
        // 1. Tentative par ID de demande
        try {
            Long id = Long.parseLong(critere);
            demandeOpt = demandeRepository.findById(id);
            
            // 2. Si non trouvé par ID, on tente par numéro de passeport
            if (demandeOpt.isEmpty()) {
                demandeOpt = demandeRepository.findAll().stream()
                        .filter(d -> d.getVisaTransformable() != null && 
                                d.getVisaTransformable().getPasseport() != null &&
                                critere.equalsIgnoreCase(d.getVisaTransformable().getPasseport().getNumeroPasseport()))
                        .findFirst();
            }
        } catch (NumberFormatException e) {
            // 3. Tentative par référence de carte
            demandeOpt = carteResidentRepository.findAll().stream()
                    .filter(c -> critere.equalsIgnoreCase(c.getReference()))
                    .map(CarteResident::getDemande)
                    .findFirst();
        }

        return demandeOpt.filter(this::estEligibleAuDuplicata);
    }

    public Optional<DemandeDuplicata> findByDemandeId(Long demandeId) {
        return demandeDuplicataRepository.findByDemandeId(demandeId);
    }

    private boolean estEligibleAuDuplicata(Demande d) {
        System.out.println("Vérification éligibilité Duplicata pour Demande ID: " + d.getId());
        
        // 1. La demande d'origine doit être terminée
        if (d.getStatut() != Demande.STATUT_TERMINE) {
            System.out.println("-> ÉCHEC : Statut actuel (" + d.getStatut() + ") != TERMINE (2)");
            return false;
        }

        // 2. Une carte de résident doit exister
        Optional<CarteResident> carteOpt = carteResidentRepository.findByDemandeId(d.getId());
        if (carteOpt.isEmpty()) {
            System.out.println("-> ÉCHEC : Aucune CarteResident trouvée pour cette demande");
            return false;
        }

        // 3. La carte ne doit pas être expirée
        if (carteOpt.get().getDateFin().isBefore(LocalDate.now())) {
            System.out.println("-> ÉCHEC : La carte est expirée (Date fin: " + carteOpt.get().getDateFin() + ")");
            return false;
        }

        System.out.println("-> SUCCÈS : Le dossier est éligible");
        return true;
    }

    /**
     * Crée une demande de duplicata
     */
    @Transactional
    public DemandeDuplicata creerDemandeDuplicata(DuplicataFormDTO form) {
        Demande demandeOrigine = demandeRepository.findById(form.getIdDemandeOrigine())
                .orElseThrow(() -> new RuntimeException("Demande d'origine introuvable"));

        if (!estEligibleAuDuplicata(demandeOrigine)) {
            throw new RuntimeException("Ce dossier n'est pas éligible au duplicata (soit non terminé, soit carte expirée)");
        }

        // RÈGLE : Pas de demande de duplicata déjà en cours pour ce demandeur
        boolean aDejaUneDemandeEnCours = demandeRepository.findByDemandeurId(demandeOrigine.getDemandeur().getId()).stream()
                .anyMatch(d -> d.getStatut() == Demande.STATUT_DUPLICATA_DEMANDE
                        || d.getStatut() == Demande.STATUT_DUPLICATA_SIGNATURE_TERMINEE
                        || d.getStatut() == Demande.STATUT_DUPLICATA_SCANNE
                        || d.getStatut() == Demande.STATUT_DUPLICATA_VALIDE);
        
        if (aDejaUneDemandeEnCours) {
            throw new RuntimeException("Une demande de duplicata est déjà en cours pour ce demandeur");
        }

        CarteResident carteOrigine = carteResidentRepository.findByDemandeId(demandeOrigine.getId())
                .orElseThrow(() -> new RuntimeException("Carte de résident d'origine introuvable"));

        // RÈGLE : Validation des dates
        if ("Perte".equals(form.getMotif()) || "Vol".equals(form.getMotif())) {
            if (form.getDateDeclaration() == null) {
                throw new RuntimeException("La date de déclaration est obligatoire pour perte ou vol");
            }
            if (form.getDateDeclaration().isAfter(LocalDate.now())) {
                throw new RuntimeException("La date de déclaration ne peut pas être dans le futur");
            }
        }

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
        
        // RÈGLE : Date de délivrance du duplicata >= aujourd'hui
        LocalDate dateDelivrance = form.getDateDelivranceDuplicata() != null ? form.getDateDelivranceDuplicata() : LocalDate.now();
        if (dateDelivrance.isBefore(LocalDate.now())) {
            throw new RuntimeException("La date de délivrance ne peut pas être dans le passé");
        }
        detail.setDateDelivranceDuplicata(dateDelivrance);
        
        // RÈGLE : Un duplicata conserve la date d'expiration de la carte d'origine
        detail.setDateExpirationDuplicata(carteOrigine.getDateFin());
        
        DemandeDuplicata detailEnregistre = demandeDuplicataRepository.save(detail);
        demandeService.initialiserPiecesPourUpload(demandeDuplicata);
        return detailEnregistre;
    }

    @Transactional
    public void scannerDuplicata(Long duplicataDemandeId) {
        demandeDuplicataRepository.findByDemandeId(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Détails du duplicata introuvables"));

        demandeService.scannerDossier(duplicataDemandeId);
    }

    @Transactional
    public void validerDuplicata(Long duplicataDemandeId) {
        Demande demande = demandeRepository.findById(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));
        
        if (demande.getStatut() != Demande.STATUT_DUPLICATA_SCANNE) {
            throw new RuntimeException("Le duplicata doit etre scanne avant validation");
        }
        
        demande.setStatut(Demande.STATUT_DUPLICATA_VALIDE);
        demandeRepository.save(demande);
        enregistrerChangementStatut(demande, Demande.STATUT_DUPLICATA_VALIDE);
    }

    @Transactional
    public void rejeterDuplicata(Long duplicataDemandeId) {
        Demande demande = demandeRepository.findById(duplicataDemandeId)
                .orElseThrow(() -> new RuntimeException("Demande de duplicata introuvable"));
        
        if (demande.getStatut() != Demande.STATUT_DUPLICATA_SCANNE) {
            throw new RuntimeException("Le duplicata doit etre scanne avant rejet");
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
