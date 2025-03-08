import { useNavigate } from "react-router-dom";
import SearchBar from "./SearchBar";
import "../styles/Header.css";
import logo from "../logo.svg";

const Header = () => {
    const navigate = useNavigate();

    return (
        <header className="header">
            <div className="logo" onClick={() => navigate("/")}>
                <img src={logo} alt="logo"/>
                <h1>FinSight</h1>
            </div>
            <SearchBar />
            <button>Mon Compte</button>
        </header>
    );
};

export default Header;
