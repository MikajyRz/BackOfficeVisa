package com.backoffice.visa.service;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.*;
import com.backoffice.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DemandeService {

    private final DemandeurRepository demandeurRepository;
    private final PasseportRepository passeportRepository;
    private final DemandeRepository demandeRepository;
    private final StatutDemandeRepository statutDemandeRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final TypePieceCommuneRepository typePieceCommuneRepository;
    private final TypePieceSpecifiqueRepository typePieceSpecifiqueRepository;
    private final PieceDemandeRepository pieceDemandeRepository;
    private final PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository;
    private final VisaRepository visaRepository;
    private final CarteResidentRepository carteResidentRepository;

    public DemandeService(
            DemandeurRepository demandeurRepository,
            PasseportRepository passeportRepository,
            DemandeRepository demandeRepository,
            StatutDemandeRepository statutDemandeRepository,
            TypeVisaRepository typeVisaRepository,
            TypeDemandeRepository typeDemandeRepository,
            NationaliteRepository nationaliteRepository,
            SituationFamilialeRepository situationFamilialeRepository,
            TypePieceCommuneRepository typePieceCommuneRepository,
            TypePieceSpecifiqueRepository typePieceSpecifiqueRepository,
            PieceDemandeRepository pieceDemandeRepository,
            PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository,
            VisaRepository visaRepository,
            CarteResidentRepository carteResidentRepository) {
        this.demandeurRepository = demandeurRepository;
        this.passeportRepository = passeportRepository;
        this.demandeRepository = demandeRepository;
        this.statutDemandeRepository = statutDemandeRepository;
        this.typeVisaRepository = typeVisaRepository;
        this.typeDemandeRepository = typeDemandeRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
        this.typePieceCommuneRepository = typePieceCommuneRepository;
        this.typePieceSpecifiqueRepository = typePieceSpecifiqueRepository;
        this.pieceDemandeRepository = pieceDemandeRepository;
        this.pieceDemandeSpecifiqueRepository = pieceDemandeSpecifiqueRepository;
        this.visaRepository = visaRepository;
        this.carteResidentRepository = carteResidentRepository;
    }

    /**
     * Créer une nouvelle demande complète (demandeur + passeport + demande + pièces)
     * Statut initial : Brouillon (1)
     */
    @Transactional
    public Demande creerDemande(DemandeFormDTO form) {
        // 1. Créer le demandeur
        Nationalite nationalite = nationaliteRepository.findById(form.getIdNationalite())
                .orElseThrow(() -> new RuntimeException("Nationalité introuvable"));
        SituationFamiliale situation = situationFamilialeRepository.findById(form.getIdSituationFamiliale())
                .orElseThrow(() -> new RuntimeException("Situation familiale introuvable"));

        Demandeur demandeur = new Demandeur();
        demandeur.setNom(form.getNom());
        demandeur.setPrenom(form.getPrenom());
        demandeur.setDateNaissance(form.getDateNaissance());
        demandeur.setLieuNaissance(form.getLieuNaissance());
        demandeur.setTelephone(form.getTelephone());
        demandeur.setEmail(form.getEmail());
        demandeur.setAdresse(form.getAdresse());
        demandeur.setNationalite(nationalite);
        demandeur.setSituationFamiliale(situation);
        demandeur = demandeurRepository.save(demandeur);

        // 2. Créer le passeport
        Passeport passeport = new Passeport();
        passeport.setDemandeur(demandeur);
        passeport.setNumeroPasseport(form.getNumeroPasseport());
        passeport.setDateDelivrance(form.getDateDelivrancePasseport());
        passeport.setDateExpiration(form.getDateExpirationPasseport());
        passeport.setPaysDelivrance(form.getPaysDelivrance());
        passeport = passeportRepository.save(passeport);

        // 3. Créer la demande (statut = Brouillon)
        TypeVisa typeVisa = typeVisaRepository.findById(form.getIdTypeVisa())
                .orElseThrow(() -> new RuntimeException("Type de visa introuvable"));
        TypeDemande typeDemande = typeDemandeRepository.findById(form.getIdTypeDemande())
                .orElseThrow(() -> new RuntimeException("Type de demande introuvable"));

        Demande demande = new Demande();
        demande.setDemandeur(demandeur);
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemande);
        demande.setDateDemande(LocalDate.now());
        demande.setStatut(1); // Brouillon
        demande = demandeRepository.save(demande);

        // 4. Enregistrer l'historique du statut
        enregistrerChangementStatut(demande, 1);

        // 5. Enregistrer les pièces communes
        List<TypePieceCommune> toutesCommunes = typePieceCommuneRepository.findAll();
        for (TypePieceCommune type : toutesCommunes) {
            PieceDemande piece = new PieceDemande();
            piece.setDemande(demande);
            piece.setTypePieceCommune(type);
            boolean presente = form.getPiecesCommunesPresentes() != null
                    && form.getPiecesCommunesPresentes().contains(type.getId());
            piece.setPresente(presente);
            pieceDemandeRepository.save(piece);
        }

        // 6. Enregistrer les pièces spécifiques au type de visa
        List<TypePieceSpecifique> specifiques = typePieceSpecifiqueRepository.findByTypeVisaId(form.getIdTypeVisa());
        for (TypePieceSpecifique type : specifiques) {
            PieceDemandeSpecifique piece = new PieceDemandeSpecifique();
            piece.setDemande(demande);
            piece.setTypePieceSpecifique(type);
            boolean presente = form.getPiecesSpecifiquesPresentes() != null
                    && form.getPiecesSpecifiquesPresentes().contains(type.getId());
            piece.setPresente(presente);
            pieceDemandeSpecifiqueRepository.save(piece);
        }

        return demande;
    }

    /**
     * Soumettre une demande (passe de Brouillon à Soumise)
     */
    @Transactional
    public Demande soumettreDemande(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() != 1) {
            throw new RuntimeException("Seule une demande en brouillon peut être soumise");
        }

        // Vérifier que toutes les pièces obligatoires communes sont présentes
        List<PieceDemande> piecesCommunes = pieceDemandeRepository.findByDemandeId(demandeId);
        for (PieceDemande p : piecesCommunes) {
            if (p.getTypePieceCommune().getObligatoire() && !p.getPresente()) {
                throw new RuntimeException("Pièce obligatoire manquante : " + p.getTypePieceCommune().getLibelle());
            }
        }

        // Vérifier que toutes les pièces obligatoires spécifiques sont présentes
        List<PieceDemandeSpecifique> piecesSpec = pieceDemandeSpecifiqueRepository.findByDemandeId(demandeId);
        for (PieceDemandeSpecifique p : piecesSpec) {
            if (p.getTypePieceSpecifique().getObligatoire() && !p.getPresente()) {
                throw new RuntimeException("Pièce spécifique obligatoire manquante : " + p.getTypePieceSpecifique().getLibelle());
            }
        }

        demande.setStatut(2); // Soumise
        enregistrerChangementStatut(demande, 2);
        return demandeRepository.save(demande);
    }

    /**
     * Valider une demande et générer le visa + carte de résident
     */
    @Transactional
    public Demande validerDemande(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() != 2 && demande.getStatut() != 3) {
            throw new RuntimeException("La demande doit être soumise ou en cours de traitement pour être validée");
        }

        Passeport passeport = passeportRepository.findByDemandeurId(demande.getDemandeur().getId())
                .orElseThrow(() -> new RuntimeException("Passeport introuvable pour ce demandeur"));

        // Passer en validée
        demande.setStatut(4);
        demande.setDateTraitement(LocalDate.now());
        enregistrerChangementStatut(demande, 4);
        demande = demandeRepository.save(demande);

        // Créer le visa
        Visa visa = new Visa();
        visa.setDemande(demande);
        visa.setPasseport(passeport);
        visa.setReference("VISA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        visa.setDateDebut(LocalDate.now());
        visa.setDateFin(LocalDate.now().plusYears(2));
        visaRepository.save(visa);

        // Créer la carte de résident
        CarteResident carte = new CarteResident();
        carte.setDemande(demande);
        carte.setPasseport(passeport);
        carte.setReference("CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        carte.setDateDebut(LocalDate.now());
        carte.setDateFin(LocalDate.now().plusYears(2));
        carteResidentRepository.save(carte);

        return demande;
    }

    /**
     * Rejeter une demande
     */
    @Transactional
    public Demande rejeterDemande(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        demande.setStatut(5); // Rejetée
        demande.setDateTraitement(LocalDate.now());
        enregistrerChangementStatut(demande, 5);
        return demandeRepository.save(demande);
    }

    /**
     * Récupérer une demande par ID
     */
    public Demande getDemandeById(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
    }

    /**
     * Lister toutes les demandes
     */
    public List<Demande> getAllDemandes() {
        return demandeRepository.findAll();
    }

    private void enregistrerChangementStatut(Demande demande, int statut) {
        StatutDemande sd = new StatutDemande();
        sd.setDemande(demande);
        sd.setStatut(statut);
        sd.setDateChangementStatut(LocalDate.now());
        statutDemandeRepository.save(sd);
    }
}
