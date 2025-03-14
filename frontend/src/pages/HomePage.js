import Footer from "../components/Footer";
import Header from "../components/Header";



const HomePage = ()=> {
    return (
        <>
            <Header/>
            <main className="main">
                <h1>Bienvenue sur FinSight</h1>
                <p>
                    Grâce à FinSight, gérer votre portefeuille de votre actifs préférés (Action, ETF, Crypto)
                </p>
            </main>
            <Footer/>
        </>
    );
}

export default HomePage;