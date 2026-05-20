import { useState } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [demandes, setDemandes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [qrcodeModalUrl, setQrcodeModalUrl] = useState(null);

  const fetchDemandes = async (searchQuery = '') => {
    setLoading(true);
    setHasSearched(true);
    try {
        const response = await axios.get(`/api/demandes/search?q=${searchQuery}`);
        setDemandes(response.data);
    } catch (error) {
        console.error("Erreur lors de la recherche des demandes", error);
    } finally {
        setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (query.trim()) {
        fetchDemandes(query);
    }
  };

  const getBadgeClass = (statut) => {
    switch(statut) {
      case 1: return 'badge-cree';
      case 2: return 'badge-termine';
      case 3: return 'badge-termine';
      case 10: return 'badge-duplicata-demande';
      case 11: return 'badge-duplicata-scanne';
      case 12: return 'badge-duplicata-valide';
      case 13: return 'badge-duplicata-rejete';
      case 14: return 'badge-duplicata-emis';
      case 20: return 'badge-transfert-demande';
      case 21: return 'badge-transfert-scanne';
      case 22: return 'badge-transfert-valide';
      case 23: return 'badge-transfert-rejete';
      case 24: return 'badge-transfert-emis';
      default: return '';
    }
  };

  return (
    <>
      <div className="header">
        <h1>Back Office Visa — Madagascar (ETU3188 - 3214 - 3210)</h1>
      </div>
      
      <div className="container">
        <div className="card">
          <h2>Recherche de Demandes</h2>
          
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: '15px', alignItems: 'flex-end', marginBottom: '10px' }}>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Rechercher par ID ou Numéro de Passeport</label>
              <input 
                type="text" 
                value={query} 
                onChange={(e) => setQuery(e.target.value)} 
                placeholder="Ex: 1234, B1234567..." 
                style={{ padding: '12px 14px', fontSize: '1em' }}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ padding: '12px 25px', fontSize: '1em' }}>
              Rechercher
            </button>
          </form>
        </div>

        {loading && <p style={{ textAlign: 'center', margin: '30px 0', color: '#555' }}>Recherche en cours...</p>}

        {!loading && hasSearched && (
          <div className="card">
            <h2>Résultats de recherche</h2>
            <div style={{ overflowX: 'auto' }}>
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Demandeur</th>
                    <th>Type Demande</th>
                    <th>Type Visa</th>
                    <th>Date Demande</th>
                    <th>Statut</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {demandes.length === 0 ? (
                    <tr>
                      <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#999' }}>
                        <div style={{ fontSize: '2.5em', marginBottom: '10px' }}>📋</div>
                        <p>Aucune demande trouvée avec ce critère.</p>
                      </td>
                    </tr>
                  ) : (
                    demandes.map(demande => {
                      const qrUrl = `${window.location.origin}/detail/${demande.id}`;
                      return (
                        <tr key={demande.id}>
                          <td><strong>#{demande.id}</strong></td>
                          <td>{demande.demandeur?.nom} {demande.demandeur?.prenom}</td>
                          <td>{demande.typeDemande?.libelle || '-'}</td>
                          <td>{demande.typeVisa?.libelle || '-'}</td>
                          <td>{demande.dateDemande || '-'}</td>
                          <td><span className={`badge ${getBadgeClass(demande.statut)}`}>{demande.statutLibelle}</span></td>
                          <td style={{ display: 'flex', gap: '8px' }}>
                            <Link to={`/detail/${demande.id}`} className="btn btn-outline btn-sm">
                              Voir
                            </Link>
                            <button 
                              className="btn btn-secondary btn-sm"
                              onClick={() => setQrcodeModalUrl(qrUrl)} 
                            >
                              QrCode
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {qrcodeModalUrl && (
          <div className="modal-backdrop" onClick={() => setQrcodeModalUrl(null)}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
              <h2 style={{ color: '#1a5276', marginBottom: '15px', borderBottom: '2px solid #ebf5fb', paddingBottom: '10px' }}>Code QR</h2>
              <p style={{ marginBottom: '20px', fontSize: '0.9em', color: '#555' }}>
                Scannez ce QR Code pour accéder au détail de la demande.
              </p>
              <QRCodeSVG value={qrcodeModalUrl} size={220} />
              <br />
              <button 
                className="btn btn-secondary"
                onClick={() => setQrcodeModalUrl(null)} 
                style={{ marginTop: '25px', width: '100%', padding: '12px' }}
              >
                Fermer
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
