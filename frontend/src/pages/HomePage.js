import { Link } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/HomePage.css';

const HomePage = ()=> {
    return (
        <>
            <Header/>
            <main className="main">
                <h1>Bienvenue sur FinSight !</h1>
                <div className="auth-box">
                    <p style={{ textAlign: "center" }}>Grâce à FinSight, gérez le portefeuille de vos actifs préférés (Action, ETF, Crypto) !</p>
                    <div className="auth-links">
                        <Link to="/signin" className="btn">Connexion</Link>
                        <Link to="/signup" className="btn">Inscription</Link>
                    </div>

                </div>
                
                <hr/>
            </main>
            <Footer/>
        </>
    );
}

export default HomePage;