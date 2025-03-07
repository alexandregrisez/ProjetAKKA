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

    // En cours : connexion au backend
    const handleSell = () => {
        //Quantité invalide
        if (quantity <= 0) return; 
    };

    return (
        <>
            <h3>Vendre {symbol}</h3>
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
        </>
    );
};

export default SellBar;
