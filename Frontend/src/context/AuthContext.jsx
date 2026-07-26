import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../lib/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchUser = async () => {
    try {
      const response = await api.get('/auth/me');
      if (response.data?.success) {
        setUser(response.data.data);
      } else {
        setUser(null);
      }
    } catch (error) {
      console.error("[AuthContext] fetchUser error:", error.response?.data || error.message);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const login = async (emailOrUsername, password) => {
    try {
      const response = await api.post('/auth/login', { emailOrUsername, password });
      if (response.data?.success) {
        await fetchUser();
      }
      return response.data;
    } catch (error) {
      console.error("[AuthContext] login error:", error.response?.data || error.message);
      throw error;
    }
  };
  
  const signupEmail = async (email, password) => {
    try {
      const response = await api.post('/auth/signup/email', { email, password });
      if (response.data?.success) {
        await fetchUser();
      }
      return response.data;
    } catch (error) {
      console.error("[AuthContext] signupEmail error:", error.response?.data || error.message);
      throw error;
    }
  };
  
  const signupUsername = async (username) => {
    try {
      const response = await api.post('/auth/signup/username', { username });
      if (response.data?.success) {
        await fetchUser();
      }
      return response.data;
    } catch (error) {
      console.error("[AuthContext] signupUsername error:", error.response?.data || error.message);
      throw error;
    }
  };

  const loginWithGoogle = async (idToken) => {
    try {
      const response = await api.post('/auth/oauth/google', { idToken });
      if (response.data?.success) {
        await fetchUser();
      }
      return response.data;
    } catch (error) {
      console.error("[AuthContext] loginWithGoogle error:", error.response?.data || error.message);
      throw error;
    }
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error("[AuthContext] logout error:", error.response?.data || error.message);
    } finally {
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, loginWithGoogle, signupEmail, signupUsername, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
