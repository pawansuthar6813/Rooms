import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useRoom } from '../context/RoomContext';
import { Moon, Sun, LogOut, Plus, Monitor, Loader2, Mail } from 'lucide-react';
import { motion } from 'framer-motion';
import { RoomCard } from '../components/RoomCard';
import { CreateRoomModal } from '../components/CreateRoomModal';
import { RoomDetailPanel } from '../components/RoomDetailPanel';
import toast from 'react-hot-toast';

import { useNavigate } from 'react-router-dom';

export default function Dashboard() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const { rooms, invites, isLoading, fetchRooms, joinRoom } = useRoom();

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [isJoining, setIsJoining] = useState(false);

  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);

  const handleAcceptInvite = async (token) => {
    setIsJoining(true);
    try {
      await joinRoom(token);
      toast.success('Successfully joined the room!');
    } catch (err) {
      toast.error(err || 'Failed to join room');
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-darkBg transition-colors duration-200">
      <nav className="bg-white dark:bg-darkSurface border-b border-slate-200 dark:border-darkBorder">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center space-x-3">
              <Monitor className="w-7 h-7 text-indigo-600 dark:text-accentGold" />
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

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <motion.div 
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex justify-between items-center mb-8"
        >
          <h1 className="text-3xl font-bold text-slate-900 dark:text-white">Your Rooms</h1>
          <button 
            onClick={() => setIsCreateModalOpen(true)}
            className="flex items-center space-x-2 bg-indigo-600 dark:bg-accentGold hover:bg-indigo-700 dark:hover:bg-accentGoldHover text-white dark:text-slate-900 px-4 py-2 rounded-lg font-medium transition-colors shadow-sm"
          >
            <Plus className="w-5 h-5" />
            <span>Create Room</span>
          </button>
        </motion.div>

        {invites?.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="mb-8"
          >
            <h2 className="text-lg font-semibold text-slate-800 dark:text-slate-200 mb-4 flex items-center gap-2">
              <Mail className="w-5 h-5 text-indigo-500 dark:text-accentGold" />
              Pending Invites
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {invites.map(invite => (
                <div key={invite.token} className="bg-white dark:bg-darkCard border border-indigo-100 dark:border-darkBorder rounded-xl p-4 shadow-sm flex flex-col justify-between">
                  <div>
                    <h3 className="font-semibold text-slate-800 dark:text-slate-200">{invite.roomName}</h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Expires: {new Date(invite.expiresAt).toLocaleDateString()}</p>
                  </div>
                  <button
                    onClick={() => handleAcceptInvite(invite.token)}
                    disabled={isJoining}
                    className="mt-4 w-full bg-indigo-50 dark:bg-darkBorder text-indigo-700 dark:text-accentGold hover:bg-indigo-100 dark:hover:bg-slate-700 py-2 rounded-lg text-sm font-medium transition-colors"
                  >
                    Accept Invite
                  </button>
                </div>
              ))}
            </div>
          </motion.div>
        )}

        {isLoading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="w-8 h-8 text-indigo-600 dark:text-accentGold animate-spin" />
          </div>
        ) : rooms.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {rooms.map(room => (
              <RoomCard 
                key={room.id} 
                room={room} 
                onClick={(r) => navigate(`/rooms/${r.id}`)}
                onSettingsClick={(r) => setSelectedRoom(r)}
              />
            ))}
          </div>
        ) : (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.2, duration: 0.5 }}
            className="bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder border-dashed rounded-2xl p-12 text-center"
          >
            <div className="w-16 h-16 bg-indigo-50 dark:bg-darkBg rounded-full flex items-center justify-center mx-auto mb-4">
              <Monitor className="w-8 h-8 text-indigo-600 dark:text-accentGold" />
            </div>
            <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No rooms yet</h3>
            <p className="text-slate-500 dark:text-slate-400 max-w-md mx-auto mb-6">
              Get started by creating a new room for your team to collaborate in real-time.
            </p>
            <button 
              onClick={() => setIsCreateModalOpen(true)}
              className="bg-white dark:bg-darkBg text-indigo-600 dark:text-accentGold border border-slate-200 dark:border-darkBorder hover:bg-slate-50 dark:hover:bg-darkBorder/50 px-6 py-2.5 rounded-lg font-medium transition-colors"
            >
              Create your first room
            </button>
          </motion.div>
        )}
      </main>

      <CreateRoomModal 
        isOpen={isCreateModalOpen} 
        onClose={() => setIsCreateModalOpen(false)} 
      />
      
      <RoomDetailPanel 
        room={selectedRoom} 
        isOpen={!!selectedRoom} 
        onClose={() => setSelectedRoom(null)} 
      />
    </div>
  );
}
