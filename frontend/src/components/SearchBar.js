import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/SearchBar.css";

const SearchBar = () => {
    const [query, setQuery] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [showPopup, setShowPopup] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const searchRef = useRef(null);

    const API_KEY = "cv4nc6hr01qn2gab5ju0cv4nc6hr01qn2gab5jug";
    const NUMBER_SUGGESTIONS = 5;

    useEffect(() => {
        if (query.length < 2) {
            setSuggestions([]);
            setShowPopup(false);
            return;
        }

        const fetchSuggestions = async () => {
            setError(null);
            try {
                const response = await fetch(`https://finnhub.io/api/v1/search?q=${query}&token=${API_KEY}`);
                const data = await response.json();

                if (data.result && data.result.length > 0) {
                    setSuggestions(
                        data.result.slice(0, NUMBER_SUGGESTIONS).map((item) => ({
                            symbol: item.symbol,
                            name: item.description,
                        }))
                    );
                    setShowPopup(true);
                } else {
                    setSuggestions([]);
                    setShowPopup(true);
                }
            } catch (error) {
                console.error("Erreur lors de la récupération des suggestions :", error);
                setError("Erreur lors du chargement des données.");
            }
        };

        fetchSuggestions();
    }, [query]);

    const handleSelect = (symbol) => {
        navigate(`/asset/${symbol}`);
        setQuery("");
        setShowPopup(false);
    };

    // Fermer le popup si on clique en dehors
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
                        {error && <li className="error-message">{error}</li>}
                        {!error && suggestions.length === 0 && <li>Aucun résultat trouvé.</li>}
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
