import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('handoffiq_token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('handoffiq_token');
      const storedUser = localStorage.getItem('handoffiq_user');

      if (storedToken && storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          setToken(storedToken);
          // Verify with backend
          const res = await authApi.getMe();
          if (res.data) {
            setUser(res.data);
            localStorage.setItem('handoffiq_user', JSON.stringify(res.data));
          }
        } catch (err) {
          console.warn('Session verification failed, logging out', err);
          logout();
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (identifier, password) => {
    const cleanId = identifier ? String(identifier).trim() : '';
    const res = await authApi.login({ email: cleanId, username: cleanId, password });
    const { token, ...userData } = res.data;
    localStorage.setItem('handoffiq_token', token);
    localStorage.setItem('handoffiq_user', JSON.stringify(userData));
    setToken(token);
    setUser(userData);
    return userData;
  };

  const register = async (data) => {
    const res = await authApi.register(data);
    const { token, ...userData } = res.data;
    localStorage.setItem('handoffiq_token', token);
    localStorage.setItem('handoffiq_user', JSON.stringify(userData));
    setToken(token);
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('handoffiq_token');
    localStorage.removeItem('handoffiq_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
