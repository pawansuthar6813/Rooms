import { createContext, useContext, useState, useCallback } from 'react';
import api from '../lib/api';

const RoomContext = createContext(null);

export const RoomProvider = ({ children }) => {
  const [rooms, setRooms] = useState([]);
  const [invites, setInvites] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchRooms = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.get('/rooms');
      setRooms(response.data.data || []);
      
      const invitesResponse = await api.get('/rooms/invites');
      setInvites(invitesResponse.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch rooms');
      console.error('Error fetching rooms:', err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createRoom = async (name, description) => {
    try {
      const response = await api.post('/rooms', { name, description });
      const newRoom = response.data.data;
      setRooms(prev => [...prev, newRoom]);
      return newRoom;
    } catch (err) {
      console.error('Error creating room:', err);
      throw err.response?.data?.message || 'Failed to create room';
    }
  };

  const deleteRoom = async (roomId) => {
    try {
      await api.delete(`/rooms/${roomId}`);
      setRooms(prev => prev.filter(r => r.id !== roomId));
    } catch (err) {
      console.error('Error deleting room:', err);
      throw err.response?.data?.message || 'Failed to delete room';
    }
  };

  const inviteMember = async (roomId, email) => {
    try {
      await api.post(`/rooms/${roomId}/invite`, null, { params: { email } });
    } catch (err) {
      console.error('Error sending invite:', err);
      throw err.response?.data?.message || 'Failed to send invite';
    }
  };

  const removeMember = async (roomId, memberId) => {
    try {
      await api.delete(`/rooms/${roomId}/members/${memberId}`);
      setRooms(prev => prev.map(room => {
        if (room.id === roomId) {
          return { ...room, members: room.members.filter(m => m.userId !== memberId) };
        }
        return room;
      }));
    } catch (err) {
      console.error('Error removing member:', err);
      throw err.response?.data?.message || 'Failed to remove member';
    }
  };

  const changeRole = async (roomId, memberId, newRole) => {
    try {
      await api.patch(`/rooms/${roomId}/members/${memberId}/role`, null, { params: { role: newRole } });
      setRooms(prev => prev.map(room => {
        if (room.id === roomId) {
          return {
            ...room,
            members: room.members.map(m => m.userId === memberId ? { ...m, role: newRole } : m)
          };
        }
        return room;
      }));
    } catch (err) {
      console.error('Error changing role:', err);
      throw err.response?.data?.message || 'Failed to change role';
    }
  };

  const joinRoom = async (token) => {
    try {
      await api.post(`/rooms/join/${token}`);
      await fetchRooms(); // Refresh list after joining
    } catch (err) {
      console.error('Error joining room:', err);
      throw err.response?.data?.message || 'Failed to join room';
    }
  };

  const getRoomById = (id) => rooms.find(r => r.id === id);

  const value = {
    rooms,
    invites,
    isLoading,
    error,
    fetchRooms,
    createRoom,
    deleteRoom,
    inviteMember,
    removeMember,
    changeRole,
    joinRoom,
    getRoomById,
  };

  return <RoomContext.Provider value={value}>{children}</RoomContext.Provider>;
};

export const useRoom = () => {
  const context = useContext(RoomContext);
  if (!context) {
    throw new Error('useRoom must be used within a RoomProvider');
  }
  return context;
};
