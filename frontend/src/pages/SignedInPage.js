import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/SignedInPage.css';
import AssetList from "../components/AssetListUser";
import PieChart from "../components/PieChartUser";

const SignedInPage = () => {
    const [user, setUser] = useState(null);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        console.log("Token récupéré dans SignedInPage:", token);

        if (!token) {
            setError("Le token n'a pas pu être récupéré.");
            setLoading(false);
            //Redirection sur la page de connexion
            navigate("/signin");
            return;
        }

        const fetchUserInfo = async () => {
            try {
                const response = await fetch("http://localhost:8080/userinfo", {
                    method: "GET",
                    headers: { Authorization: `Bearer ${token}` },
                });

                const data = await response.json();

                console.log(data.status)
                if (data.status === 0) {
                    setUser(data);
                } else {
                    setError("Erreur d'authentification. Veuillez vous reconnecter.");
                    localStorage.removeItem("token");
                    //navigate("/signin");
                }
            } catch (err) {
                console.error("Erreur lors de la récupération de l'utilisateur :", err);
                setError("Une erreur est survenue. Veuillez réessayer.");
                localStorage.removeItem("token");
                //navigate("/signin");
            } finally {
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, [navigate]);

    return (
        <>
        <Header/>
        <div className="container-signedin">
            <div className="side">
                <div className="box1">
                    {loading ? (
                        <p>Chargement des informations...</p>
                    ) : error ? (
                        <p className="error-message">{error}</p>
                    ) : user ? (
                        <div>
                        <div className="user-info">
                            <h2 className="box-title">Profil</h2>
                            <p><strong>Nom :</strong> {user.lastName}</p>
                            <p><strong>Prénom :</strong> {user.firstName}</p>
                            <p><strong>Email :</strong> {user.email}</p>
                            <p><strong>Date de naissance :</strong> {user.dateOfBirth}</p>
                            <button 
                                onClick={() => {
                                localStorage.removeItem("token");
                                navigate("/signin?message=2");
                                }}
                            >
                            Se déconnecter
                            </button>
                        </div>
                        </div>
                    ) : null}
                </div>
            </div>
            <div className="main-content">
                <PieChart userId={localStorage.getItem("token")}/>
                <AssetList userId={localStorage.getItem("token")}/>
            </div>
            
        </div>
        
        <Footer/>
        </>
    );
};

export default SignedInPage;
