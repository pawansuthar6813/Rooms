import React, { useEffect, useState, useRef } from 'react';
import { X, UploadCloud, File as FileIcon, Trash2, Download, Loader2 } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function FilePanel({ roomId, isOpen, onClose, zIndex, onFocus }) {
  const { user } = useAuth();
  const [files, setFiles] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (isOpen) fetchFiles();
  }, [roomId, isOpen]);

  const fetchFiles = async () => {
    setIsLoading(true);
    try {
      const res = await api.get(`/files/rooms/${roomId}`);
      setFiles(res.data.data);
    } catch (err) {
      toast.error('Failed to load files');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Check size limit (e.g. 25MB)
    if (file.size > 25 * 1024 * 1024) {
      toast.error('File exceeds 25MB limit');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setIsUploading(true);
    setUploadProgress(0);

    try {
      const res = await api.post(`/files/rooms/${roomId}/upload`, formData, {
        headers: {
          'Content-Type': undefined, // Remove default application/json so Axios sets multipart/form-data with boundary
        },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percentCompleted);
        },
      });

      setFiles((prev) => [res.data.data, ...prev]);
      toast.success('File uploaded');
      
      // Also send a chat message containing this file
      api.post(`/chat/${roomId}/send`, {
          content: res.data.data.cloudinarySecureUrl,
          type: 'FILE_REF'
      }).catch(err => console.error('Failed to send chat message for file', err));

    } catch (err) {
      toast.error('Upload failed');
    } finally {
      setIsUploading(false);
      setUploadProgress(0);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleDelete = async (fileId) => {
    try {
      await api.delete(`/files/${fileId}`);
      setFiles((prev) => prev.filter(f => f.id !== fileId));
      toast.success('File deleted');
    } catch (err) {
      toast.error('Failed to delete file');
    }
  };

  const formatSize = (bytes) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

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
          className="absolute top-4 bottom-16 right-4 w-80 bg-white dark:bg-darkSurface border border-slate-200 dark:border-darkBorder rounded-xl shadow-[0_8px_30px_rgb(0,0,0,0.12)] dark:shadow-[0_8px_30px_rgb(0,0,0,0.4)] flex flex-col overflow-hidden pointer-events-auto"
        >
          {/* Header */}
          <div className="h-12 border-b border-slate-200 dark:border-darkBorder flex items-center justify-between px-4 shrink-0 bg-slate-50 dark:bg-darkSurface cursor-default">
            <h3 className="font-semibold text-slate-800 dark:text-slate-200">Room Files</h3>
            <button onClick={onClose} className="p-1 rounded-md hover:bg-slate-200 dark:hover:bg-darkBorder text-slate-500 transition-colors">
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Upload Zone */}
          <div className="p-4 border-b border-slate-200 dark:border-darkBorder bg-slate-50/50 dark:bg-darkBg/50">
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileUpload}
              className="hidden"
            />
            <div 
              onClick={() => !isUploading && fileInputRef.current?.click()}
              className={`border-2 border-dashed rounded-lg p-4 flex flex-col items-center justify-center text-center cursor-pointer transition-colors ${
                isUploading 
                  ? 'border-indigo-300 bg-indigo-50 dark:border-indigo-500/30 dark:bg-indigo-500/10' 
                  : 'border-slate-300 dark:border-darkBorder hover:border-indigo-400 dark:hover:border-indigo-500 hover:bg-slate-50 dark:hover:bg-darkBorder/50'
              }`}
            >
              {isUploading ? (
                <>
                  <Loader2 className="w-6 h-6 text-indigo-500 animate-spin mb-2" />
                  <span className="text-sm font-medium text-slate-700 dark:text-slate-300">Uploading... {uploadProgress}%</span>
                  <div className="w-full h-1.5 bg-slate-200 dark:bg-darkBorder rounded-full mt-2 overflow-hidden">
                    <div 
                      className="h-full bg-indigo-500 transition-all duration-300" 
                      style={{ width: `${uploadProgress}%` }}
                    />
                  </div>
                </>
              ) : (
                <>
                  <UploadCloud className="w-6 h-6 text-slate-400 mb-2" />
                  <span className="text-sm font-medium text-slate-700 dark:text-slate-300">Click to upload file</span>
                  <span className="text-xs text-slate-500 mt-1">Max 25MB</span>
                </>
              )}
            </div>
          </div>

          {/* Files List */}
          <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-white dark:bg-darkSurface">
            {isLoading ? (
              <div className="flex justify-center items-center h-20">
                <Loader2 className="w-6 h-6 animate-spin text-indigo-500" />
              </div>
            ) : files.length === 0 ? (
              <div className="text-center text-slate-500 dark:text-slate-400 text-sm mt-4">
                No files uploaded yet.
              </div>
            ) : (
              files.map(file => (
                <div key={file.id} className="flex items-start gap-3 p-3 rounded-lg border border-slate-100 dark:border-darkBorder/50 hover:bg-slate-50 dark:hover:bg-darkBorder/30 transition-colors group">
                  <div className="p-2 bg-indigo-100 dark:bg-indigo-500/20 text-indigo-600 dark:text-indigo-400 rounded-md shrink-0">
                    <FileIcon className="w-5 h-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-slate-800 dark:text-slate-200 truncate" title={file.originalName}>
                      {file.originalName}
                    </p>
                    <div className="flex items-center gap-2 mt-0.5 text-xs text-slate-500">
                      <span>{formatSize(file.size)}</span>
                      <span>•</span>
                      <span>{new Date(file.uploadedAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                  <div className="flex flex-col gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <a 
                      href={file.cloudinarySecureUrl} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="p-1.5 text-slate-500 hover:text-indigo-600 dark:hover:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-500/10 rounded-md transition-colors"
                      title="Download/Open"
                    >
                      <Download className="w-4 h-4" />
                    </a>
                    {file.uploadedBy === user.id && (
                      <button 
                        onClick={() => handleDelete(file.id)}
                        className="p-1.5 text-slate-500 hover:text-red-600 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-md transition-colors"
                        title="Delete"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
