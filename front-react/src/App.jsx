import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SearchPage from './SearchPage';
import DetailPage from './DetailPage';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<SearchPage />} />
        <Route path="/detail/:id" element={<DetailPage />} />
      </Routes>
    </Router>
  );
}

export default App;
