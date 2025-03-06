import { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";

const StockChart = ({ symbol }) => {
    const [chartData, setChartData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!symbol) {
            return;
        }

        const fetchStockData = async () => {
            const API_KEY = "EPBFL2E6MA2KG765";
            const url = `https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=${symbol}&apikey=${API_KEY}`;

            try {
                const response = await fetch(url);
                const data = await response.json();

                if (data["Time Series (Daily)"]) {
                    const timeSeries = data["Time Series (Daily)"];
                    const labels = Object.keys(timeSeries).slice(0, 30).reverse(); // 30 derniers jours
                    const prices = labels.map(date => parseFloat(timeSeries[date]["4. close"]));

                    setChartData({
                        labels,
                        datasets: [
                            {
                                label: `Prix de ${symbol} (USD)`,
                                data: prices,
                                borderColor: "rgba(75, 192, 192, 1)",
                                backgroundColor: "rgba(75, 192, 192, 0.2)",
                                tension: 0.2,
                            },
                        ],
                    });
                } else {
                    setError("Données non disponibles.");
                }
            } catch (e) {
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
            <h2>Suivi du prix - {symbol}</h2>
            <Line data={chartData} />
        </div>
    );
};

export default StockChart;
