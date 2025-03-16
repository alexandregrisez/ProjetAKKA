import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from "react-router-dom";
import { Link } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/SigninPage.css';

const SigninPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // États pour les champs du formulaire
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  // Vérification des paramètres d'URL pour afficher un message de succès
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const message = params.get('message');
    if (message === '0') {
        setMessage('Inscription réussie ! Vous pouvez maintenant vous connecter.');
        setMessageType('success');
    }
    if (message === '1') {
        setMessage("Le couple email - mot de passe est incorrect.");
        setMessageType('error');
    }
    if (message === '2') {
        setMessage("Vous avez bien été déconnecté.");
        setMessageType('success');
    }
  }, [location.search]);

  // Fonction pour soumettre le formulaire
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validation basique
    if (!email || !password) {
      setError("Tous les champs sont obligatoires");
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/signin', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          "Origin": "http://localhost:3000"
        },
        body: new URLSearchParams({
          email,
          password
        })
      });

      const data = await response.json();
      
      if (data.token) {
        localStorage.setItem("token", data.token);
        console.log("Token ajouté au localStorage:", localStorage.getItem("token"));
        setTimeout(() => {
            navigate('/signed-in');
        }, 100);
      } else {
        setError("Email ou mot de passe incorrect");
      }
    } catch (error) {
      setError("Une erreur est survenue. Veuillez réessayer.");
    }
  };

  return (
    <>
      <Header />
      {/* Affichage du message si présent */}
      {(message || error) && (
          <div className={`message-box ${messageType}`}>
            <p>{message || error}</p>
          </div>
        )}
      
      <div className="signup-box">
        <h2>Connexion</h2>

        

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Mot de passe</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {error && <p className="error">{error}</p>}

          <button type="submit">Se connecter</button>
        </form>

        
      </div>
      <p style={{ textAlign: "center" }}>
          Pas encore inscrit ? <Link to="/signup" className="btn">Inscription</Link>
        </p>
        <br/>
      <Footer />
    </>
  );
};

export default SigninPage;
