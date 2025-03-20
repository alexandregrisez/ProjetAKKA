import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/AssetListUser.css";

const AssetList = () => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userId, setUserId] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchData = async () => {
      try {
        // 1. Obtenir l'identité de l'utilisateur
        const token = localStorage.getItem("token");
        const response1 = await fetch("http://localhost:8080/userinfo", {
          method: "GET",
          headers: { Authorization: `Bearer ${token}` },
        });

        const data1 = await response1.json();

        console.log(data1.status);
        if (data1.status === 0) {
          setUserId(data1.email);
          console.log("Utilisateur connecté pour la vente.");
        } else {
          alert("Erreur d'authentification. Veuillez vous reconnecter.");
          localStorage.removeItem("token");
          navigate("/signin");
          return;
        }

        // 2. Récupérer les actifs en utilisant l'email comme userId
        if (userId) {
          const response = await fetch(`http://localhost:8080/assets/${userId}`);
          const data = await response.json();

          if (data) {
            setAssets(data);
          } else {
            throw new Error("Données manquantes ou invalides.");
          }
        }
      } catch (err) {
        console.error("Erreur lors du chargement des données :", err);
        setError("Erreur lors du chargement des données");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [userId, navigate]);

  if (loading) {
    return (
      <div className="asset-list-container">
        <div className="loading-container">
          <p>Chargement des actifs...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="asset-list-container">
        <h2 className="box-title">Liste des actifs</h2>
        <div className="error-assets-container">
          <p style={{ color: "red" }}>{error}</p>
        </div>
      </div>
    );
  }

  if (assets.length === 0) {
    return (
      <div className="asset-list-container">
        <h2 className="box-title">Liste des actifs</h2>
        <div className="no-assets-container">
          <p>Aucun actif trouvé pour cet utilisateur.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="asset-list-container">
      <h2 className="box-title">Liste des actifs</h2>
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
