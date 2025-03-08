import {BrowserRouter as Router, Routes, Route} from 'react-router-dom';
import StockPage from './pages/StockPage';
import HomePage from './pages/HomePage'

function App() {
  return(
    <Router>
      <Routes>
        <Route path='/' element={<HomePage />}/>
        <Route path='/asset/:symbol' element={<StockPage />}/>
      </Routes>
    </Router>
  )
}

export default App;