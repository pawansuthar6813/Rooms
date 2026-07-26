import React, { useEffect, useState } from 'react';
import { Users } from 'lucide-react';
import api from '../lib/api';

export default function ActiveUsersBar({ roomId }) {
  const [members, setMembers] = useState([]);

  useEffect(() => {
    // In Phase 6, we just fetch members to show avatars.
    // In later phases we could use WebSockets for live presence.
    fetchMembers();
  }, [roomId]);

  const fetchMembers = async () => {
    try {
      const response = await api.get(`/rooms/${roomId}`);
      setMembers(response.data.data.members || []);
    } catch (err) {
      console.error('Failed to fetch room members', err);
    }
  };

  return (
    <div className="flex items-center space-x-2">
      <Users className="w-4 h-4 text-slate-400" />
      <div className="flex -space-x-2 overflow-hidden">
        {members.map(member => (
          <div 
            key={member.id}
            title={member.username}
            className="inline-block h-6 w-6 rounded-full ring-2 ring-white dark:ring-darkSurface bg-indigo-100 dark:bg-indigo-900/50 flex items-center justify-center text-xs font-medium text-indigo-700 dark:text-indigo-300 uppercase"
          >
            {member.username.charAt(0)}
          </div>
        ))}
      </div>
    </div>
  );
}
