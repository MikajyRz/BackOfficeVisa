import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import axios from 'axios';

export default function DetailPage() {
  const { id } = useParams();
  const [demande, setDemande] = useState(null);
  const [historiques, setHistoriques] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDetailAndHistorique = async () => {
      try {
        setLoading(true);
        const [demandeRes, histoRes] = await Promise.all([
          axios.get(`/api/demandes/${id}`),
          axios.get(`/api/demandes/${id}/historiques`)
        ]);
        setDemande(demandeRes.data);
        setHistoriques(histoRes.data);
      } catch (error) {
        console.error("Erreur de récupération", error);
      } finally {
        setLoading(false);
      }
    };
    fetchDetailAndHistorique();
  }, [id]);

  const getBadgeClass = (statut) => {
    switch(statut) {
      case 1: return 'badge-cree';
      case 2: return 'badge-termine';
      case 3: return 'badge-termine';
      case 10: return 'badge-duplicata-demande';
      case 11: return 'badge-duplicata-valide';
      case 12: return 'badge-duplicata-rejete';
      case 13: return 'badge-duplicata-emis';
      case 20: return 'badge-transfert-demande';
      case 21: return 'badge-transfert-valide';
      case 22: return 'badge-transfert-rejete';
      case 23: return 'badge-transfert-emis';
      default: return '';
    }
  };

  const getStatutLibelle = (statut) => {
    switch (statut) {
      case 1: return "Dossier créé";
      case 2: return "Dossier scanné";
      case 3: return "Dossier terminé";
      case 10: return "Duplicata demandé";
      case 11: return "Duplicata validé";
      case 12: return "Duplicata rejeté";
      case 13: return "Duplicata émis";
      case 20: return "Transfert demandé";
      case 21: return "Transfert validé";
      case 22: return "Transfert rejeté";
      case 23: return "Transfert émis";
      default: return "Inconnu (" + statut + ")";
    }
  };

  if (loading) return (
    <>
      <div className="header"><h1>Back Office Visa — Madagascar (ETU3188 - 3214 - 3210)</h1></div>
      <div className="container" style={{ textAlign: 'center', marginTop: '50px', color: '#555' }}>
        Chargement des détails...
      </div>
    </>
  );

  if (!demande) return (
    <>
      <div className="header"><h1>Back Office Visa — Madagascar (ETU3188 - 3214 - 3210)</h1></div>
      <div className="container">
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <div style={{ fontSize: '2.5em', marginBottom: '10px' }}>❌</div>
          <p style={{ color: '#e74c3c', fontWeight: 600 }}>Demande introuvable.</p>
          <div style={{ marginTop: '20px' }}>
            <Link to="/" className="btn btn-secondary">← Retour à la recherche</Link>
          </div>
        </div>
      </div>
    </>
  );

  const d = demande;
  const passeport = d.visaTransformable?.passeport;

  return (
    <>
      <div className="header">
        <h1>Back Office Visa — Madagascar (ETU3188 - 3214 - 3210)</h1>
      </div>

      <div className="container">

        {/* Top Bar */}
        <div className="top-bar">
          <h2>Demande #{d.id}</h2>
          <Link to="/" className="btn btn-secondary">← Retour à la recherche</Link>
        </div>

        {/* Statut Card */}
        <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '15px' }}>
          <div>
            <div className="info-label">STATUT ACTUEL</div>
            <span className={`badge ${getBadgeClass(d.statut)}`} style={{ fontSize: '0.95em' }}>{d.statutLibelle}</span>
          </div>
          <div>
            <div className="info-label">DATE DE DEMANDE</div>
            <span style={{ fontWeight: 600 }}>{d.dateDemande || '-'}</span>
          </div>
          <div>
            <div className="info-label">DATE TRAITEMENT</div>
            <span style={{ fontWeight: 600 }}>{d.dateTraitement || '-'}</span>
          </div>
        </div>

        {/* Informations personnelles */}
        <div className="card">
          <h3 className="card-title">Informations personnelles</h3>
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
            <div style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
              <div style={{ fontSize: '2.5em', marginBottom: '10px' }}>🕒</div>
              <p>Aucun historique trouvé pour cette demande.</p>
            </div>
          ) : (
            <div className="timeline">
              {historiques.map((h, index) => {
                const isLatest = index === 0;
                const libelle = getStatutLibelle(h.statut);
                return (
                  <div key={h.id} className={`timeline-item ${isLatest ? 'timeline-latest' : ''}`}>
                    <div className="timeline-dot" style={{ background: isLatest ? '#2980b9' : '#bdc3c7' }}></div>
                    <div className="timeline-content">
                      <div className="timeline-title">
                        {libelle}
                        {isLatest && <span className={`badge ${getBadgeClass(h.statut)}`} style={{ marginLeft: '10px', fontSize: '0.75em' }}>Statut actuel</span>}
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
