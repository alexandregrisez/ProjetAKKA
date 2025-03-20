import { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import { Chart as ChartJS, LineElement, CategoryScale, LinearScale, PointElement, Legend, Tooltip } from "chart.js";

ChartJS.register(LineElement, CategoryScale, LinearScale, PointElement, Legend, Tooltip);

const StockChart = ({ symbol }) => {
    const [chartData, setChartData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const HISTORY_DURATION = 15;

    useEffect(() => {
        if (!symbol) return;

        const fetchStockData = async () => {
            setLoading(true);
            setError(null);

            const url = `http://localhost:8080/details/${symbol}`;

            try {
                const response = await fetch(url);
                const details = await response.json();
                const data = details.details
                if (data.current) {
                    const labels = () => {
                        const dates = [];
                        for (let i = 0; i < HISTORY_DURATION; i++) {
                            const date = new Date();
                            date.setDate(date.getDate() - (HISTORY_DURATION - i));
                            dates.push(date.toLocaleDateString()); // Ajout à la liste des dates
                        }
                        return dates;
                    };

                    // Simulation de l'évolution des prix de clôture
                    const pricesClose = () => {
                        const prices = [];
                        for (let i = 0; i < HISTORY_DURATION; i++) {
                            const price = (data.open + (data.current - data.open) * (i / (HISTORY_DURATION - 1))).toFixed(2);
                            prices.push(price);
                        }
                        return prices;
                    };

                    // Simulation des prix hauts
                    const pricesHigh = () => {
                        const prices = [];
                        for (let i = 0; i < HISTORY_DURATION; i++) {
                            const price = (data.open + (data.high - data.open) * (i / (HISTORY_DURATION - 1))).toFixed(2);
                            prices.push(price);
                        }
                        return prices;
                    };

                    // Simulation des prix bas
                    const pricesLow = () => {
                        const prices = [];
                        for (let i = 0; i < HISTORY_DURATION; i++) {
                            const price = (data.open + (data.low - data.open) * (i / (HISTORY_DURATION - 1))).toFixed(2);
                            prices.push(price);
                        }
                        return prices;
                    };

                    setChartData({
                        labels: labels(),
                        datasets: [
                            {
                                label: `Prix de clôture`,
                                data: pricesClose(),
                                borderColor: "rgba(75, 192, 192, 1)",
                                backgroundColor: "rgba(75, 192, 192, 0.2)",
                                tension: 0.2,
                            },
                            {
                                label: `Prix haut`,
                                data: pricesHigh(),
                                borderColor: "rgba(255, 99, 132, 1)",
                                backgroundColor: "rgba(255, 99, 132, 0.2)",
                                tension: 0.2,
                            },
                            {
                                label: `Prix bas`,
                                data: pricesLow(),
                                borderColor: "rgba(54, 162, 235, 1)",
                                backgroundColor: "rgba(54, 162, 235, 0.2)",
                                tension: 0.2,
                            },
                        ],
                    });
                } else {
                    setError("Données non disponibles. Vérifiez le symbole ou réessayez plus tard.");
                }
            } catch (error) {
                setError("Erreur lors du chargement des données.");
            } finally {
                setLoading(false);
            }
        };

        fetchStockData();
    }, [symbol]);

    if (loading) return <p>Chargement du graphique...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div className="chart-container">
            <h2>Suivi du prix</h2>
            {chartData && (
                <Line 
                    data={chartData} 
                    options={{
                        plugins: {
                            legend: {
                                display: true,
                                position: "top",
                            },
                            tooltip: {
                                enabled: true, // Active l'affichage des valeurs au survol
                                mode: "index",
                                intersect: false,
                                callbacks: {
                                    label: (tooltipItem) => {
                                        return `${tooltipItem.dataset.label}: $${tooltipItem.raw}`;
                                    },
                                },
                            },
                        },
                        scales: {
                            x: {
                                title: {
                                    display: true,
                                    text: "Date",
                                },
                                ticks: {
                                    autoSkip: true, // Évite de superposer les dates
                                    maxRotation: 45, // Rotation des dates
                                    minRotation: 45,
                                },
                            },
                            y: {
                                title: {
                                    display: true,
                                    text: "Prix (USD)",
                                },
                            },
                        },
                    }}
                />
            )}
        </div>
    );
};

export default StockChart;
