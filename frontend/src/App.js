import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import StockPage from './pages/StockPage';
import HomePage from './pages/HomePage';
import SigninPage from "./pages/SigninPage";  
import SignupPage from "./pages/SignupPage"; 
import SignedInPage from "./pages/SignedInPage"; 

function App() {
  return(
    <Router>
      <Routes>
        <Route path='/' element={<HomePage/>}/>
        <Route path='/signin' element={<SigninPage/>}/>
        <Route path='/signup' element={<SignupPage/>}/>
        <Route path="/signed-in" element={<SignedInPage />} />
        <Route path='/asset/:symbol' element={<StockPage/>}/>
      </Routes>
    </Router>
  )
}

export default App;