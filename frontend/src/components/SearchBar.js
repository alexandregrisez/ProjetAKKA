import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/SearchBar.css";

const SearchBar = () => {
    const [query, setQuery] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [showPopup, setShowPopup] = useState(false);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const searchRef = useRef(null);

    const NUMBER_SUGGESTIONS = 5;

    useEffect(() => {
        if (query.length < 2) {
            setSuggestions([]);
            setShowPopup(false);
            return;
        }

        const fetchSuggestions = async () => {
            setError(null);
            setLoading(true);
            try {
                const response = await fetch(`http://localhost:8080/suggestion/${query}`);
                const data = await response.json();
                
                if (data.details.result && data.details.result.length > 0) {
                    setSuggestions(
                        data.details.result.slice(0, NUMBER_SUGGESTIONS).map((item) => ({
                            symbol: item.symbol,
                            name: item.description,
                        }))
                    );
                    setError(null)
                } else {
                    setSuggestions([]);
                }
                setShowPopup(true);
            } catch (error) {
                console.error("Erreur lors de la récupération des suggestions :", error);
                setError("Erreur lors du chargement des données.");
                setSuggestions([]);
            }
            setLoading(false);
        };

        fetchSuggestions();
    }, [query]);

    const handleSelect = (symbol) => {
        navigate(`/asset/${symbol}`);
        setQuery("");
        setShowPopup(false);
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (searchRef.current && !searchRef.current.contains(event.target)) {
                setShowPopup(false);
            }
        };

        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);

    return (
        <div className="search-container" ref={searchRef}>
            <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Rechercher un actif"
            />
            {showPopup && (
                <div className="search-popup">
                    <ul>
                        {loading && <li className="loading-message">Recherche en cours...</li>}
                        {error && <li className="error-message">{error}</li>}
                        {!loading && !error && suggestions.length === 0 && (
                            <li className="no-results">Aucun actif correspond à votre recherche.</li>
                        )}
                        {!loading &&
                            suggestions.map((item) => (
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
