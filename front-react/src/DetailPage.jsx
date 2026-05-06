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
        const demandeRes = await axios.get(`/api/demandes/${id}`);
        setDemande(demandeRes.data);
        
        const histoRes = await axios.get(`/api/demandes/${id}/historiques`);
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

  if (loading) return (
    <div className="container" style={{ textAlign: 'center', marginTop: '50px' }}>
      Chargement des détails...
    </div>
  );

  if (!demande) return (
    <div className="container">
      <div className="card" style={{ textAlign: 'center', color: '#e74c3c', padding: '40px' }}>
        <h2 style={{ color: '#e74c3c' }}>Erreur</h2>
        <p>Demande introuvable.</p>
        <div style={{ marginTop: '20px' }}>
            <Link to="/" className="btn btn-secondary">Retour à la recherche</Link>
        </div>
      </div>
    </div>
  );

  return (
    <>
      <div className="header">
        <h1>Back Office Visa — Madagascar</h1>
      </div>

      <div className="container">
        
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', borderBottom: '2px solid #ebf5fb', paddingBottom: '10px' }}>
              <h2 style={{ borderBottom: 'none', paddingBottom: 0, margin: 0 }}>Détails de la demande #{demande.id}</h2>
              <Link to="/" className="btn btn-secondary" style={{ fontSize: '0.85em', padding: '6px 12px' }}>
                &larr; Retour à la recherche
              </Link>
          </div>
          
          <div style={{ marginTop: '15px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <tbody>
                  <tr>
                    <th style={{ width: '40%', padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555' }}>
                        Statut Actuel
                    </th>
                    <td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>
                        <span className={`badge ${getBadgeClass(demande.statut)}`} style={{ fontSize: '1em' }}>
                            {demande.statutLibelle}
                        </span>
                    </td>
                  </tr>
                  <tr><th style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555', textAlign: 'left' }}>Date de demande</th><td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>{demande.dateDemande}</td></tr>
                  <tr><th style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555', textAlign: 'left' }}>Demandeur</th><td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>{demande.demandeur?.nom} {demande.demandeur?.prenom}</td></tr>
                  <tr><th style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555', textAlign: 'left' }}>Numéro de Passeport</th><td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>{demande.visaTransformable?.passeport?.numeroPasseport || '-'}</td></tr>
                  <tr><th style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555', textAlign: 'left' }}>Type de Visa</th><td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>{demande.typeVisa?.libelle || '-'}</td></tr>
                  <tr><th style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1', color: '#555', textAlign: 'left' }}>Type de Demande</th><td style={{ padding: '10px 0', borderBottom: '1px solid #ecf0f1' }}>{demande.typeDemande?.libelle || '-'}</td></tr>
                </tbody>
              </table>
          </div>
        </div>

        <div className="card">
          <h2>Historique de la Demande</h2>
          {historiques.length === 0 ? (
            <div className="empty" style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
              <div style={{ fontSize: '2.5em', marginBottom: '10px' }}>🕒</div>
              <p>Aucun historique trouvé pour cette demande.</p>
            </div>
          ) : (
            <div style={{ marginTop: '20px' }}>
              {historiques.map((historique, index) => {
                let libelleHisto = "";
                switch (historique.statut) {
                    case 1: libelleHisto = "Dossier créé"; break;
                    case 2: libelleHisto = "Dossier scanné"; break;
                    case 3: libelleHisto = "Dossier terminé"; break;
                    case 10: libelleHisto = "Duplicata demandé"; break;
                    case 11: libelleHisto = "Duplicata validé"; break;
                    case 12: libelleHisto = "Duplicata rejeté"; break;
                    case 13: libelleHisto = "Duplicata émis"; break;
                    case 20: libelleHisto = "Transfert demandé"; break;
                    case 21: libelleHisto = "Transfert validé"; break;
                    case 22: libelleHisto = "Transfert rejeté"; break;
                    case 23: libelleHisto = "Transfert émis"; break;
                    default: libelleHisto = "Inconnu (" + historique.statut + ")";
                }

                const isLatest = index === 0;

                return (
                  <div key={historique.id} style={{ 
                    display: 'flex', 
                    padding: '15px 20px', 
                    border: '1px solid #ecf0f1',
                    borderLeft: isLatest ? '5px solid #2980b9' : '5px solid #bdc3c7', 
                    backgroundColor: isLatest ? '#ebf5fb' : '#fff', 
                    marginBottom: '10px',
                    borderRadius: '6px',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    boxShadow: isLatest ? '0 2px 5px rgba(41,128,185,0.1)' : 'none'
                  }}>
                    <div>
                      <div style={{ fontSize: '1.05em', fontWeight: 'bold', color: isLatest ? '#1a5276' : '#2c3e50', marginBottom: '5px' }}>
                        Passage au statut : {libelleHisto}
                      </div>
                      <div style={{ fontSize: '0.88em', color: '#7f8c8d' }}>
                        Date de mise à jour : {historique.dateChangementStatut}
                      </div>
                    </div>
                    {isLatest && <span className={`badge ${getBadgeClass(historique.statut)}`}>Statut actuel</span>}
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
