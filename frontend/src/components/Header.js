import { useNavigate } from "react-router-dom";
import SearchBar from "./SearchBar";
import "../styles/Header.css";
import logo from "../logo.svg";
import { useEffect, useState } from "react";

const Header = () => {
    const navigate = useNavigate();
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("token");
        //Convertion en booléen
        setIsAuthenticated(!!token);
    }, []);

    return (
        <header className="header">
            <div className="logo" onClick={() => navigate("/")}>
                <img src={logo} alt="logo"/>
                <h1 className="company-name">FinSight</h1>
            </div>
            <SearchBar />
            <button onClick={() => navigate(isAuthenticated ? "/signed-in" : "/signin")}>
                {isAuthenticated ? "Mon Profil" : "Se Connecter"}
            </button>
        </header>
    );
};

export default Header;
