/*
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/SignedInPage.css';

const AssetsPage = ({ email }) => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);
    const [assetIds, setAssetIds] = useState([]);
    const [assets, setAssets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    // Fonction pour récupérer la liste des IDs des assets de l'utilisateur
    const fetchUserAssets = async () => {
        try {
            const response = await fetch(`http://localhost:8080/userAssets?email=${email}`);
            const data = await response.json();
            setAssetIds(data);
        } catch (err) {
            setError('Erreur lors de la récupération des assets');
            console.error(err);
        }
    };

    // Fonction pour récupérer les informations détaillées d'un asset
    const fetchAssetDetails = async (assetId) => {
        try {
            const response = await fetch(`http://localhost:8080/asset/${assetId}`);
            const data = await response.json();
            return data;
        } catch (err) {
            console.error(`Erreur lors de la récupération de l'asset avec l'ID ${assetId}:`, err);
            return null;
        }
    };

    // Récupérer le prix de l'actif
    const fetchPrice = async (s) => {
        const url = `http://localhost:8080/stock/${s}`;
        try {
            const response = await fetch(url);
            console.log(response)
            if (!response.ok) {
                // Si le serveur retourne une erreur HTTP, récupérez le message
                const errorData = await response.json();
                setErrorMessage(errorData.message || "Erreur lors de la récupération du prix.");
                return;
            }
            const data = await response.json();
            if (data.price) {
                setPrice(data.price);
            } else {
                setErrorMessage("Impossible de récupérer le prix de l'actif.");
            }
        } catch (error) {
            console.error("Erreur de requête", error);
            setErrorMessage("Une erreur est survenue lors de la récupération des informations sur l'actif.");
        }
    };

    useEffect(() => {
        const token = localStorage.getItem("token");
        console.log("Token récupéré dans SignedInPage:", token);

        if (!token) {
            setError("Le token n'a pas pu être récupéré.");
            setLoading(false);
            //Redirection sur la page de connexion
            navigate("/signin");
            return;
        }

        const fetchUserInfo = async () => {
            try {
                const response = await fetch("http://localhost:8080/userinfo", {
                    method: "GET",
                    headers: { Authorization: `Bearer ${token}` },
                });

                const data = await response.json();

                console.log(data.status)
                if (data.status === 0) {
                    setUser(data);
                } else {
                    setError("Erreur d'authentification. Veuillez vous reconnecter.");
                    localStorage.removeItem("token");
                    //navigate("/signin");
                }
            } catch (err) {
                console.error("Erreur lors de la récupération de l'utilisateur :", err);
                setError("Une erreur est survenue. Veuillez réessayer.");
                localStorage.removeItem("token");
                //navigate("/signin");
            } finally {
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, [navigate]);

    // Utilisation de useEffect pour charger les assets au démarrage
    useEffect(() => {
        const loadAssets = async () => {
            await fetchUserAssets(); // Charge d'abord les IDs des assets
            const assetDetails = await Promise.all(
                assetIds.map(async (id) => {
                    const details = await fetchAssetDetails(id); // Récupère les détails pour chaque asset
                    return details;
                })
            );
            setAssets(assetDetails.filter(Boolean)); // Ajoute uniquement les assets valides
            setLoading(false);
        };

        if (email) {
            loadAssets();
        }
    }, [email, assetIds]);

    if (loading) {
        return <div>Chargement des assets...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <>
        <Header/>
        <div>
            <h2>Mes Assets</h2>
            {assets.length === 0 ? (
                <p>Aucun asset trouvé.</p>
            ) : (
                <ul>
                    {assets.map((asset, index) => (
                        <li key={index}>
                            <div><strong>Symbol:</strong> {asset.symbol}</div>
                            <div><strong>Quantité:</strong> {asset.quantity}</div>
                            <SellBar symbol={symbol} price={fetchPrice(symbol)} maxQuantity={asset.quantity}/>
                        </li>
                    ))}
                </ul>
            )}
        </div>
        <Footer/>
        </>
    );
};

export default AssetsPage;

*/