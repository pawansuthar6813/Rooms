import React from 'react';
import { Users, Calendar } from 'lucide-react';
import { motion } from 'framer-motion';

export function RoomCard({ room, onClick, onSettingsClick }) {
  const memberCount = room.members?.length || 0;
  const date = new Date(room.createdAt).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  });

  return (
    <motion.div
      whileHover={{ y: -4, boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)' }}
      className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden cursor-pointer group transition-all"
      onClick={() => onClick(room)}
    >
      <div className="h-2 bg-indigo-600 w-full" />
      <div className="p-5">
        <div className="flex justify-between items-start mb-2">
          <h3 className="text-lg font-semibold text-slate-800 group-hover:text-indigo-600 transition-colors line-clamp-1">
            {room.name}
          </h3>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onSettingsClick(room);
            }}
            className="text-slate-400 hover:text-indigo-600 p-1 rounded hover:bg-indigo-50 transition-colors"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="1"></circle><circle cx="19" cy="12" r="1"></circle><circle cx="5" cy="12" r="1"></circle></svg>
          </button>
        </div>
        
        <p className="text-slate-500 text-sm mb-6 line-clamp-2 min-h-[40px]">
          {room.description || "No description provided."}
        </p>
        
        <div className="flex items-center justify-between text-xs text-slate-500 font-medium">
          <div className="flex items-center gap-1.5 bg-slate-100 px-2.5 py-1 rounded-full">
            <Users size={14} className="text-slate-400" />
            <span>{memberCount} member{memberCount !== 1 ? 's' : ''}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Calendar size={14} className="text-slate-400" />
            <span>{date}</span>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
