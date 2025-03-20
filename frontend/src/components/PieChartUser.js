import React, { useEffect, useState } from "react";
import { PieChart, Pie, Cell } from "recharts";

const PieChartUser = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userId, setUserId] = useState(null);

  const fetchWalletCategories = async () => {
    try {
      // 1. Obtenir l'identité de l'utilisateur
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("Token manquant, veuillez vous reconnecter.");
      }
      const response = await fetch("http://localhost:8080/userinfo", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const userData = await response.json();

      if (userData) {
        setUserId(userData.email);
        console.log("ID utilisateur récupéré:", userData.email);
      } else {
        throw new Error("Impossible de récupérer les informations de l'utilisateur.");
      }
    } catch (err) {
      console.error("Erreur lors du chargement des données utilisateur :", err);
      setError("Erreur lors du chargement des données utilisateur");
    }
  };

  // Fonction pour récupérer les données du graphique après récupération de l'ID utilisateur
  const fetchPieData = async (userId) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("Token manquant, veuillez vous reconnecter.");
      }

      // Récupérer les données du graphique
      const pieResponse = await fetch(`http://localhost:8080/pie/${userId}`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const pieData = await pieResponse.json();
      if (pieData) {
        setData(pieData);
      } else {
        throw new Error("Données manquantes ou invalides pour le graphique.");
      }
    } catch (err) {
      console.error("Erreur lors du chargement des données pour le graphique :", err);
      setError("Erreur lors du chargement des données du graphique");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWalletCategories();
  }, []);

  useEffect(() => {
    if (userId) {
      fetchPieData(userId);
    }
  }, [userId]);

  // Somme des actifs
  const total = data.reduce((acc, item) => acc + item.value, 0);

  // Palette de couleurs
  const COLORS = ["#0088FE", "#00C49F", "#FFBB28"];

  if (loading) {
    return (
      <div className="box2">
        <h2 className="box-title">Répartition de vos Actifs</h2>
        <p>Chargement du diagramme...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="box2">
        <h2 className="box-title">Répartition de vos Actifs</h2>
        <p style={{ color: "red" }}>{error}</p>
      </div>
    );
  }

  return (
    <div className="box2" style={{ textAlign: "center" }}>
      <h2 className="box-title">Répartition de vos Actifs</h2>
      <PieChart width={300} height={300}>
        <Pie
          data={data}
          cx="50%"
          cy="50%"
          innerRadius={60}
          outerRadius={100}
          fill="#8884d8"
          paddingAngle={5}
          dataKey="value"
        >
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>

        <text
          x="50%"
          y="50%"
          textAnchor="middle"
          dominantBaseline="middle"
          fontSize={20}
          fontWeight="bold"
        >
          {total}
        </text>
      </PieChart>

      <div>
        {data.map((entry, index) => (
          <div
            key={index}
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              marginTop: 5,
            }}
          >
            <div
              style={{
                width: 10,
                height: 10,
                backgroundColor: COLORS[index % COLORS.length],
                marginRight: 5,
              }}
            ></div>
            <span>
              {entry.name}: {entry.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PieChartUser;
