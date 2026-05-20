package com.backoffice.visa.service;

import com.backoffice.visa.dto.DemandeFormDTO;
import com.backoffice.visa.entity.*;
import com.backoffice.visa.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final SignatureService signatureService;

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
            VisaTransformableRepository visaTransformableRepository,
            SignatureService signatureService) {
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
        this.signatureService = signatureService;
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

        // 4.5 Créer le Visa initial (si fourni)
        if (form.getReferenceVisaOrigine() != null && !form.getReferenceVisaOrigine().isBlank()) {
            if (form.getDateDebutVisaOrigine() == null || form.getDateFinVisaOrigine() == null) {
                throw new RuntimeException("Les dates de début et de fin du visa d'origine sont obligatoires");
            }
            if (form.getDateDebutVisaOrigine().isAfter(form.getDateFinVisaOrigine())) {
                throw new RuntimeException("La date de début du visa d'origine doit être antérieure ou égale à la date de fin");
            }
            Visa visaInitial = new Visa();
            visaInitial.setDemande(demande);
            visaInitial.setPasseport(passeport);
            visaInitial.setReference(form.getReferenceVisaOrigine());
            visaInitial.setDateDebut(form.getDateDebutVisaOrigine());
            visaInitial.setDateFin(form.getDateFinVisaOrigine());
            visaRepository.save(visaInitial);
        }

        // 5. Enregistrer les pièces communes
        creerPiecesPourDemande(demande, form.getPiecesCommunesPresentes(), form.getPiecesSpecifiquesPresentes());

        // 6. Enregistrer les pièces spécifiques au type de visa
        // 7. Vérifier que toutes les pièces obligatoires sont fournies → "Dossier créé"
        boolean toutesObligatoiresFournies = verifierPiecesObligatoires(demande.getId());
        if (!toutesObligatoiresFournies) {
            throw new RuntimeException("Toutes les pièces obligatoires doivent être fournies pour créer le dossier");
        }

        // Statut = Dossier créé (1)
        enregistrerChangementStatut(demande, 1);

        return demande;
    }

    /**
     * Initialise les pieces a importer pour une demande creee par un workflow annexe
     * (duplicata, transfert). Les fichiers seront importes ensuite depuis upload.html.
     */
    @Transactional
    public void initialiserPiecesPourUpload(Demande demande) {
        creerPiecesPourDemande(demande, Collections.emptyList(), Collections.emptyList());
    }

    @Transactional
    public void initialiserPiecesPourUpload(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        initialiserPiecesPourUpload(demande);
    }

    private void creerPiecesPourDemande(Demande demande, List<Long> piecesCommunesPresentes, List<Long> piecesSpecifiquesPresentes) {
        Set<Long> communesPresentes = piecesCommunesPresentes == null
                ? Collections.emptySet()
                : new HashSet<>(piecesCommunesPresentes);
        Set<Long> specifiquesPresentes = piecesSpecifiquesPresentes == null
                ? Collections.emptySet()
                : new HashSet<>(piecesSpecifiquesPresentes);

        Set<Long> communesExistantes = pieceDemandeRepository.findByDemandeId(demande.getId()).stream()
                .map(p -> p.getTypePieceCommune().getId())
                .collect(Collectors.toSet());
        Set<Long> specifiquesExistantes = pieceDemandeSpecifiqueRepository.findByDemandeId(demande.getId()).stream()
                .map(p -> p.getTypePieceSpecifique().getId())
                .collect(Collectors.toSet());

        List<TypePieceCommune> toutesCommunes = typePieceCommuneRepository.findAll();
        for (TypePieceCommune type : toutesCommunes) {
            if (communesExistantes.contains(type.getId())) {
                continue;
            }
            PieceDemande piece = new PieceDemande();
            piece.setDemande(demande);
            piece.setTypePieceCommune(type);
            piece.setPresente(communesPresentes.contains(type.getId()));
            pieceDemandeRepository.save(piece);
        }

        List<TypePieceSpecifique> specifiques = typePieceSpecifiqueRepository.findByTypeVisaId(demande.getTypeVisa().getId());
        for (TypePieceSpecifique type : specifiques) {
            if (specifiquesExistantes.contains(type.getId())) {
                continue;
            }
            PieceDemandeSpecifique piece = new PieceDemandeSpecifique();
            piece.setDemande(demande);
            piece.setTypePieceSpecifique(type);
            piece.setPresente(specifiquesPresentes.contains(type.getId()));
            pieceDemandeSpecifiqueRepository.save(piece);
        }
    }

    /**
     * Vérifie si toutes les pièces obligatoires (communes + spécifiques) sont fournies
     */
    private boolean verifierPiecesObligatoires(Long demandeId) {
        List<PieceDemande> piecesCommunes = pieceDemandeRepository.findByDemandeId(demandeId);
        for (PieceDemande p : piecesCommunes) {
            if (Boolean.TRUE.equals(p.getTypePieceCommune().getObligatoire()) && !Boolean.TRUE.equals(p.getPresente())) {
                return false;
            }
        }
        List<PieceDemandeSpecifique> piecesSpec = pieceDemandeSpecifiqueRepository.findByDemandeId(demandeId);
        for (PieceDemandeSpecifique p : piecesSpec) {
            if (Boolean.TRUE.equals(p.getTypePieceSpecifique().getObligatoire()) && !Boolean.TRUE.equals(p.getPresente())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scanner le dossier (passe de "Dossier créé" à "Scan terminé")
     * Cette étape est possible une fois que toutes les pièces sont importées.
     */
    @Transactional
    public Demande scannerDossier(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        int nouveauStatut;
        if (demande.getStatut() == Demande.STATUT_PHOTO_PRISE) {
            nouveauStatut = Demande.STATUT_SCANNE;
        } else if (demande.getStatut() == Demande.STATUT_DUPLICATA_SIGNATURE_TERMINEE) {
            nouveauStatut = Demande.STATUT_DUPLICATA_SCANNE;
        } else if (demande.getStatut() == Demande.STATUT_TRANSFERT_SIGNATURE_TERMINEE) {
            nouveauStatut = Demande.STATUT_TRANSFERT_SCANNE;
        } else {
            throw new RuntimeException("Ce dossier ne peut pas etre scanne depuis le statut actuel : " + demande.getStatutLibelle());
        }

        // On vérifie une dernière fois que toutes les pièces obligatoires sont présentes avec un fichier
        initialiserPiecesPourUpload(demande);

        if (!signatureService.captureExiste(demandeId)) {
            throw new RuntimeException("La photo et la signature doivent etre enregistrees avant de scanner.");
        }

        if (!verifierToutesPiecesImportees(demandeId)) {
            throw new RuntimeException("Toutes les pièces obligatoires doivent avoir un fichier importé avant de scanner.");
        }

        demande.setStatut(nouveauStatut);
        enregistrerChangementStatut(demande, nouveauStatut);
        return demandeRepository.save(demande);
    }

    /**
     * Vérifie si toutes les pièces obligatoires ont effectivement un fichier uploadé
     */
    public boolean verifierToutesPiecesImportees(Long demandeId) {
        List<PieceDemande> piecesCommunes = pieceDemandeRepository.findByDemandeId(demandeId);
        for (PieceDemande p : piecesCommunes) {
            if (p.getFichierPath() == null || p.getFichierPath().isEmpty()) {
                return false;
            }
        }
        List<PieceDemandeSpecifique> piecesSpec = pieceDemandeSpecifiqueRepository.findByDemandeId(demandeId);
        for (PieceDemandeSpecifique p : piecesSpec) {
            if (p.getFichierPath() == null || p.getFichierPath().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Terminer le dossier (passe de "Scan terminé" à "Dossier terminé")
     */
    @Transactional
    public Demande terminerDossier(Long demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() != Demande.STATUT_SCANNE) {
            throw new RuntimeException("Le dossier doit être scanné avant d'être terminé. Statut actuel : " + demande.getStatutLibelle());
        }

        demande.setStatut(Demande.STATUT_TERMINE); // Nouveau statut 3
        demande.setDateTraitement(LocalDate.now());
        enregistrerChangementStatut(demande, Demande.STATUT_TERMINE);
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

    public List<Demande> searchDemandes(String query) {
        if (query == null || query.isBlank()) {
            return getAllDemandes();
        }
        return demandeRepository.searchByIdOrPasseport(query.trim());
    }

    public List<StatutDemande> getHistorique(Long demandeId) {
        return statutDemandeRepository.findByDemandeIdOrderByIdDesc(demandeId);
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
     * Créer une demande déjà terminée (pour le cas de duplicata sans données antérieures)
     */
    @Transactional
    public Demande creerDossierTermine(DemandeFormDTO form) {
        // On réutilise la création de base
        Demande demande = creerDemande(form);
        
        // Pour un dossier antérieur, on simule que le scan est déjà fait
        demande.setStatut(Demande.STATUT_SCANNE);
        enregistrerChangementStatut(demande, Demande.STATUT_SCANNE);
        demande = demandeRepository.save(demande);
        
        // On la termine immédiatement
        return terminerDossier(demande.getId());
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
