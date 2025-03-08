import { useEffect, useState } from "react";

const PurchaseBar = ({ symbol, price }) => {
    const [quantity, setQuantity] = useState(1);
    const [totalPrice, setTotalPrice] = useState(0);

    useEffect(() => {
        if (price && quantity) {
            setTotalPrice((price * quantity).toFixed(2));
        }
    }, [price, quantity]);

    const handleIncrease = () => {
        setQuantity(quantity + 1);
    };

    const handleDecrease = () => {
        if (quantity > 1) {
            setQuantity(quantity - 1);
        }
    };

    const handleQuantityChange = (value) => {
        const newQuantity = parseInt(value.target.value);
        if (newQuantity >= 1) {
            setQuantity(newQuantity);
        }
    };

    // En cours : Connexion backend
    const handleBuy = () => {
        // Quantité invalide
        if (quantity <= 0) return;
    };

    return (
        <>
        <h3>Acheter</h3>
        <div className="purchase-container">
            <div className="purchase-controls">
                <button onClick={handleDecrease}>-</button>
                <input
                    type="number"
                    value={quantity}
                    onChange={handleQuantityChange}
                    min={1}
                />
                <button onClick={handleIncrease}>+</button>
            </div>
            <p>Total : ${totalPrice}</p>
            <button className="buy-button" onClick={handleBuy}>Acheter</button>
        </div>
        </>
    );
};

export default PurchaseBar;
