import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import '../styles/SearchBar.css';

const SearchBar = () => {
    const [query, setQuery] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [showPopup, setShowPopup] = useState(false);
    const navigate = useNavigate();
    const searchRef = useRef(null);

    useEffect(() => {
        if (query.length < 2) {
            setSuggestions([]);
            setShowPopup(false);
            return;
        }

        const fetchSuggestions = async () => {
            const API_KEY = "EPBFL2E6MA2KG765";
            const url = `https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=${query}&apikey=${API_KEY}`;

            try {
                const response = await fetch(url);
                const data = await response.json();
                
                if (data.bestMatches) {
                    setSuggestions(
                        data.bestMatches.map((item) => ({
                            symbol: item["1. symbol"],
                            name: item["2. name"],
                        }))
                    );
                    setShowPopup(true);
                }
            } catch (error) {
                console.error("Erreur", error);
            }
        };

        fetchSuggestions();
    }, [query]);

    const handleSelect = (symbol) => {
        navigate(`/asset/${symbol}`);
        setQuery("");
        setShowPopup(false);
    };

    return (
        <div className="search-container" ref={searchRef}>
            <input type="text" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Rechercher un actif" />
            {showPopup && (
                <div className="search-popup">
                    <ul>
                        {suggestions.map((item) => (
                            <li key={item.symbol} onClick={() => handleSelect(item.symbol)}>
                                {item.symbol} - {item.name}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default SearchBar;
