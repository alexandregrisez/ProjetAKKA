import { useParams } from "react-router-dom";
import AssetDetails from "../components/AssetDetails";
import Header from "../components/Header";
import StockChart from "../components/StockChart";

const StockPage = () => {
    const { symbol } = useParams();

    return (
        <>
            <Header />
            <div>
                <h1>Détails de {symbol}</h1>
                <AssetDetails symbol={symbol} />
                <StockChart symbol={symbol}/>
            </div>
        </>
    );
}

export default StockPage;
