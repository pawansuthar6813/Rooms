import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, UserPlus, Settings, LogOut, Shield, Trash2, Mail } from 'lucide-react';
import { useRoom } from '../context/RoomContext';
import { Avatar } from './Avatar';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';

export function RoomDetailPanel({ room, isOpen, onClose }) {
  const { inviteMember, removeMember, changeRole, deleteRoom } = useRoom();
  const { user } = useAuth();
  const [inviteEmail, setInviteEmail] = useState('');
  const [isInviting, setIsInviting] = useState(false);

  if (!room) return null;

  const amIOwner = room.ownerId === user?.id;

  const handleInvite = async (e) => {
    e.preventDefault();
    if (!inviteEmail.trim()) return;
    
    setIsInviting(true);
    try {
      await inviteMember(room.id, inviteEmail);
      toast.success(`Invite sent to ${inviteEmail}`);
      setInviteEmail('');
    } catch (err) {
      toast.error(err || 'Failed to send invite');
    } finally {
      setIsInviting(false);
    }
  };

  const handleRemove = async (memberId) => {
    if (!window.confirm('Are you sure you want to remove this member?')) return;
    try {
      await removeMember(room.id, memberId);
      toast.success('Member removed');
    } catch (err) {
      toast.error(err || 'Failed to remove member');
    }
  };

  const handleRoleChange = async (memberId, newRole) => {
    try {
      await changeRole(room.id, memberId, newRole);
      toast.success('Role updated successfully');
    } catch (err) {
      toast.error(err || 'Failed to change role');
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-slate-900/20 backdrop-blur-sm z-40"
            onClick={onClose}
          />
          <motion.div
            initial={{ x: '100%' }}
            animate={{ x: 0 }}
            exit={{ x: '100%' }}
            transition={{ type: 'spring', damping: 25, stiffness: 200 }}
            className="fixed top-0 right-0 h-full w-full max-w-md bg-white shadow-2xl z-50 flex flex-col"
          >
            <div className="flex items-center justify-between p-6 border-b border-slate-100 bg-slate-50/50">
              <div>
                <h2 className="text-xl font-bold text-slate-800">{room.name}</h2>
                <p className="text-sm text-slate-500 mt-1">Room Settings</p>
              </div>
              <button
                onClick={onClose}
                className="text-slate-400 hover:text-slate-600 hover:bg-white p-2 rounded-full transition-colors shadow-sm border border-transparent hover:border-slate-200"
              >
                <X size={20} />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto">
              <div className="p-6">
                <div className="mb-8">
                  <h3 className="text-sm font-semibold text-slate-900 uppercase tracking-wider mb-2">Description</h3>
                  <p className="text-slate-600 text-sm bg-slate-50 p-4 rounded-lg border border-slate-100">
                    {room.description || "No description provided."}
                  </p>
                </div>

                {amIOwner && (
                  <div className="mb-8">
                    <h3 className="text-sm font-semibold text-slate-900 uppercase tracking-wider mb-4 flex items-center gap-2">
                      <UserPlus size={16} className="text-indigo-500" />
                      Invite People
                    </h3>
                    <form onSubmit={handleInvite} className="flex gap-2">
                      <div className="relative flex-1">
                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                        <input
                          type="email"
                          value={inviteEmail}
                          onChange={(e) => setInviteEmail(e.target.value)}
                          placeholder="Email address"
                          className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none text-sm transition-all"
                          required
                        />
                      </div>
                      <button
                        type="submit"
                        disabled={isInviting || !inviteEmail.trim()}
                        className="px-4 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors whitespace-nowrap"
                      >
                        {isInviting ? 'Sending...' : 'Send Invite'}
                      </button>
                    </form>
                  </div>
                )}

                <div>
                  <h3 className="text-sm font-semibold text-slate-900 uppercase tracking-wider mb-4 flex items-center gap-2">
                    <UsersIcon size={16} className="text-slate-500" />
                    Members ({room.members?.length || 0})
                  </h3>
                  <div className="space-y-3">
                    {room.members?.map((member) => (
                      <div key={member.userId} className="flex items-center justify-between p-3 rounded-xl border border-slate-100 hover:border-slate-200 hover:bg-slate-50 transition-colors group">
                        <div className="flex items-center gap-3 overflow-hidden">
                          <Avatar name={member.username || member.email} />
                          <div className="truncate">
                            <p className="text-sm font-medium text-slate-900 truncate">
                              {member.username || 'Pending User'}
                              {member.userId === user?.id && <span className="ml-2 text-xs bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full font-semibold">You</span>}
                            </p>
                            <p className="text-xs text-slate-500 truncate">{member.email}</p>
                          </div>
                        </div>
                        
                        <div className="flex items-center gap-2 shrink-0 ml-2">
                          {amIOwner && member.userId !== user?.id ? (
                            <select
                              value={member.role}
                              onChange={(e) => handleRoleChange(member.userId, e.target.value)}
                              className="text-xs border-slate-200 rounded-md bg-white text-slate-600 py-1 pl-2 pr-6 focus:ring-indigo-500 focus:border-indigo-500"
                            >
                              <option value="EDITOR">Editor</option>
                              <option value="VIEWER">Viewer</option>
                            </select>
                          ) : (
                            <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded-md flex items-center gap-1">
                              {member.role === 'OWNER' && <Shield size={12} className="text-amber-500" />}
                              {member.role}
                            </span>
                          )}

                          {amIOwner && member.userId !== user?.id && (
                            <button
                              onClick={() => handleRemove(member.userId)}
                              className="text-slate-400 hover:text-red-600 p-1.5 rounded-md hover:bg-red-50 transition-colors opacity-0 group-hover:opacity-100"
                              title="Remove member"
                            >
                              <Trash2 size={16} />
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {amIOwner && (
                  <div className="mt-12 pt-6 border-t border-red-100">
                    <h3 className="text-sm font-semibold text-red-600 uppercase tracking-wider mb-2">Danger Zone</h3>
                    <div className="bg-red-50 p-4 rounded-xl border border-red-100 flex items-center justify-between">
                      <div>
                        <h4 className="text-sm font-bold text-red-900">Delete Room</h4>
                        <p className="text-xs text-red-700 mt-1">This action cannot be undone.</p>
                      </div>
                      <button
                        onClick={async () => {
                          if (window.confirm('Are you absolutely sure you want to delete this room? This will permanently delete the room, all members, and all data within it.')) {
                            try {
                              await deleteRoom(room.id);
                              toast.success('Room deleted successfully');
                              onClose();
                            } catch (err) {
                              toast.error(err || 'Failed to delete room');
                            }
                          }
                        }}
                        className="px-4 py-2 bg-red-100 text-red-700 hover:bg-red-200 text-sm font-semibold rounded-lg transition-colors border border-red-200"
                      >
                        Delete Room
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}

function UsersIcon(props) {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" {...props}>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
      <circle cx="9" cy="7" r="4"></circle>
      <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
    </svg>
  );
}
