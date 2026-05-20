import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

const getBadgeClass = (statut) => {
  switch (statut) {
    case 1:  return 'badge-cree';
    case 2:  return 'badge-photo';
    case 3:  return 'badge-scanne';
    case 4:  return 'badge-termine';
    case 10: return 'badge-cree';
    case 11: return 'badge-photo';
    case 12: return 'badge-duplicata-scanne';
    case 13: return 'badge-termine';
    case 14: return 'badge-no';
    case 15: return 'badge-termine';
    case 20: return 'badge-cree';
    case 21: return 'badge-photo';
    case 22: return 'badge-transfert-scanne';
    case 23: return 'badge-termine';
    case 24: return 'badge-no';
    case 25: return 'badge-termine';
    default: return '';
  }
};

const getStatutLibelle = (statut) => {
  switch (statut) {
    case 1:  return 'Dossier créé';
    case 2:  return 'Photo prise';
    case 3:  return 'Dossier scanné';
    case 4:  return 'Dossier terminé';
    case 10: return 'Duplicata demandé';
    case 11: return 'Duplicata photo prise';
    case 12: return 'Duplicata scanné';
    case 13: return 'Duplicata validé';
    case 14: return 'Duplicata rejeté';
    case 15: return 'Duplicata émis';
    case 20: return 'Transfert demandé';
    case 21: return 'Transfert photo prise';
    case 22: return 'Transfert scanné';
    case 23: return 'Transfert validé';
    case 24: return 'Transfert rejeté';
    case 25: return 'Transfert émis';
    default: return `Inconnu (${statut})`;
  }
};

export default function DetailPage() {
  const { id } = useParams();
  const [demande, setDemande]         = useState(null);
  const [historiques, setHistoriques] = useState([]);
  const [loading, setLoading]         = useState(true);
  const [alert, setAlert]             = useState(null);
  const [editMode, setEditMode]       = useState(false);
  const [editData, setEditData]       = useState({});
  const [saving, setSaving]           = useState(false);
  const [pdfModal, setPdfModal]       = useState({ open: false, url: '', title: '' });

  const showAlert = (msg, type = 'success') => {
    setAlert({ msg, type });
    setTimeout(() => setAlert(null), 4000);
  };

  const chargerDetail = async () => {
    try {
      setLoading(true);
      const [demandeRes, histoRes] = await Promise.all([
        axios.get(`/api/demandes/${id}`),
        axios.get(`/api/demandes/${id}/historiques`),
      ]);
      setDemande(demandeRes.data);
      setHistoriques(histoRes.data);
    } catch {
      showAlert('Erreur lors du chargement', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { chargerDetail(); }, [id]);

  // --- Actions ---
  const scannerDossier = async () => {
    if (!confirm('Confirmer que toutes les pièces sont scannées ?')) return;
    try {
      await axios.put(`/api/demandes/${id}/scanner`);
      showAlert('Dossier marqué comme scanné.');
      chargerDetail();
    } catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const terminerDossier = async () => {
    if (!confirm('Terminer définitivement le dossier ?')) return;
    try {
      await axios.put(`/api/demandes/${id}/terminer`);
      showAlert('Dossier terminé.');
      chargerDetail();
    } catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const validerDuplicata = async () => {
    try { await axios.put(`/api/cartes-resident/duplicata/${id}/valider`); showAlert('Duplicata validé.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const rejeterDuplicata = async () => {
    try { await axios.put(`/api/cartes-resident/duplicata/${id}/rejeter`); showAlert('Duplicata rejeté.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const emettreDuplicata = async () => {
    try { await axios.put(`/api/cartes-resident/duplicata/${id}/emettre`); showAlert('Duplicata émis.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const validerTransfert = async () => {
    try { await axios.put(`/api/transferts/${id}/valider`); showAlert('Transfert validé.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const rejeterTransfert = async () => {
    try { await axios.put(`/api/transferts/${id}/rejeter`); showAlert('Transfert rejeté.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };
  const emettreTransfert = async () => {
    try { await axios.put(`/api/transferts/${id}/emettre`); showAlert('Transfert émis.'); chargerDetail(); }
    catch (e) { showAlert(e.response?.data?.error || 'Erreur', 'error'); }
  };

  // --- Edition demandeur ---
  const ouvrirEdition = () => {
    const dem = demande?.demandeur || {};
    setEditData({
      nom:           dem.nom || '',
      prenom:        dem.prenom || '',
      dateNaissance: dem.dateNaissance || '',
      lieuNaissance: dem.lieuNaissance || '',
      telephone:     dem.telephone || '',
      email:         dem.email || '',
      adresse:       dem.adresse || '',
    });
    setEditMode(true);
  };
  const enregistrerDemandeur = async () => {
    setSaving(true);
    try {
      await axios.put(`/api/demandes/${id}/demandeur`, editData);
      showAlert('Informations mises à jour.');
      setEditMode(false);
      chargerDetail();
    } catch (e) {
      showAlert(e.response?.data?.error || 'Erreur', 'error');
    } finally { setSaving(false); }
  };

  // --- Modal PDF ---
  const ouvrirApercuPieces = () =>
    setPdfModal({ open: true, url: `/api/demandes/${id}/apercu-pieces`, title: 'Aperçu des pièces justificatives' });
  const ouvrirAccuseReception = () =>
    setPdfModal({ open: true, url: `/api/demandes/${id}/accuse-reception`, title: `Accusé de réception — ${id}` });
  const fermerModal = () => setPdfModal({ open: false, url: '', title: '' });

  // ──────────────────────────────────────────────────────
  if (loading) return (
    <>
      <div className="header"><h1>Back Office Visa — Madagascar</h1></div>
      <div className="container" style={{ textAlign: 'center', marginTop: '50px', color: '#555' }}>Chargement…</div>
    </>
  );

  if (!demande) return (
    <>
      <div className="header"><h1>Back Office Visa — Madagascar</h1></div>
      <div className="container">
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <p style={{ color: '#e74c3c', fontWeight: 600 }}>Demande introuvable.</p>
          <Link to="/" className="btn btn-secondary" style={{ marginTop: '20px' }}>Retour</Link>
        </div>
      </div>
    </>
  );

  const d = demande;
  const passeport = d.visaTransformable?.passeport;
  const statut = d.statut;

  return (
    <>
      <div className="header"><h1>Back Office Visa — Madagascar</h1></div>

      <div className="container">

        {/* Alert */}
        {alert && <div className={`alert alert-${alert.type}`}>{alert.msg}</div>}

        {/* Modal PDF */}
        {pdfModal.open && (
          <div className="pdf-modal-overlay" onClick={fermerModal}>
            <div className="pdf-modal-box" onClick={e => e.stopPropagation()}>
              <div className="pdf-modal-header">
                <span>{pdfModal.title}</span>
                <button className="pdf-modal-close" onClick={fermerModal}>✕</button>
              </div>
              <iframe src={pdfModal.url} title="PDF" className="pdf-modal-frame" />
            </div>
          </div>
        )}

        {/* Top Bar */}
        <div className="top-bar">
          <h2>Demande #{d.id}</h2>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <button className="btn btn-orange" onClick={ouvrirApercuPieces}>Aperçu pièces</button>
            {statut >= 3 && statut <= 4 && (
              <button className="btn btn-purple" onClick={ouvrirAccuseReception}>Accusé de réception</button>
            )}
            <Link to="/" className="btn btn-secondary">Retour</Link>
          </div>
        </div>

        {/* Statut + Actions */}
        <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px' }}>
          <div>
            <div className="info-label">STATUT ACTUEL</div>
            <span className={`badge ${getBadgeClass(statut)}`} style={{ fontSize: '0.95em' }}>
              {d.statutLibelle || getStatutLibelle(statut)}
            </span>
          </div>
          <div>
            <div className="info-label">DATE DE DEMANDE</div>
            <span style={{ fontWeight: 600 }}>{d.dateDemande || '-'}</span>
          </div>
          <div>
            <div className="info-label">DATE TRAITEMENT</div>
            <span style={{ fontWeight: 600 }}>{d.dateTraitement || '-'}</span>
          </div>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            {statut === 1 && <a className="btn btn-success" href={`/signature.html?id=${id}`}>Prendre photo</a>}
            {statut === 2 && (<>
              <a className="btn btn-success" href={`/signature.html?id=${id}`}>Reprendre photo</a>
              <a className="btn btn-primary" href={`/upload.html?id=${id}`}>Ajouter scan</a>
              <button className="btn btn-orange" onClick={scannerDossier}>Scan terminé</button>
            </>)}
            {statut === 3 && <button className="btn btn-success" onClick={terminerDossier}>Terminer le dossier</button>}
            {(statut === 10 || statut === 20) && <a className="btn btn-success" href={`/signature.html?id=${id}`}>Prendre photo</a>}
            {(statut === 11 || statut === 21) && (<>
              <a className="btn btn-primary" href={`/upload.html?id=${id}`}>Ajouter scan</a>
              <button className="btn btn-orange" onClick={scannerDossier}>Scan terminé</button>
            </>)}
            {statut === 12 && (<>
              <button className="btn btn-success" onClick={validerDuplicata}>Valider</button>
              <button className="btn btn-danger" onClick={rejeterDuplicata}>Rejeter</button>
            </>)}
            {statut === 13 && <button className="btn btn-success" onClick={emettreDuplicata}>Émettre duplicata</button>}
            {statut === 22 && (<>
              <button className="btn btn-success" onClick={validerTransfert}>Valider</button>
              <button className="btn btn-danger" onClick={rejeterTransfert}>Rejeter</button>
            </>)}
            {statut === 23 && <button className="btn btn-success" onClick={emettreTransfert}>Émettre transfert</button>}
          </div>
        </div>

        {/* Informations personnelles */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
            <h3 className="card-title" style={{ margin: 0 }}>Informations personnelles</h3>
            {statut === 2 && !editMode && (
              <button className="btn btn-primary btn-sm" onClick={ouvrirEdition}>Modifier</button>
            )}
          </div>

          {editMode ? (
            <>
              <div className="edit-banner">Mode édition — statut Photo prise</div>
              <div className="edit-grid">
                {[
                  ['nom', 'Nom'],
                  ['prenom', 'Prénom'],
                  ['dateNaissance', 'Date de naissance', 'date'],
                  ['lieuNaissance', 'Lieu de naissance'],
                  ['telephone', 'Téléphone'],
                  ['email', 'Email', 'email'],
                ].map(([field, label, type = 'text']) => (
                  <div className="form-group" key={field}>
                    <label>{label}</label>
                    <input
                      type={type}
                      value={editData[field] || ''}
                      onChange={e => setEditData(prev => ({ ...prev, [field]: e.target.value }))}
                    />
                  </div>
                ))}
                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                  <label>Adresse</label>
                  <input
                    type="text"
                    value={editData.adresse || ''}
                    onChange={e => setEditData(prev => ({ ...prev, adresse: e.target.value }))}
                  />
                </div>
              </div>
              <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                <button className="btn btn-success" onClick={enregistrerDemandeur} disabled={saving}>
                  {saving ? 'Enregistrement…' : 'Enregistrer'}
                </button>
                <button className="btn btn-secondary" onClick={() => setEditMode(false)}>Annuler</button>
              </div>
            </>
          ) : (
            <div className="info-grid">
              <div className="info-item"><div className="info-label">NOM</div><div className="info-value">{d.demandeur?.nom || '-'}</div></div>
              <div className="info-item"><div className="info-label">PRÉNOM</div><div className="info-value">{d.demandeur?.prenom || '-'}</div></div>
              <div className="info-item"><div className="info-label">DATE DE NAISSANCE</div><div className="info-value">{d.demandeur?.dateNaissance || '-'}</div></div>
              <div className="info-item"><div className="info-label">LIEU DE NAISSANCE</div><div className="info-value">{d.demandeur?.lieuNaissance || '-'}</div></div>
              <div className="info-item"><div className="info-label">NATIONALITÉ</div><div className="info-value">{d.demandeur?.nationalite?.libelle || '-'}</div></div>
              <div className="info-item"><div className="info-label">SITUATION FAMILIALE</div><div className="info-value">{d.demandeur?.situationFamiliale?.libelle || '-'}</div></div>
              <div className="info-item"><div className="info-label">TÉLÉPHONE</div><div className="info-value">{d.demandeur?.telephone || '-'}</div></div>
              <div className="info-item"><div className="info-label">EMAIL</div><div className="info-value">{d.demandeur?.email || '-'}</div></div>
              <div className="info-item" style={{ gridColumn: '1 / -1' }}><div className="info-label">ADRESSE</div><div className="info-value">{d.demandeur?.adresse || '-'}</div></div>
            </div>
          )}
        </div>

        {/* Passeport */}
        <div className="card">
          <h3 className="card-title">Passeport</h3>
          <div className="info-grid">
            <div className="info-item"><div className="info-label">NUMÉRO</div><div className="info-value">{passeport?.numeroPasseport || '-'}</div></div>
            <div className="info-item"><div className="info-label">PAYS DE DÉLIVRANCE</div><div className="info-value">{passeport?.paysDelivrance || '-'}</div></div>
            <div className="info-item"><div className="info-label">DATE DE DÉLIVRANCE</div><div className="info-value">{passeport?.dateDelivrance || '-'}</div></div>
            <div className="info-item"><div className="info-label">DATE D'EXPIRATION</div><div className="info-value">{passeport?.dateExpiration || '-'}</div></div>
          </div>
        </div>

        {/* Demande */}
        <div className="card">
          <h3 className="card-title">Demande</h3>
          <div className="info-grid">
            <div className="info-item"><div className="info-label">TYPE DE DEMANDE</div><div className="info-value">{d.typeDemande?.libelle || '-'}</div></div>
            <div className="info-item"><div className="info-label">TYPE DE VISA</div><div className="info-value">{d.typeVisa?.libelle || '-'}</div></div>
          </div>
        </div>

        {/* Visa transformable */}
        {d.visaTransformable && (
          <div className="card">
            <h3 className="card-title">Visa transformable</h3>
            <div className="info-grid">
              <div className="info-item"><div className="info-label">NUMÉRO DE RÉFÉRENCE</div><div className="info-value">{d.visaTransformable.numeroReference || '-'}</div></div>
              <div className="info-item"><div className="info-label">LIEU</div><div className="info-value">{d.visaTransformable.lieu || '-'}</div></div>
              <div className="info-item"><div className="info-label">DATE DE DÉBUT</div><div className="info-value">{d.visaTransformable.dateDebut || '-'}</div></div>
              <div className="info-item"><div className="info-label">DATE DE FIN</div><div className="info-value">{d.visaTransformable.dateFin || '-'}</div></div>
            </div>
          </div>
        )}

        {/* Historique */}
        <div className="card">
          <h3 className="card-title">Historique de la Demande</h3>
          {historiques.length === 0 ? (
            <p style={{ textAlign: 'center', color: '#999', padding: '30px' }}>Aucun historique.</p>
          ) : (
            <div className="timeline">
              {historiques.map((h, index) => {
                const isLatest = index === 0;
                return (
                  <div key={h.id} className={`timeline-item ${isLatest ? 'timeline-latest' : ''}`}>
                    <div className="timeline-dot" style={{ background: isLatest ? '#2980b9' : '#bdc3c7' }} />
                    <div className="timeline-content">
                      <div className="timeline-title">
                        {getStatutLibelle(h.statut)}
                        {isLatest && (
                          <span className={`badge ${getBadgeClass(h.statut)}`} style={{ marginLeft: '10px', fontSize: '0.75em' }}>
                            Statut actuel
                          </span>
                        )}
                      </div>
                      <div className="timeline-date">{h.dateChangementStatut}</div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

      </div>
    </>
  );
}
