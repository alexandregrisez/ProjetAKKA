import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const SignedInPage = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();
    var info = "";

    useEffect(() => {
        // Récupération du token
        const token = localStorage.getItem("token");

        if (!token) {
            info += "Le token n'a pas pu être récupéré"
            // Redirection vers la connexion si aucun token
            // navigate("/signin");
        } else {
            fetch("http://localhost:8080/userinfo", {
                method: "GET",
                headers: { Authorization: `Bearer ${token}` },
            })
            .then(res => res.json())
            .then(data => {
                setUser(data);
            })
            .catch(err => {
                info += "Erreur lors de la récupération de l'utilisateur"
                console.error("Erreur lors de la récupération de l'utilisateur :", err);
                localStorage.removeItem("token");
                //navigate("/signin"); // Rediriger vers connexion
            });
        }
    }, [navigate]);

    return (
        <div className="container">
            <h1>Test Connexion</h1>
            {user ? (
                <div className="user-info">
                    <p><strong>Nom :</strong> {user.lastName}</p>
                    <p><strong>Prénom :</strong> {user.firstName}</p>
                    <p><strong>Email :</strong> {user.email}</p>
                    <button onClick={() => {
                        localStorage.removeItem("token");
                        navigate("/signin");
                    }}>
                        Se déconnecter
                    </button>
                </div>
            ) : (
                <p>{info}</p>
            )}
        </div>
    );
};

export default SignedInPage;
