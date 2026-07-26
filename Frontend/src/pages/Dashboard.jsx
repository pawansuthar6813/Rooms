import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { Moon, Sun, LogOut, Plus, Monitor } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-darkBg transition-colors duration-200">
      {/* Navbar */}
      <nav className="bg-white dark:bg-darkSurface border-b border-slate-200 dark:border-darkBorder">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center space-x-3">
              <Monitor className="w-7 h-7 text-blue-600 dark:text-blue-500" />
              <span className="text-xl font-bold text-slate-900 dark:text-slate-100">Rooms</span>
            </div>
            
            <div className="flex items-center space-x-6">
              <span className="text-sm text-slate-600 dark:text-slate-400 hidden sm:block">
                Welcome, <span className="font-semibold text-slate-900 dark:text-slate-100">@{user?.username}</span>
              </span>
              
              <button 
                onClick={toggleTheme} 
                className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-darkBorder transition-colors text-slate-600 dark:text-slate-400"
                title="Toggle Theme"
              >
                {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
              </button>
              
              <button 
                onClick={logout}
                className="flex items-center space-x-2 text-sm font-medium text-red-600 dark:text-red-400 hover:text-red-700 dark:hover:text-red-300 transition-colors"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:block">Log out</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex justify-between items-center mb-8"
        >
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Your Rooms</h1>
          <button className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium transition-colors shadow-sm">
            <Plus className="w-5 h-5" />
            <span>Create Room</span>
          </button>
        </motion.div>

        {/* Empty State */}
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder border-dashed rounded-2xl p-12 text-center"
        >
          <div className="w-16 h-16 bg-blue-50 dark:bg-blue-900/20 rounded-full flex items-center justify-center mx-auto mb-4">
            <Monitor className="w-8 h-8 text-blue-600 dark:text-blue-500" />
          </div>
          <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No rooms yet</h3>
          <p className="text-slate-500 dark:text-slate-400 max-w-md mx-auto mb-6">
            Get started by creating a new room for your team to collaborate in real-time.
          </p>
          <button className="bg-white dark:bg-darkBg text-blue-600 dark:text-blue-400 border border-slate-200 dark:border-darkBorder hover:bg-slate-50 dark:hover:bg-darkBorder/50 px-6 py-2.5 rounded-lg font-medium transition-colors">
            Create your first room
          </button>
        </motion.div>
      </main>
    </div>
  );
}
