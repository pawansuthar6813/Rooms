import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { ArrowLeft, Moon, Sun, Plus, MoreVertical, MessageSquare, Folder } from 'lucide-react';
import { motion } from 'framer-motion';
import api from '../lib/api';
import toast from 'react-hot-toast';
import TopicCanvas from '../components/TopicCanvas';
import ActiveUsersBar from '../components/ActiveUsersBar';
import ChatPanel from '../components/ChatPanel';
import FilePanel from '../components/FilePanel';

export default function RoomWorkspace() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();
  
  const [topics, setTopics] = useState([]);
  const [activeTopic, setActiveTopic] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreatingTopic, setIsCreatingTopic] = useState(false);
  const [newTopicName, setNewTopicName] = useState('');

  // Panel states
  const [chatOpen, setChatOpen] = useState(false);
  const [filesOpen, setFilesOpen] = useState(false);
  const [activePanel, setActivePanel] = useState(null); // 'chat' or 'files'

  useEffect(() => {
    fetchTopics();
  }, [roomId]);

  const fetchTopics = async () => {
    try {
      const response = await api.get(`/rooms/${roomId}/topics`);
      const fetchedTopics = response.data.data;
      setTopics(fetchedTopics);
      if (fetchedTopics.length > 0 && !activeTopic) {
        setActiveTopic(fetchedTopics[0]);
      }
    } catch (err) {
      toast.error('Failed to load topics');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateTopic = async (e) => {
    e.preventDefault();
    if (!newTopicName.trim()) return;
    
    try {
      const response = await api.post(`/rooms/${roomId}/topics`, { name: newTopicName });
      const newTopic = response.data.data;
      setTopics([...topics, newTopic]);
      setActiveTopic(newTopic);
      setNewTopicName('');
      setIsCreatingTopic(false);
      toast.success('Topic created');
    } catch (err) {
      toast.error('Failed to create topic');
    }
  };

  const toggleChat = () => {
    if (chatOpen && activePanel === 'chat') {
      setChatOpen(false);
    } else {
      setChatOpen(true);
      setActivePanel('chat');
    }
  };

  const toggleFiles = () => {
    if (filesOpen && activePanel === 'files') {
      setFilesOpen(false);
    } else {
      setFilesOpen(true);
      setActivePanel('files');
    }
  };

  return (
    <div className="h-screen flex flex-col bg-slate-50 dark:bg-darkBg transition-colors duration-200 overflow-hidden">
      {/* Top Navbar */}
      <nav className="h-14 bg-white dark:bg-darkSurface border-b border-slate-200 dark:border-darkBorder flex items-center justify-between px-4 shrink-0 z-10">
        <div className="flex items-center space-x-4">
          <button 
            onClick={() => navigate('/dashboard')}
            className="p-2 -ml-2 rounded-lg hover:bg-slate-100 dark:hover:bg-darkBorder text-slate-600 dark:text-slate-400 transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          
          <div className="h-6 w-px bg-slate-200 dark:bg-darkBorder"></div>
          
          {/* Topic Tabs */}
          <div className="flex items-center space-x-1 overflow-x-auto no-scrollbar">
            {topics.map(topic => (
              <button
                key={topic.id}
                onClick={() => setActiveTopic(topic)}
                className={`px-4 py-1.5 rounded-md text-sm font-medium transition-colors whitespace-nowrap ${
                  activeTopic?.id === topic.id
                    ? 'bg-indigo-50 dark:bg-indigo-500/10 text-indigo-600 dark:text-indigo-400'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-darkBorder/50'
                }`}
              >
                {topic.name}
              </button>
            ))}
            
            {isCreatingTopic ? (
              <form onSubmit={handleCreateTopic} className="flex items-center ml-2">
                <input
                  autoFocus
                  type="text"
                  value={newTopicName}
                  onChange={(e) => setNewTopicName(e.target.value)}
                  onBlur={() => setIsCreatingTopic(false)}
                  placeholder="New topic..."
                  className="px-3 py-1 text-sm border border-indigo-300 dark:border-indigo-500 rounded-md bg-white dark:bg-darkBg text-slate-900 dark:text-white outline-none focus:ring-2 focus:ring-indigo-500/50 w-32"
                />
              </form>
            ) : (
              <button
                onClick={() => setIsCreatingTopic(true)}
                className="p-1.5 ml-1 rounded-md hover:bg-slate-100 dark:hover:bg-darkBorder text-slate-500 dark:text-slate-400"
                title="New Topic"
              >
                <Plus className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>

        <div className="flex items-center space-x-3">
          <button 
            onClick={toggleTheme} 
            className="p-2 rounded-full hover:bg-slate-100 dark:hover:bg-darkBorder transition-colors text-slate-600 dark:text-slate-400"
          >
            {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
          </button>
        </div>
      </nav>

      {/* Main Workspace */}
      <div className="flex-1 flex overflow-hidden relative">
        <div className="flex-1 relative bg-[#fafafa] dark:bg-[#121212]">
          {/* Tldraw Canvas */}
          {activeTopic ? (
            <TopicCanvas key={activeTopic.id} roomId={roomId} topic={activeTopic} />
          ) : (
            <div className="flex items-center justify-center h-full text-slate-500 dark:text-slate-400">
              {isLoading ? 'Loading topics...' : 'Create or select a topic to start drawing.'}
            </div>
          )}
        </div>
        
        {/* Sliding Panels Overlay */}
        <div className="absolute inset-y-0 right-0 pointer-events-none w-0 z-[9999]">
           <ChatPanel 
             roomId={roomId} 
             isOpen={chatOpen} 
             onClose={() => setChatOpen(false)} 
             zIndex={activePanel === 'chat' ? 50 : 40}
             onFocus={() => setActivePanel('chat')}
           />
           <FilePanel 
             roomId={roomId} 
             isOpen={filesOpen} 
             onClose={() => setFilesOpen(false)} 
             zIndex={activePanel === 'files' ? 50 : 40}
             onFocus={() => setActivePanel('files')}
           />
        </div>
      </div>

      {/* Bottom Navbar (Presence & Tools) */}
      <div className="h-12 bg-white dark:bg-darkSurface border-t border-slate-200 dark:border-darkBorder flex items-center justify-between px-4 shrink-0 z-[60]">
        <div className="flex items-center">
          <ActiveUsersBar roomId={roomId} />
        </div>
        
        <div className="flex items-center space-x-2">
          <button 
            onClick={toggleFiles}
            className={`p-2 rounded-lg transition-colors flex items-center gap-2 ${
              filesOpen 
                ? 'bg-indigo-50 dark:bg-indigo-500/20 text-indigo-600 dark:text-indigo-400' 
                : 'hover:bg-slate-100 dark:hover:bg-darkBorder text-slate-600 dark:text-slate-400'
            }`}
          >
            <Folder className="w-4 h-4" />
            <span className="text-sm font-medium">Files</span>
          </button>
          <button 
            onClick={toggleChat}
            className={`p-2 rounded-lg transition-colors flex items-center gap-2 ${
              chatOpen 
                ? 'bg-indigo-50 dark:bg-indigo-500/20 text-indigo-600 dark:text-indigo-400' 
                : 'hover:bg-slate-100 dark:hover:bg-darkBorder text-slate-600 dark:text-slate-400'
            }`}
          >
            <MessageSquare className="w-4 h-4" />
            <span className="text-sm font-medium">Chat</span>
          </button>
        </div>
      </div>
    </div>
  );
}
