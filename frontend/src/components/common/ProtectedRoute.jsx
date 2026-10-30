import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import LoadingSpinner from './LoadingSpinner';

const ProtectedRoute = ({ children, requiredRole }) => {
  const { user, isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <LoadingSpinner text="Authenticating session..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole && user?.role !== requiredRole && user?.role !== 'ROLE_ADMIN') {
    return (
      <div style={{ padding: '40px', textAlign: 'center' }}>
        <h2>Access Restricted</h2>
        <p style={{ color: 'var(--text-muted)', marginTop: '8px' }}>
          Your role ({user?.role}) does not have permission to view this section.
        </p>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;
