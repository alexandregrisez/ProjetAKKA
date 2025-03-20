import { useEffect, useState } from "react";
import '../styles/AssetDetails.css';

function AssetDetails({ symbol }) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!symbol) {
            setData(null);
            return;
        }
    
        const fetchData = async () => {
            setLoading(true);
            setError(null);
    
            try {
                // Récupérer les cotations
                const quoteResponse = await fetch(`http://localhost:8080/details/${symbol}`);
                const quoteData = await quoteResponse.json()
                // Récupérer les informations de l'entreprise
                const profileResponse = await fetch(`http://localhost:8080/company/${symbol}`);
                const profileData = await profileResponse.json();
    
                // Vérification si les données ont été récupérées
                if (quoteData && quoteData.details && profileData && profileData.companyName) {
    
                    setData({
                        name: profileData.companyName || symbol, // Nom de l'actif
                        symbol,
                        price: quoteData.details.current.toFixed(2),
                        open: quoteData.details.open.toFixed(2),
                        high: quoteData.details.high.toFixed(2),
                        low: quoteData.details.low.toFixed(2),
                    });
                } else {
                    throw new Error("Données manquantes ou invalides.");
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
            <h2 className="box-title">Dernières informations</h2>
            <p><strong>Prix actuel :</strong> ${data.price}</p>
            <p><strong>Ouverture :</strong> ${data.open}</p>
            <p><strong>Plus haut :</strong> ${data.high}</p>
            <p><strong>Plus bas :</strong> ${data.low}</p>
        </div>
    );
}

export default AssetDetails;
