import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import AssetDetails from "../components/AssetDetails";
import Header from "../components/Header";
import StockChart from "../components/StockChart";
import '../styles/StockPage.css';
import PurchaseBar from "../components/PurchaseBar";

const StockPage = () => {
    const { symbol } = useParams();
    const [price, setPrice] = useState(null);
    const [companyName, setCompanyName] = useState("");

    const API_KEY = "cv4nc6hr01qn2gab5ju0cv4nc6hr01qn2gab5jug";
    
    useEffect(() => {
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
                <PurchaseBar symbol={symbol} price={price} />

                {/*Si l'utisateur a cet actif, Ajouter le composent de vente
                    <Sellbar symbol={symbol} price={price} maxQuantity={maxQuantity} />
                */}
            </main>
        </>
    );
};

export default StockPage;
