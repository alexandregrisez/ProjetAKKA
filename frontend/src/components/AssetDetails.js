import { useEffect, useState } from "react";

function AssetDetails({ symbol }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!symbol) {
            setData(null); // Réinitialise les données si aucun symbole n'est sélectionné
            return;
        }

        const fetchData = async () => {
            setLoading(true);
            setError(null); // Réinitialise l'erreur avant chaque requête

            const API_KEY = "EPBFL2E6MA2KG765";
            const url = `https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=${symbol}&apikey=${API_KEY}`;

            try {
                const response = await fetch(url);
                const result = await response.json();
                const quote = result["Global Quote"];

                if (quote && quote["01. symbol"]) {
                    setData({
                        symbol: quote["01. symbol"],
                        price: parseFloat(quote["05. price"]).toLocaleString(),
                        open: parseFloat(quote["02. open"]).toLocaleString(),
                        high: parseFloat(quote["03. high"]).toLocaleString(),
                        low: parseFloat(quote["04. low"]).toLocaleString(),
                        volume: parseInt(quote["06. volume"]).toLocaleString(),
                    });
                } else {
                    setError("Données indisponibles.");
                    setData(null);
                }
            } catch (err) {
                console.error("Erreur lors du chargement des données :", err);
                setError("Erreur lors du chargement des données.");
                setData(null);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [symbol]);

    if (!symbol) return <p>Veuillez sélectionner un actif.</p>;
    if (loading) return <p>Chargement...</p>;
    if (error) return <p>{error}</p>;
    if (!data) return <p>Aucune donnée disponible.</p>;

    return (
        <div>
            <h2>{data.symbol}</h2>
            <p>Prix actuel : ${data.price}</p>
            <p>Ouverture : ${data.open}</p>
            <p>Plus haut : ${data.high}</p>
            <p>plus bas :  ${data.low}</p>
            <p>Volume : {data.volume}</p>
        </div>
    );
}

export default AssetDetails;
