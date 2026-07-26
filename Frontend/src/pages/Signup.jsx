import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { Moon, Sun, Monitor, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { GoogleLogin } from '@react-oauth/google';

export default function Signup() {
  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { signupEmail, signupUsername, loginWithGoogle, user } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  useEffect(() => {
    if (user && !user.username) {
      setStep(2);
    }
  }, [user]);

  const handleEmailSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      const res = await signupEmail(email, password);
      if (res.success) {
        setStep(2);
      } else {
        setError(res.message || 'Signup failed');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred during signup');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUsernameSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      const res = await signupUsername(username);
      if (res.success) {
        navigate('/dashboard');
      } else {
        setError(res.message || 'Failed to set username');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex bg-slate-50 dark:bg-darkBg transition-colors duration-200">
      {/* Theme Toggle */}
      <div className="absolute top-6 right-8 z-10">
        <button onClick={toggleTheme} className="p-2 rounded-full bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder hover:bg-slate-100 dark:hover:bg-darkBorder transition-colors">
          {theme === 'light' ? <Moon className="w-5 h-5 text-slate-800" /> : <Sun className="w-5 h-5 text-slate-200" />}
        </button>
      </div>

      <div className="flex-1 flex flex-col justify-center px-4 sm:px-6 lg:px-20 xl:px-24">
        <div className="mx-auto w-full max-w-sm lg:w-96">
          <Link to="/" className="flex items-center space-x-2 mb-8">
            <Monitor className="w-8 h-8 text-blue-600 dark:text-blue-500" />
            <span className="text-2xl font-bold text-slate-900 dark:text-slate-100">Rooms</span>
          </Link>

          <AnimatePresence mode="wait">
            {step === 1 ? (
              <motion.div
                key="step1"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                transition={{ duration: 0.3 }}
              >
                <h2 className="text-3xl font-extrabold text-slate-900 dark:text-white">Create an account</h2>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
                  Already have an account?{' '}
                  <Link to="/login" className="font-medium text-blue-600 dark:text-blue-400 hover:underline">
                    Log in
                  </Link>
                </p>

                <div className="mt-8">
                  {error && (
                    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mb-4 p-3 rounded bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 text-sm border border-red-200 dark:border-red-900/50">
                      {error}
                    </motion.div>
                  )}

                  <form onSubmit={handleEmailSubmit} className="space-y-5">
                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">Email address</label>
                      <input
                        type="email"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="mt-1 block w-full px-4 py-3 bg-white dark:bg-darkSurface border border-slate-300 dark:border-darkBorder rounded-lg shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:text-white transition-colors"
                        placeholder="you@example.com"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">Password</label>
                      <input
                        type="password"
                        required
                        minLength={8}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="mt-1 block w-full px-4 py-3 bg-white dark:bg-darkSurface border border-slate-300 dark:border-darkBorder rounded-lg shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:text-white transition-colors"
                      />
                    </div>

                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="w-full flex justify-center items-center px-4 py-3 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
                    >
                      {isSubmitting ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Continue'}
                    </button>
                  </form>

                  <div className="mt-6">
                    <div className="relative">
                      <div className="absolute inset-0 flex items-center">
                        <div className="w-full border-t border-slate-300 dark:border-darkBorder" />
                      </div>
                      <div className="relative flex justify-center text-sm">
                        <span className="px-2 bg-slate-50 dark:bg-darkBg text-slate-500">Or</span>
                      </div>
                    </div>
                    <div className="mt-6 flex justify-center">
                      <GoogleLogin
                        onSuccess={async (credentialResponse) => {
                          try {
                            setError('');
                            setIsSubmitting(true);
                            const res = await loginWithGoogle(credentialResponse.credential);
                            if (res.success) {
                              if (!res.data?.username) {
                                setStep(2);
                              } else {
                                navigate('/dashboard');
                              }
                            } else {
                              setError(res.message || 'Google Signup failed');
                            }
                          } catch (err) {
                            setError(err.response?.data?.message || 'An error occurred during Google signup');
                          } finally {
                            setIsSubmitting(false);
                          }
                        }}
                        onError={() => {
                          setError('Google Signup Failed');
                        }}
                        theme={theme === 'dark' ? 'filled_black' : 'outline'}
                        size="large"
                        width="100%"
                        text="signup_with"
                      />
                    </div>
                  </div>
                </div>
              </motion.div>
            ) : (
              <motion.div
                key="step2"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3 }}
              >
                <h2 className="text-3xl font-extrabold text-slate-900 dark:text-white">Choose a username</h2>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-400">
                  This is how others will identify you in Rooms.
                </p>

                <div className="mt-8">
                  {error && (
                    <div className="mb-4 p-3 rounded bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 text-sm border border-red-200 dark:border-red-900/50">
                      {error}
                    </div>
                  )}

                  <form onSubmit={handleUsernameSubmit} className="space-y-5">
                    <div>
                      <input
                        type="text"
                        required
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="mt-1 block w-full px-4 py-3 bg-white dark:bg-darkSurface border border-slate-300 dark:border-darkBorder rounded-lg shadow-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 dark:text-white transition-colors"
                        placeholder="e.g. johndoe"
                      />
                    </div>
                    <button
                      type="submit"
                      disabled={isSubmitting}
                      className="w-full flex justify-center items-center px-4 py-3 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
                    >
                      {isSubmitting ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Complete Setup'}
                    </button>
                  </form>
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>
      </div>
      
      {/* Decorative background */}
      <div className="hidden lg:block relative w-0 flex-1 bg-slate-200 dark:bg-[#151515]">
        <div className="absolute inset-0 flex items-center justify-center">
           <Monitor className="w-64 h-64 text-slate-300 dark:text-darkBorder" />
        </div>
      </div>
    </div>
  );
}
