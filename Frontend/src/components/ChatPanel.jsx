import React, { useEffect, useState, useRef } from 'react';
import { X, Send, File as FileIcon, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function ChatPanel({ roomId, isOpen, onClose, zIndex, onFocus }) {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [typingUsers, setTypingUsers] = useState(new Set());
  const [isLoading, setIsLoading] = useState(true);
  const clientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const typingTimeoutRef = useRef(null);
  const typingClearTimeoutsRef = useRef({});

  useEffect(() => {
    if (isOpen) fetchHistory();
  }, [roomId, isOpen]);

  useEffect(() => {
    if (!isOpen) return;

    const socketUrl = import.meta.env.VITE_API_URL?.replace('/api/v1', '/ws/chat') || 'http://localhost:8080/ws/chat';
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl, null, { withCredentials: true }),
      debug: (str) => console.log('Chat STOMP:', str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      // Subscribe to messages
      client.subscribe(`/topic/chat/${roomId}`, (message) => {
        const newMsg = JSON.parse(message.body);
        setMessages((prev) => [...prev, newMsg]);
        scrollToBottom();
      });

      // Subscribe to typing indicators
      client.subscribe(`/topic/chat/${roomId}/typing`, (message) => {
        const { userId, username, typing } = JSON.parse(message.body);
        if (userId === user.id) return; // ignore own

        setTypingUsers((prev) => {
          const newSet = new Set(prev);
          if (typing) {
            newSet.add(username);
            // Auto clear after 3s
            if (typingClearTimeoutsRef.current[userId]) {
              clearTimeout(typingClearTimeoutsRef.current[userId]);
            }
            typingClearTimeoutsRef.current[userId] = setTimeout(() => {
               setTypingUsers(currentSet => {
                  const s = new Set(currentSet);
                  s.delete(username);
                  return s;
               });
            }, 3000);
          } else {
            newSet.delete(username);
          }
          return newSet;
        });
      });
    };

    client.onStompError = (frame) => {
      console.error('Chat Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      Object.values(typingClearTimeoutsRef.current).forEach(clearTimeout);
    };
  }, [roomId, isOpen, user.id]);

  const fetchHistory = async () => {
    setIsLoading(true);
    try {
      const res = await api.get(`/chat/${roomId}/messages?limit=50`);
      setMessages(res.data.data.reverse()); 
      setIsLoading(false);
      scrollToBottom();
    } catch (err) {
      toast.error('Failed to load chat history');
      setIsLoading(false);
    }
  };

  const scrollToBottom = () => {
    setTimeout(() => {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  };

  const handleSend = (e) => {
    e.preventDefault();
    if (!inputValue.trim() || !clientRef.current?.connected) return;

    clientRef.current.publish({
      destination: `/app/chat/${roomId}/send`,
      body: JSON.stringify({ content: inputValue, type: 'TEXT' }),
    });

    setInputValue('');
    sendTypingStatus(false);
  };

  const sendTypingStatus = (typing) => {
    if (!clientRef.current?.connected) return;
    clientRef.current.publish({
      destination: `/app/chat/${roomId}/typing`,
      body: JSON.stringify({ typing }),
    });
  };

  const handleInputChange = (e) => {
    setInputValue(e.target.value);
    
    if (!isTyping) {
      setIsTyping(true);
      sendTypingStatus(true);
    }

    if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
    
    typingTimeoutRef.current = setTimeout(() => {
      setIsTyping(false);
      sendTypingStatus(false);
    }, 1500);
  };
  
  const typingArray = Array.from(typingUsers);
  let typingText = '';
  if (typingArray.length === 1) typingText = `${typingArray[0]} is typing...`;
  else if (typingArray.length === 2) typingText = `${typingArray[0]} and ${typingArray[1]} are typing...`;
  else if (typingArray.length > 2) typingText = 'Several people are typing...';

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ x: 320, opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          exit={{ x: 320, opacity: 0 }}
          transition={{ type: 'spring', damping: 25, stiffness: 200 }}
          style={{ zIndex }}
          onPointerDown={onFocus}
          className="absolute top-4 bottom-16 right-4 w-80 bg-white dark:bg-darkCard border border-slate-200 dark:border-darkBorder rounded-xl shadow-[0_8px_30px_rgb(0,0,0,0.12)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.4)] flex flex-col overflow-hidden pointer-events-auto"
        >
          {/* Header */}
          <div className="h-12 border-b border-slate-200 dark:border-darkBorder flex items-center justify-between px-4 shrink-0 bg-slate-50 dark:bg-darkCard cursor-default">
            <h3 className="font-semibold text-slate-800 dark:text-slate-200">Room Chat</h3>
            <button onClick={onClose} className="p-1 rounded-md hover:bg-slate-200 dark:hover:bg-darkBorder text-slate-500 transition-colors">
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-slate-50/50 dark:bg-darkBg">
            {isLoading ? (
              <div className="flex justify-center items-center h-full">
                <Loader2 className="w-6 h-6 animate-spin text-indigo-500 dark:text-accentGold" />
              </div>
            ) : (
              messages.map((msg, i) => {
                const isMe = msg.authorId === user.id;
                
                return (
                  <div key={msg.id || i} className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                    {!isMe && (
                      <span className="text-[10px] text-slate-500 mb-1 ml-1">{msg.authorUsername}</span>
                    )}
                    
                    <div className={`px-3 py-2 rounded-2xl max-w-[85%] text-sm ${
                      isMe 
                        ? 'bg-indigo-600 dark:bg-accentGold text-white dark:text-slate-900 rounded-br-sm shadow-sm' 
                        : 'bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder text-slate-800 dark:text-slate-200 rounded-bl-sm shadow-sm'
                    }`}>
                      {msg.type === 'FILE_REF' ? (
                        <div className="flex items-center gap-2">
                           <FileIcon className="w-4 h-4 opacity-80" />
                           <a href={msg.content} target="_blank" rel="noopener noreferrer" className="underline underline-offset-2 hover:opacity-80 transition-opacity">
                              View File
                           </a>
                        </div>
                      ) : (
                        msg.content
                      )}
                    </div>
                  </div>
                );
              })
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Typing Indicator */}
          <div className="px-4 py-1 h-6 text-[11px] text-slate-500 italic bg-white dark:bg-darkCard border-t border-slate-100 dark:border-darkBorder/50">
            {typingText}
          </div>

          {/* Input Area */}
          <div className="p-3 bg-white dark:bg-darkCard border-t border-slate-200 dark:border-darkBorder">
            <form onSubmit={handleSend} className="flex gap-2">
              <input
                type="text"
                value={inputValue}
                onChange={handleInputChange}
                placeholder="Type a message..."
                className="flex-1 bg-slate-100 dark:bg-darkSurface border border-transparent dark:border-darkBorder rounded-full px-4 py-2 text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-indigo-500 dark:focus:ring-accentGold outline-none"
              />
              <button 
                type="submit"
                disabled={!inputValue.trim()}
                className="p-2 bg-indigo-600 dark:bg-accentGold text-white dark:text-slate-900 rounded-full hover:bg-indigo-700 dark:hover:bg-accentGoldHover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
