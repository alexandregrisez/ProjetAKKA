import React, { useEffect, useState } from "react";
import { PieChart, Pie, Cell } from "recharts";

const PieChartUser = ({ userId }) => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWaletCaterogies = async () => {
    try {
        const response = await fetch(`http://localhost:8080/pie/${userId}`);
        const data = await response.json();

        if (data) {
          setData(data);
        } else {
          throw new Error("Données manquantes ou invalides.");
        }
      } catch (err) {
        console.error("Erreur lors du chargement des données :", err);
        setError("Erreur lors du chargement des données");
      } finally {
        setLoading(false);
      }
  }

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
      <div>
        <h2 className="box-title">Répartition de vos Actifs</h2>
        <h2>Erreur de chargement</h2>
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
