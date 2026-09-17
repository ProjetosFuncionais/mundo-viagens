/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './lib/auth';
import Layout from './components/Layout';

// Pages
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Checkout from './pages/Checkout';

// Admin Pages
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/Dashboard';
import AdminReservations from './pages/admin/Reservations';
import AdminCustomers from './pages/admin/Customers';
import AdminHotels from './pages/admin/Hotels';

function ProtectedRoute({ children, requireAdmin = false }: { children: React.ReactNode, requireAdmin?: boolean }) {
  const { user, profile, loading } = useAuth();
  const location = useLocation();
  
  if (loading) return <div className="p-8 text-center text-gray-500">Carregando...</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  
  if (requireAdmin) {
    const isAdmin = profile?.role === 'ATENDENTE' || profile?.role === 'GERENTE' || profile?.role === 'DONA';
    if (!isAdmin) return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="login" element={<Login />} />
            <Route path="register" element={<Register />} />
            <Route path="checkout/:flightId" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
            <Route path="profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            
            <Route path="admin" element={<ProtectedRoute requireAdmin><AdminLayout /></ProtectedRoute>}>
              <Route index element={<AdminDashboard />} />
              <Route path="reservations" element={<AdminReservations />} />
              <Route path="customers" element={<AdminCustomers />} />
              <Route path="hotels" element={<AdminHotels />} />
            </Route>
            <Route path="*" element={<div className="p-8 text-center"><h1 className="text-2xl font-bold">Página não encontrada</h1><Link to="/" className="text-blue-600 underline">Voltar para a busca de viagens</Link></div>} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
