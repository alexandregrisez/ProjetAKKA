import { useEffect, useState } from "react";

function AssetDetails({ symbol }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const API_KEY = "cv4nc6hr01qn2gab5ju0cv4nc6hr01qn2gab5jug";

    useEffect(() => {
        if (!symbol) {
            setData(null);
            return;
        }

        const fetchData = async () => {
            setLoading(true);
            setError(null);

            try {
                // Récupérer les données de cotation
                const quoteResponse = await fetch(`https://finnhub.io/api/v1/quote?symbol=${symbol}&token=${API_KEY}`);
                const quoteData = await quoteResponse.json();

                // Récupérer les informations de l'actif (nom, etc.)
                const profileResponse = await fetch(`https://finnhub.io/api/v1/stock/profile2?symbol=${symbol}&token=${API_KEY}`);
                const profileData = await profileResponse.json();

                if (quoteData && quoteData.c && profileData && profileData.name) {
                    setData({
                        name: profileData.name || symbol, // Nom de l'actif
                        symbol,
                        price: quoteData.c.toLocaleString(),
                        open: quoteData.o.toLocaleString(),
                        high: quoteData.h.toLocaleString(),
                        low: quoteData.l.toLocaleString(),
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
        <div className="details-asset">
            <h2>Dernières informations</h2>
            <p>Prix actuel : <strong>${data.price}</strong></p>
            <p>Ouverture : ${data.open}</p>
            <p>Plus haut : ${data.high}</p>
            <p>Plus bas : ${data.low}</p>
        </div>
    );
}

export default AssetDetails;
