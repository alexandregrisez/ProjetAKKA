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

    const API_KEY = "cv4nc6hr01qn2gab5ju0cv4nc6hr01qn2gab5jug";
    
    useEffect(() => {
        //Déterminer si un utilisateur est connecté
        const token = localStorage.getItem("token");
        if(token){
            setIsAuthenticated(true);
        }else {
            setIsAuthenticated(false);
        }

        //Récuperer le prix de l'actif
        const fetchPrice = async () => {
            const url = `http://localhost:8080/stock/${symbol}`;
            try {
                const response = await fetch(url);
                const data = await response.json();
                if (data.price) {
                    setPrice(data.price);
                } else {
                    console.error("Erreur lors de la récupération du prix");
                }
            } catch (error) {
                console.error("Erreur de requête", error);
            }
        };
        //Récuperer le nom de l'actif
        const fetchCompanyName = async () => {
            const url = `https://finnhub.io/api/v1/stock/profile2?symbol=${symbol}&token=${API_KEY}`;
            try {
                const response = await fetch(url);
                const data = await response.json();
                if (data.name) {
                    setCompanyName(data.name);
                } else {
                    console.error("Erreur lors de la récupération du nom de l'actif");
                }
            } catch (error) {
                console.error("Erreur de requête", error);
            }
        };

        fetchPrice();
        fetchCompanyName();
    }, [symbol]);

    return (
        <>
            <Header />
            <main className="main">
            <h1>Détails de {companyName} - ({symbol})</h1>
                <div className="main-content">
                    <StockChart symbol={symbol} />
                    <AssetDetails symbol={symbol} />
                </div>

                {!isAuthenticated ? (
                    <div className="login-message">
                        <p>Pour acheter cet actif, vous devez d'abord vous connecter.</p>
                    </div>
                ) : (
                    //Un utilisateur est connecté
                    <PurchaseBar symbol={symbol} price={price} />
                    //<SellBar symbol={symbol} price={price} maxQuantity={maxQuantity}/>
                )}
            </main>
            <Footer/> 
        </>
    );
};

export default StockPage;
