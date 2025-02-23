import { useEffect, useState } from "react";

function AssetDetails({symbol}){
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);


    useEffect(()=>{
        // Empty variable
        if(!symbol){
            return;
        }

        const fetchData = async() => {
            const API_KEY = "EPBFL2E6MA2KG765";
            const url = `https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=${symbol}&apikey=${API_KEY}`;

            try{
                const response = await fetch(url);
                const result = await response.json();
                const quote = result["Global Quote"];
                console.log(result);
                if (quote){
                    setData({
                        symbol: quote["01. symbol"],
                        price: quote["05. price"],
                        open: quote["02. open"],
                        high: quote["03. high"],
                        low: quote["04. low"],
                        volume: quote["06. volume"],
                    });
                } else{
                    setError("Données indisponibles");
                }
                
            } catch (err){
                setError("Erreur lors du chargement des données");
            }
            setLoading(false);
        };
        fetchData();
    }, [symbol]);

    if(!symbol){
        return <p>Veuillez sélectionner un actif</p>
    }
    if(loading){
        return <p>Chargement...</p>
    }
    if(error){
        return <p>{error}</p>
    }

    return (
        <div>
            <h2>{data.symbol}</h2>
            <p>Prix actuel : ${data.price}</p>
            <p>Ouverture : ${data.open}</p>
            <p>Plus haut : ${data.high}</p>
            <p>Plus bas : ${data.low}</p>
            <p>Volume : {data.volume}</p>
        </div>
    );
}

export default AssetDetails;