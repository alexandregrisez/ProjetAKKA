import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import AssetDetails from "../components/AssetDetails";
import Header from "../components/Header";
import StockChart from "../components/StockChart";
import '../styles/StockPage.css';
import PurchaseBar from "../components/PurchaseBar";
import Footer from "../components/Footer";

const StockPage = () => {
    const { symbol } = useParams();
    const [price, setPrice] = useState(null);
    const [companyName, setCompanyName] = useState("");
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState(""); // State pour le message d'erreur

    useEffect(() => {
        // Déterminer si un utilisateur est connecté
        const token = localStorage.getItem("token");
        if (token) {
            setIsAuthenticated(true);
        } else {
            setIsAuthenticated(false);
        }

        setErrorMessage("")
        // Récupérer le prix de l'actif
        const fetchPrice = async () => {
            const url = `http://localhost:8080/stock/${symbol}`;
            try {
                const response = await fetch(url);
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

        // Récupérer le nom de l'actif
        const fetchCompanyName = async () => {
            const url = `http://localhost:8080/company/${symbol}`;
            try {
                const response = await fetch(url);
                if (!response.ok) {
                    // Si le serveur retourne une erreur HTTP, récupérez le message
                    const errorData = await response.json();
                    setErrorMessage(errorData.message || "Erreur lors de la récupération du nom de l'actif.");
                    return;
                }
                const data = await response.json();
                if (data.companyName) {
                    setCompanyName(data.companyName);
                } else {
                    setErrorMessage("Impossible de récupérer le nom de l'actif.");
                }
                setIsLoading(false);
            } catch (error) {
                console.error("Erreur de requête", error);
                setErrorMessage("Vous n'avez pas accès à cet actif, Veuillez en choisir un autre");
                setIsLoading(false);
            }
        };

        fetchPrice();
        fetchCompanyName();
    }, [symbol]);

    return (
        <>
            <Header />
            
            {errorMessage ? (
                <div className="error-container">
                    <h1 className="error-message">{errorMessage}</h1>
                </div>
            ) : (
                <main className="main">
                    {isLoading ? (
                        <h1>Chargement...</h1>
                    ) : (
                        <h1 className="asset-title">Détails de {companyName} - ({symbol})</h1>
                    )}
                    <div className="asset-content">
                        <StockChart symbol={symbol} />
                        <AssetDetails symbol={symbol} />
                    </div>
    
                    {!isAuthenticated ? (
                        <div className="login-message">
                            <p>Pour acheter cet actif, vous devez d'abord vous connecter.</p>
                        </div>
                    ) : (
                        // Un utilisateur est connecté
                        <PurchaseBar symbol={symbol} price={price} />
                        // <SellBar symbol={symbol} price={price} maxQuantity={maxQuantity}/>
                    )}
                </main>
            )}
            
            <Footer />
        </>
    );
    
    
};

export default StockPage;
