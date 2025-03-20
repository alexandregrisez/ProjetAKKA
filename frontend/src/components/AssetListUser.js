import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AssetListUser.css"
const AssetList = ({ userId }) => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate(); // Hook pour la navigation

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch(`http://localhost:8080/assets/${userId}`);
        const data = await response.json();

        if (data) {
          setAssets(data);
        } else {
          throw new Error("Données manquantes ou invalides.");
        }
      } catch (err) {
        console.error("Erreur lors du chargement des données :", err);
        setError("Erreur lors du chargement des données");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [userId]);

  if (loading) {
    return (
      <div className="loading-container">
        <p>Chargement des actifs...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="asset-list-container">
        <h2 className="asset-list-title">Liste des actifs</h2>
        <div className="error-assets-container">
          <p style={{ color: "red" }}>Erreur : {error}</p>
        </div>
      </div>
    );
  }

  if (assets.length === 0) {
    return (
      <div className="asset-list-container">
        <h2 className="asset-list-title">Liste des actifs</h2>
        <div className="no-assets-container">
          <p>Aucun actif trouvé pour cet utilisateur.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="asset-list-container">
      <h2 className="asset-list-title">Liste des actifs</h2>
      <div className="asset-items-container">
        {assets.map((asset, index) => (
          <div key={index} className="asset-item">
            <div className="asset-details">
              <strong>{asset.name}</strong> : {asset.value} €
            </div>
            <button
              className="asset-button"
              onClick={() => navigate(`/asset/${asset.symbol}`)}
            >
              Afficher l'actif
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AssetList;
