import React from 'react';
import { Link } from 'react-router-dom';
import { useTheme } from '../context/ThemeContext';
import { Moon, Sun, Monitor, Users, Layers, Zap } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Landing() {
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-darkBg text-slate-900 dark:text-slate-100 transition-colors duration-200">
      <header className="px-8 py-6 flex justify-between items-center border-b border-slate-200 dark:border-darkBorder">
        <div className="flex items-center space-x-2">
          <Monitor className="w-8 h-8 text-blue-600 dark:text-blue-500" />
          <span className="text-2xl font-bold tracking-tight">Rooms</span>
        </div>
        <div className="flex items-center space-x-6">
          <button onClick={toggleTheme} className="p-2 rounded-full hover:bg-slate-200 dark:hover:bg-darkSurface transition-colors">
            {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
          </button>
          <Link to="/login" className="font-medium hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
            Log in
          </Link>
          <Link to="/signup" className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors">
            Sign up
          </Link>
        </div>
      </header>

      <main className="flex-1 flex flex-col items-center justify-center px-4 text-center">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="max-w-3xl space-y-8"
        >
          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight">
            The workspace for <span className="text-blue-600 dark:text-blue-500">creative teams</span>
          </h1>
          <p className="text-lg md:text-xl text-slate-600 dark:text-slate-400 max-w-2xl mx-auto">
            Persistent real-time collaborative whiteboards, instant chat, and file sharing in one unified experience.
          </p>
          <div className="flex justify-center space-x-4 pt-4">
            <Link to="/signup" className="px-8 py-4 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-semibold text-lg transition-colors">
              Get Started for Free
            </Link>
          </div>
        </motion.div>

        <motion.div 
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.2 }}
          className="mt-20 grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl w-full"
        >
          <div className="p-8 rounded-2xl bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder text-left">
            <div className="w-12 h-12 rounded-lg bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center mb-6">
              <Layers className="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Infinite Canvas</h3>
            <p className="text-slate-600 dark:text-slate-400">Create boundless diagrams, mind maps, and wireframes with our real-time collaborative board.</p>
          </div>
          <div className="p-8 rounded-2xl bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder text-left">
            <div className="w-12 h-12 rounded-lg bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center mb-6">
              <Users className="w-6 h-6 text-emerald-600 dark:text-emerald-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Persistent Rooms</h3>
            <p className="text-slate-600 dark:text-slate-400">Your work is always saved. Invite your team, jump into a room, and pick up right where you left off.</p>
          </div>
          <div className="p-8 rounded-2xl bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder text-left">
            <div className="w-12 h-12 rounded-lg bg-purple-100 dark:bg-purple-900/30 flex items-center justify-center mb-6">
              <Zap className="w-6 h-6 text-purple-600 dark:text-purple-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Instant Sync</h3>
            <p className="text-slate-600 dark:text-slate-400">Changes reflect instantly across all devices. Built for speed and reliability from the ground up.</p>
          </div>
        </motion.div>
      </main>

      <footer className="py-8 text-center text-slate-500 dark:text-slate-400 border-t border-slate-200 dark:border-darkBorder mt-20">
        <p>&copy; {new Date().getFullYear()} Rooms. All rights reserved.</p>
      </footer>
    </div>
  );
}
