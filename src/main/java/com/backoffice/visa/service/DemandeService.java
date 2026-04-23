package com.backoffice.visa.service;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.*;
import com.backoffice.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final VisaTransformableRepository visaTransformableRepository;

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
            CarteResidentRepository carteResidentRepository,
            VisaTransformableRepository visaTransformableRepository) {
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
        this.visaTransformableRepository = visaTransformableRepository;
    }

    /**
     * Créer une nouvelle demande complète (demandeur + passeport + demande + pièces)
     * Statut initial : Brouillon (1)
     */
    @Transactional
    public Demande creerDemande(DemandeFormDTO form) {
        String numeroPasseportNormalise = normaliserNumero(form.getNumeroPasseport());
        String numeroVisaNormalise = normaliserNumero(form.getNumeroReferenceVisa());

        if (numeroPasseportDejaUtilise(numeroPasseportNormalise)) {
            throw new RuntimeException("Ce numéro de passeport est déjà utilisé");
        }
        if (numeroVisaDejaUtilise(numeroVisaNormalise)) {
            throw new RuntimeException("Ce numéro de référence du visa est déjà utilisé");
        }
        if (form.getDateDelivrancePasseport() == null || form.getDateExpirationPasseport() == null) {
            throw new RuntimeException("Les dates du passeport sont obligatoires");
        }
        if (!form.getDateExpirationPasseport().isAfter(form.getDateDelivrancePasseport())) {
            throw new RuntimeException("La date d'expiration du passeport doit être strictement supérieure à la date de délivrance");
        }

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
        passeport.setNumeroPasseport(numeroPasseportNormalise);
        passeport.setDateDelivrance(form.getDateDelivrancePasseport());
        passeport.setDateExpiration(form.getDateExpirationPasseport());
        passeport.setPaysDelivrance(form.getPaysDelivrance());
        passeport = passeportRepository.save(passeport);

        // 3. Créer le visa transformable
        if (numeroVisaNormalise == null || numeroVisaNormalise.isBlank()) {
            throw new RuntimeException("Le numéro de référence du visa est obligatoire");
        }
        if (form.getLieuVisa() == null || form.getDateDebutVisa() == null || form.getDateFinVisa() == null) {
            throw new RuntimeException("Les informations du visa (lieu, dates) sont obligatoires");
        }
        if (form.getDateDebutVisa().isAfter(form.getDateFinVisa())) {
            throw new RuntimeException("La date de début du visa doit être antérieure ou égale à la date de fin");
        }

        LocalDate dateDemande = LocalDate.now();
        if (dateDemande.isBefore(form.getDateDebutVisa()) || dateDemande.isAfter(form.getDateFinVisa())) {
            throw new RuntimeException("La date de la demande doit être comprise dans la période du visa transformable");
        }

        VisaTransformable visaTransformable = new VisaTransformable();
        visaTransformable.setDemandeur(demandeur);
        visaTransformable.setPasseport(passeport);
        visaTransformable.setNumeroReference(numeroVisaNormalise);
        visaTransformable.setLieu(form.getLieuVisa());
        visaTransformable.setDateDebut(form.getDateDebutVisa());
        visaTransformable.setDateFin(form.getDateFinVisa());
        visaTransformable = visaTransformableRepository.save(visaTransformable);

        // 4. Créer la demande (statut = Dossier créé)
        TypeVisa typeVisa = typeVisaRepository.findById(form.getIdTypeVisa())
                .orElseThrow(() -> new RuntimeException("Type de visa introuvable"));
        TypeDemande typeDemande = typeDemandeRepository.findById(form.getIdTypeDemande())
                .orElseThrow(() -> new RuntimeException("Type de demande introuvable"));

        Demande demande = new Demande();
        demande.setDemandeur(demandeur);
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemande);
        demande.setDateDemande(dateDemande);
        demande.setStatut(1);
        demande.setVisaTransformable(visaTransformable);
        demande = demandeRepository.save(demande);

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

        // 7. Déterminer le statut selon la complétude des pièces obligatoires
        boolean toutesObligatoiresFournies = verifierPiecesObligatoires(demande.getId());
        int statutFinal = toutesObligatoiresFournies ? Demande.STATUT_CREATION : Demande.STATUT_INCOMPLET;
        demande.setStatut(statutFinal);
        demande = demandeRepository.save(demande);
        enregistrerChangementStatut(demande, statutFinal);

        return demande;
    }

    /**
     * Vérifie si toutes les pièces (obligatoires ET facultatives) sont fournies.
     * → TRUE  = toutes présentes → "Dossier créé"
     * → FALSE = au moins une manquante → "Dossier"
     */
    private boolean verifierPiecesObligatoires(Long demandeId) {
        List<PieceDemande> piecesCommunes = pieceDemandeRepository.findByDemandeId(demandeId);
        for (PieceDemande p : piecesCommunes) {
            if (!p.getPresente()) {
                return false;
            }
        }
        List<PieceDemandeSpecifique> piecesSpec = pieceDemandeSpecifiqueRepository.findByDemandeId(demandeId);
        for (PieceDemandeSpecifique p : piecesSpec) {
            if (!p.getPresente()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scanner le dossier (passe de "Dossier créé" à "Dossier scanné").
     */
    @Transactional
    public Demande scannerDossier(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() != Demande.STATUT_CREATION) {
            throw new RuntimeException("Seul un dossier créé peut être scanné. Statut actuel : " + demande.getStatutLibelle());
        }

        demande.setStatut(Demande.STATUT_SCANNE);
        enregistrerChangementStatut(demande, Demande.STATUT_SCANNE);
        return demandeRepository.save(demande);
    }

    /**
     * Approuver le dossier (passe de "Dossier scanné" à "Dossier approuvé").
     * Crée automatiquement la carte de résident.
     */
    @Transactional
    public Demande approuverDossier(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() != Demande.STATUT_SCANNE) {
            throw new RuntimeException("Seul un dossier scanné peut être approuvé. Statut actuel : " + demande.getStatutLibelle());
        }

        demande.setStatut(Demande.STATUT_APPROUVE);
        demande.setDateTraitement(LocalDate.now());
        enregistrerChangementStatut(demande, Demande.STATUT_APPROUVE);
        demande = demandeRepository.save(demande);

        // Créer automatiquement la carte de résident
        CarteResident cr = new CarteResident();
        cr.setDemande(demande);
        LocalDate dateFinCR = LocalDate.now().plusYears(10);
        
        if (demande.getVisaTransformable() != null && demande.getVisaTransformable().getPasseport() != null) {
            cr.setPasseport(demande.getVisaTransformable().getPasseport());
            LocalDate dateFinPasseport = demande.getVisaTransformable().getPasseport().getDateExpiration();
            if (dateFinPasseport != null && dateFinCR.isAfter(dateFinPasseport)) {
                dateFinCR = dateFinPasseport;
            }
        } else {
            throw new RuntimeException("Impossible de créer la carte de résident : Passeport introuvable");
        }
        
        cr.setDateDebut(LocalDate.now());
        cr.setDateFin(dateFinCR);
        cr.setReference("CR-" + demande.getId() + "-" + LocalDate.now().getYear());
        carteResidentRepository.save(cr);

        return demande;
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

    public boolean numeroPasseportDejaUtilise(String numeroPasseport) {
        String numeroNormalise = normaliserNumero(numeroPasseport);
        if (numeroNormalise == null || numeroNormalise.isBlank()) {
            return false;
        }
        return passeportRepository.existsByNumeroPasseport(numeroNormalise);
    }

    public boolean numeroVisaDejaUtilise(String numeroReferenceVisa) {
        String numeroNormalise = normaliserNumero(numeroReferenceVisa);
        if (numeroNormalise == null || numeroNormalise.isBlank()) {
            return false;
        }
        return demandeRepository.existsByVisaTransformableNumeroReference(numeroNormalise);
    }

    /**
     * Créer une demande déjà approuvée (cas sans données antérieures)
     */
    @Transactional
    public Demande creerDossierTermine(DemandeFormDTO form) {
        Demande demande = creerDemande(form);

        if (demande.estDossierIncomplet()) {
            throw new RuntimeException("Toutes les pièces obligatoires doivent être fournies pour finaliser le dossier");
        }

        demande = scannerDossier(demande.getId());
        return approuverDossier(demande.getId());
    }

    public Map<String, Object> rechercherReferenceTransfert(String numeroReference) {
        String numeroNormalise = normaliserNumero(numeroReference);
        Optional<VisaTransformable> optionalVisa = visaTransformableRepository.findByNumeroReference(numeroNormalise);

        Map<String, Object> response = new HashMap<>();
        response.put("existe", optionalVisa.isPresent());
        if (optionalVisa.isEmpty()) {
            return response;
        }

        VisaTransformable visa = optionalVisa.get();
        Demandeur demandeur = visa.getDemandeur();
        Passeport passeport = visa.getPasseport();

        response.put("numeroReferenceVisa", visa.getNumeroReference());
        response.put("lieuVisa", visa.getLieu());
        response.put("dateDebutVisa", visa.getDateDebut());
        response.put("dateFinVisa", visa.getDateFin());

        if (demandeur != null) {
            response.put("nom", demandeur.getNom());
            response.put("prenom", demandeur.getPrenom());
            response.put("dateNaissance", demandeur.getDateNaissance());
            response.put("lieuNaissance", demandeur.getLieuNaissance());
            response.put("telephone", demandeur.getTelephone());
            response.put("email", demandeur.getEmail());
            response.put("adresse", demandeur.getAdresse());
            response.put("idNationalite", demandeur.getNationalite() != null ? demandeur.getNationalite().getId() : null);
            response.put("idSituationFamiliale", demandeur.getSituationFamiliale() != null ? demandeur.getSituationFamiliale().getId() : null);
        }

        if (passeport != null) {
            response.put("numeroPasseport", passeport.getNumeroPasseport());
            response.put("dateDelivrancePasseport", passeport.getDateDelivrance());
            response.put("dateExpirationPasseport", passeport.getDateExpiration());
            response.put("paysDelivrance", passeport.getPaysDelivrance());
        }

        demandeRepository.findTopByVisaTransformableIdOrderByDateDemandeDesc(visa.getId())
                .ifPresent(d -> {
                    response.put("idTypeVisa", d.getTypeVisa() != null ? d.getTypeVisa().getId() : null);
                    response.put("idTypeDemande", d.getTypeDemande() != null ? d.getTypeDemande().getId() : null);
                });

        return response;
    }

    @Transactional
    public Demande creerTransfertDepuisReference(DemandeFormDTO form) {
        String numeroNormalise = normaliserNumero(form.getNumeroReferenceVisa());
        VisaTransformable visa = visaTransformableRepository.findByNumeroReference(numeroNormalise)
                .orElseThrow(() -> new RuntimeException("Référence visa introuvable"));

        if (form.getIdTypeVisa() == null) {
            throw new RuntimeException("Le type de visa est obligatoire");
        }

        TypeVisa typeVisa = typeVisaRepository.findById(form.getIdTypeVisa())
                .orElseThrow(() -> new RuntimeException("Type de visa introuvable"));

        TypeDemande typeDemandeTransfert = typeDemandeRepository.findByLibelleIgnoreCase("Transfert de visa")
                .orElseThrow(() -> new RuntimeException("Type de demande 'Transfert de visa' introuvable"));

        Demande demande = new Demande();
        demande.setDemandeur(visa.getDemandeur());
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemandeTransfert);
        demande.setDateDemande(LocalDate.now());
        demande.setStatut(Demande.STATUT_CREATION);
        demande.setVisaTransformable(visa);
        demande = demandeRepository.save(demande);

        enregistrerPieces(demande, form);

        boolean toutesObligatoiresFournies = verifierPiecesObligatoires(demande.getId());
        int statutFinalTransfert = toutesObligatoiresFournies ? Demande.STATUT_CREATION : Demande.STATUT_INCOMPLET;
        demande.setStatut(statutFinalTransfert);
        demande = demandeRepository.save(demande);
        enregistrerChangementStatut(demande, statutFinalTransfert);
        return demande;
    }

    @Transactional
    public Demande creerTransfertSansReference(DemandeFormDTO form) {
        TypeDemande typeDemandeTransfert = typeDemandeRepository.findByLibelleIgnoreCase("Transfert de visa")
                .orElseThrow(() -> new RuntimeException("Type de demande 'Transfert de visa' introuvable"));
        form.setIdTypeDemande(typeDemandeTransfert.getId());
        return creerDossierTermine(form);
    }

    private void enregistrerPieces(Demande demande, DemandeFormDTO form) {
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
    }

    private String normaliserNumero(String numero) {
        if (numero == null) {
            return null;
        }
        return numero.trim().toUpperCase();
    }

    private void enregistrerChangementStatut(Demande demande, int statut) {
        StatutDemande sd = new StatutDemande();
        sd.setDemande(demande);
        sd.setStatut(statut);
        sd.setDateChangementStatut(LocalDate.now());
        statutDemandeRepository.save(sd);
    }
}
