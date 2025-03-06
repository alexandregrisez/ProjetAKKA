import SearchBar from "./SearchBar";
import "../styles/Header.css";


const Header = () =>{
    return (
        <header className="header">
            <div className="logo">
                <img src="../logo.svg" alt="logo"/>
                <h1>FinSight</h1>
            </div>
            <SearchBar />
            <button>Mon Compte</button>
        </header>
    );
};

export default Header;