import { useEffect, useState } from "react";

const SellBar = ({ symbol, price, maxQuantity }) => {
    const [quantity, setQuantity] = useState(1);
    const [totalPrice, setTotalPrice] = useState(0);

    useEffect(() => {
        if (price && quantity) {
            setTotalPrice((price * quantity).toFixed(2));
        }
    }, [price, quantity]);

    const handleIncrease = () => {
        if (quantity < maxQuantity) {
            setQuantity(quantity + 1);
        }
    };

    const handleDecrease = () => {
        if (quantity > 1) {
            setQuantity(quantity - 1);
        }
    };


    const handleQuantityChange = (value) => {
        const newQuantity = parseInt(value.target.value);
        if (newQuantity >= 1 && newQuantity <= maxQuantity) {
            setQuantity(newQuantity);
        }
    };


    const handleAll = () => {
        setQuantity(maxQuantity);
    };

    // Achat backend
    const handleSell = async () => {
        // Quantité invalide
        if (quantity <= 0) return;

        // Envoyer les infos sur l'achat au backend
        try {
            // 1. Obtenir l'identité de l'utilisateur
            const token = localStorage.getItem("token");
            const response1 = await fetch("http://localhost:8080/userinfo", {
                method: "GET",
                headers: { Authorization: `Bearer ${token}` },
            });

            const data1 = await response1.json();

            console.log(data1.status)
            if (data1.status === 0) {
                setUser(data1);
                console.log("Utilisateur connecté pour la vente.")
            } 
            else {
                alert("Erreur d'authentification. Veuillez vous reconnecter.");
                localStorage.removeItem("token");
                navigate('/signin');
                return;
            }

            // 2. Transaction

            const formData = new URLSearchParams();
            formData.append("email", data1.email);
            formData.append("symbol", symbol);
            formData.append("quantity", quantity);
            formData.append("totalPrice", totalPrice);

            const response2 = await fetch('http://localhost:8080/sell', {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/x-www-form-urlencoded',
                  "Origin": "http://localhost:3000"
                },
                body: formData
            });
        
            const data2 = await response2.json();
              
            if (data2.status==0) {
              alert("Transaction effectuée !");
            } 
            else if(data2.status==-1){
              alert("Vous ne pouvez pas vendre cette quantité !");
            }
            else {
                alert("Un problème est survenu.");
            }
        } 
        catch (err) {
            alert("Un problème est survenu.");
        } 
        finally {
            setLoading(false);
        }

    };

    return (
        <>
            <div className="purchase-container">
                <h2 className="box-title">Vendre {symbol}</h2>
                <div className="purchase-container">
                    <div className="purchase-controls">
                        <button onClick={handleDecrease}>-</button>
                        <input
                            type="number"
                            value={quantity}
                            onChange={handleQuantityChange}
                            min={1}
                            max={maxQuantity}
                        />
                        <button onClick={handleIncrease}>+</button>
                        <button onClick={handleAll}>Tout</button>
                    </div>
                    <p>Total : ${totalPrice}</p>
                    <button className="buy-button" onClick={handleSell}>
                        Vendre
                    </button>
                </div>
            </div>
        </>
    );
};

export default SellBar;
