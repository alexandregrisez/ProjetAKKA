import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";
import Footer from "../components/Footer";
import Header from "../components/Header";
import '../styles/SignupPage.css';

const SignupPage = () => {
    const navigate = useNavigate();
  
    // États pour les champs du formulaire
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [dateOfBirth, setDateOfBirth] = useState('');
    const [error, setError] = useState('');
  
    // Fonction pour soumettre le formulaire
    const handleSubmit = async (e) => {
      e.preventDefault();
  
      // Validation basique
      if (!email || !password || !firstName || !lastName || !dateOfBirth) {
        setError("Tous les champs sont obligatoires");
        return;
      }
  
      try {
        const response = await fetch('http://localhost:8080/signup', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            "Origin": "http://localhost:3000"
          },
          body: new URLSearchParams({
            email,
            password,
            firstName,
            lastName,
            dateOfBirth
          })
        });
  
        const data = await response.json();
        
        if (data.status === 0) {
          // Inscription réussie
          navigate('/signin?message=0');
        } else if (data.status === -1) {
          setError("Cet email est déjà utilisé");
        } else {
          setError("Une erreur est survenue. Veuillez réessayer.");
        }
      } catch (error) {
        setError("Une erreur est survenue. Veuillez réessayer.");
      }
    };
  
    return (
     <>
     <Header/>
      <div className="signup-box">
        <h2>Inscription</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="firstName">Prénom</label>
            <input
              type="text"
              id="firstName"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="lastName">Nom</label>
            <input
              type="text"
              id="lastName"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="dateOfBirth">Date de naissance</label>
            <input
              type="date"
              id="dateOfBirth"
              value={dateOfBirth}
              onChange={(e) => setDateOfBirth(e.target.value)}
              required
            />
          </div>
  
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
  
          <button type="submit">S'inscrire</button>
        </form>
      </div>
      <p style={{ textAlign: "center" }}>Déjà membre ? <Link to="/signin" className="btn">Connexion</Link> </p>
      <br/>
    <Footer/>
    </>
    );
};

export default SignupPage;