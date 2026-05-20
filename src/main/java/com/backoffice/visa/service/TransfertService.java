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
    private final HistoriquePasseportVisaRepository historiquePasseportVisaRepository;
    private final DemandeService demandeService;

    public TransfertService(DemandeRepository demandeRepository,
                           DemandeTransfertRepository demandeTransfertRepository,
                           CarteResidentRepository carteResidentRepository,
                           TypeDemandeRepository typeDemandeRepository,
                           StatutDemandeRepository statutDemandeRepository,
                           PasseportRepository passeportRepository,
                           VisaTransformableRepository visaTransformableRepository,
                           HistoriquePasseportVisaRepository historiquePasseportVisaRepository,
                           DemandeService demandeService) {
        this.demandeRepository = demandeRepository;
        this.demandeTransfertRepository = demandeTransfertRepository;
        this.carteResidentRepository = carteResidentRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.passeportRepository = passeportRepository;
        this.visaTransformableRepository = visaTransformableRepository;
        this.historiquePasseportVisaRepository = historiquePasseportVisaRepository;
        this.demandeService = demandeService;
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
        System.out.println("Vérification éligibilité Transfert pour Demande ID: " + d.getId());
        
        if (d.getStatut() != Demande.STATUT_TERMINE) {
            System.out.println("-> ÉCHEC : Statut actuel (" + d.getStatut() + ") != TERMINE (2)");
            return false;
        }
        
        Optional<CarteResident> carteOpt = carteResidentRepository.findByDemandeId(d.getId());
        if (carteOpt.isEmpty()) {
            System.out.println("-> ÉCHEC : Aucune CarteResident trouvée pour cette demande");
            return false;
        }
        
        if (carteOpt.get().getDateFin().isBefore(LocalDate.now())) {
            System.out.println("-> ÉCHEC : La carte est expirée (Date fin: " + carteOpt.get().getDateFin() + ")");
            return false;
        }
        
        System.out.println("-> SUCCÈS : Le dossier est éligible");
        return true;
    }

    @Transactional
    public DemandeTransfert creerDemandeTransfert(TransfertFormDTO form) {
        Demande demandeOrigine = demandeRepository.findById(form.getIdDemandeOrigine())
                .orElseThrow(() -> new RuntimeException("Demande d'origine introuvable"));

        if (!estEligibleAuTransfert(demandeOrigine)) {
            throw new RuntimeException("Dossier non éligible au transfert");
        }

        boolean aDejaUneDemandeEnCours = demandeRepository.findByDemandeurId(demandeOrigine.getDemandeur().getId()).stream()
                .anyMatch(d -> d.getStatut() == Demande.STATUT_TRANSFERT_DEMANDE
                        || d.getStatut() == Demande.STATUT_TRANSFERT_SCANNE
                        || d.getStatut() == Demande.STATUT_TRANSFERT_VALIDE);

        if (aDejaUneDemandeEnCours) {
            throw new RuntimeException("Une demande de transfert est deja en cours pour ce demandeur");
        }

        String nouveauNumeroPasseport = normaliserNumero(form.getNouveauNumeroPasseport());
        if (nouveauNumeroPasseport == null || nouveauNumeroPasseport.isBlank()) {
            throw new RuntimeException("Le nouveau numero de passeport est obligatoire");
        }
        if (passeportRepository.existsByNumeroPasseport(nouveauNumeroPasseport)) {
            throw new RuntimeException("Ce numero de passeport est deja utilise");
        }
        if (form.getNouvelleDateDelivrance() == null || form.getNouvelleDateExpiration() == null) {
            throw new RuntimeException("Les dates du nouveau passeport sont obligatoires");
        }
        if (!form.getNouvelleDateExpiration().isAfter(form.getNouvelleDateDelivrance())) {
            throw new RuntimeException("La date d'expiration du nouveau passeport doit etre superieure a la date de delivrance");
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
        detail.setNouveauNumeroPasseport(nouveauNumeroPasseport);
        detail.setNouveauPaysDelivrance(form.getNouveauPaysDelivrance());
        detail.setNouvelleDateDelivrance(form.getNouvelleDateDelivrance());
        detail.setNouvelleDateExpiration(form.getNouvelleDateExpiration());
        
        DemandeTransfert detailEnregistre = demandeTransfertRepository.save(detail);
        demandeService.initialiserPiecesPourUpload(demandeTransfert);
        return detailEnregistre;
    }

    @Transactional
    public void scannerTransfert(Long id) {
        demandeTransfertRepository.findByDemandeId(id)
                .orElseThrow(() -> new RuntimeException("Details du transfert introuvables"));

        demandeService.scannerDossier(id);
    }

    @Transactional
    public void validerTransfert(Long id) {
        Demande d = demandeRepository.findById(id).orElseThrow();
        if (d.getStatut() != Demande.STATUT_TRANSFERT_SCANNE) {
            throw new RuntimeException("Le transfert doit etre scanne avant validation");
        }
        d.setStatut(Demande.STATUT_TRANSFERT_VALIDE);
        demandeRepository.save(d);
        enregistrerChangementStatut(d, Demande.STATUT_TRANSFERT_VALIDE);
    }

    @Transactional
    public void rejeterTransfert(Long id) {
        Demande d = demandeRepository.findById(id).orElseThrow();
        if (d.getStatut() != Demande.STATUT_TRANSFERT_SCANNE) {
            throw new RuntimeException("Le transfert doit etre scanne avant rejet");
        }
        d.setStatut(Demande.STATUT_TRANSFERT_REJETE);
        demandeRepository.save(d);
        enregistrerChangementStatut(d, Demande.STATUT_TRANSFERT_REJETE);
    }

    @Transactional
    public CarteResident emettreTransfert(Long id) {
        Demande demande = demandeRepository.findById(id).orElseThrow(() -> new RuntimeException("Demande introuvable"));
        if (demande.getStatut() != Demande.STATUT_TRANSFERT_VALIDE) {
            throw new RuntimeException("La demande doit etre validee avant emission");
        }
        DemandeTransfert detail = demandeTransfertRepository.findByDemandeId(id)
                .orElseThrow(() -> new RuntimeException("Détails du transfert introuvables"));
        
        // 1. Créer le nouveau passeport du demandeur
        Passeport nouveauPass = new Passeport();
        nouveauPass.setDemandeur(demande.getDemandeur());
        nouveauPass.setNumeroPasseport(detail.getNouveauNumeroPasseport());
        nouveauPass.setPaysDelivrance(detail.getNouveauPaysDelivrance());
        nouveauPass.setDateDelivrance(detail.getNouvelleDateDelivrance());
        nouveauPass.setDateExpiration(detail.getNouvelleDateExpiration());
        nouveauPass = passeportRepository.save(nouveauPass);

        // 2. LOGIQUE D'HISTORIQUE (VOTRE SOLUTION) :
        // On récupère le visa existant
        VisaTransformable visa = demande.getVisaTransformable();
        Passeport ancienPass = visa.getPasseport();

        // On enregistre le mouvement dans la nouvelle table d'historique
        HistoriquePasseportVisa historique = new HistoriquePasseportVisa();
        historique.setVisaTransformable(visa);
        historique.setAncienPasseport(ancienPass);
        historique.setNouveauPasseport(nouveauPass);
        historique.setDateTransfert(LocalDate.now());
        historiquePasseportVisaRepository.save(historique);

        // ON FAIT L'UPDATE sur le visa principal (pour qu'il pointe vers le nouveau passeport)
        // Mais comme on vient d'enregistrer l'histoire juste au-dessus, on ne perd rien !
        visa.setPasseport(nouveauPass);
        visaTransformableRepository.save(visa);

        // 3. Émettre nouvelle carte
        CarteResident nouvelleCarte = new CarteResident();
        nouvelleCarte.setDemande(demande);
        nouvelleCarte.setPasseport(nouveauPass);
        nouvelleCarte.setDateDebut(LocalDate.now());
        nouvelleCarte.setDateFin(detail.getCarteOrigine().getDateFin()); // On garde la même fin (règle métier)
        nouvelleCarte.setReference("CR-TRF-" + demande.getId() + "-" + LocalDate.now().getYear());
        nouvelleCarte = carteResidentRepository.save(nouvelleCarte);

        // Mise à jour de la demande
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

    private String normaliserNumero(String numero) {
        if (numero == null) {
            return null;
        }
        return numero.trim().toUpperCase();
    }
}
