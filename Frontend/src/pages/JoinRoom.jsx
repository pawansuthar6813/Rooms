import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import { useRoom } from '../context/RoomContext';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function JoinRoom() {
  const { token } = useParams();
  const navigate = useNavigate();
  const { joinRoom } = useRoom();
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth();
  const [status, setStatus] = useState('joining'); // joining, success, error

  useEffect(() => {
    if (isAuthLoading) return;

    if (!isAuthenticated) {
      toast.error('You must be logged in to join a room.');
      navigate('/login', { state: { returnTo: `/join/${token}` } });
      return;
    }

    const performJoin = async () => {
      try {
        await joinRoom(token);
        setStatus('success');
        toast.success('Successfully joined the room!');
        setTimeout(() => navigate('/dashboard'), 1500);
      } catch (err) {
        setStatus('error');
        toast.error(err || 'Failed to join room. The link may be expired.');
        setTimeout(() => navigate('/dashboard'), 3000);
      }
    };

    performJoin();
  }, [token, isAuthenticated, isAuthLoading, navigate, joinRoom]);

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="bg-white p-8 rounded-2xl shadow-sm border border-slate-200 max-w-sm w-full text-center">
        {status === 'joining' && (
          <div className="flex flex-col items-center">
            <Loader2 size={40} className="text-indigo-600 animate-spin mb-4" />
            <h2 className="text-xl font-bold text-slate-800">Joining Room...</h2>
            <p className="text-slate-500 mt-2 text-sm">Please wait while we add you to the room.</p>
          </div>
        )}
        
        {status === 'success' && (
          <div className="flex flex-col items-center">
            <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
            </div>
            <h2 className="text-xl font-bold text-slate-800">You're in!</h2>
            <p className="text-slate-500 mt-2 text-sm">Redirecting to your dashboard...</p>
          </div>
        )}

        {status === 'error' && (
          <div className="flex flex-col items-center">
            <div className="w-16 h-16 bg-red-100 text-red-600 rounded-full flex items-center justify-center mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            </div>
            <h2 className="text-xl font-bold text-slate-800">Invalid Invite</h2>
            <p className="text-slate-500 mt-2 text-sm">Redirecting to dashboard...</p>
          </div>
        )}
      </div>
    </div>
  );
}
