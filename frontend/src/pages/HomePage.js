import { Link } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/HomePage.css';

const HomePage = ()=> {
    return (
        <>
            <Header/>
            <main className="main">
                <h1 className="slogan">Bienvenue sur FinSight ! La plateforme d'investissement</h1>
                <div className="container">
                    <p>Que vous soyez un investisseur débutant ou expérimenté, <strong>FinSight</strong> vous offre une expérience unique pour acheter des actions, des ETF et des cryptomonnaies préférés.</p>
                </div>
                <div className="container">
                    <h2>Acheter des actions en un click</h2>
                    <p>Investisser dans des actions des entreprises mondiales. Avec nos outils, vous allez pouvoir suivre et analyser vos investissements en temps réel pour maximiser les gains de votre portefeuille</p>
                </div>
                <div className="container">
                    <h2>Diversifier votre portefeuille avec des ETF</h2>
                    <p>Les ETF sont un excellent moyen de diversifier votre portefeuille et de vous exposer à un large éventail de secteurs, de régions géographiques et de stratégies. Sur FinSight, vous avez accès à une vaste gamme d'ETF, vous permettant de mieux répartir vos investissements tout en minimisant les risques.</p>
                </div>
                <div className="container">
                    <h2>Investisser dans les cryptomonnaies</h2>
                    <p>Les cryptomonnaies sont l'avenir de la finance, et FinSight vous permet d’acheter et vendre des cryptos populaires comme le Bitcoin, Ethereum et bien d'autres. Profitez des tendances émergentes et gérez facilement vos actifs avec un contrôle total.</p>
                </div>
                <div className="container">
                    <h2>Commencez à investir dès aujourd'hui</h2>
                    <p>Inscrivez-vous maintenant et commencez à batir votre avenir financier avec FinSight. Acheter des actions, des ETF et des cryptomonnaies au meilleur prix. N'attendez plus, rejoigner nos milliers d'utilisateurs !</p>
                </div>
                <Link to="/signup" className="btn">Inscription</Link>
            </main>
            <Footer/>
        </>
    );
}

export default HomePage;