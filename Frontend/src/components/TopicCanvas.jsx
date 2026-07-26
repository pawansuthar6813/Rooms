import React, { useEffect, useState, useRef, useCallback } from 'react';
import { Tldraw } from 'tldraw';
import 'tldraw/tldraw.css';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../lib/api';
import toast from 'react-hot-toast';

export default function TopicCanvas({ roomId, topic }) {
  const [editor, setEditor] = useState(null);
  const stompClientRef = useRef(null);
  const isSyncingRef = useRef(false);
  const pendingUpdatesRef = useRef([]);
  const debounceTimerRef = useRef(null);

  // Set up WebSocket connection
  useEffect(() => {
    // We connect to the gateway which handles the WebSocket upgrade
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        // console.log(str);
      },
      onConnect: () => {
        console.log('Connected to WebSocket for topic:', topic.id);
        
        stompClient.subscribe(`/topic/board/${topic.id}`, (message) => {
          if (!editor) return;
          
          try {
            const payload = JSON.parse(message.body);
            const { operation } = payload;
            
            if (operation) {
              const { type, elementId, data } = operation;
              
              isSyncingRef.current = true;
              editor.store.mergeRemoteChanges(() => {
                if (type === 'ADD' || type === 'UPDATE' || type === 'SET') {
                  editor.store.put([data]);
                } else if (type === 'REMOVE' || type === 'DELETE') {
                  editor.store.remove([elementId]);
                }
              });
              isSyncingRef.current = false;
            }
          } catch (e) {
            console.error('Failed to parse WebSocket message', e);
          }
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      },
    });

    stompClient.activate();
    stompClientRef.current = stompClient;

    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
    };
  }, [topic.id, editor]);

  // Handle Tldraw mount and initial state load
  const handleMount = useCallback((mountedEditor) => {
    setEditor(mountedEditor);
    
    // Load initial state from the topic object
    if (topic.canvasState && Object.keys(topic.canvasState).length > 0) {
      try {
        const records = Object.values(topic.canvasState);
        if (records.length > 0) {
          mountedEditor.store.mergeRemoteChanges(() => {
            mountedEditor.store.put(records);
          });
        }
      } catch (err) {
        console.error('Failed to load initial canvas state', err);
        toast.error('Failed to load canvas state');
      }
    }
  }, [topic]);

  // Listen to local Tldraw changes and sync them
  useEffect(() => {
    if (!editor) return;

    const handleChange = (update) => {
      // Ignore if changes were triggered by incoming websocket sync
      if (isSyncingRef.current) return;

      const { added, updated, removed } = update.changes;

      // Queue updates
      Object.values(added).forEach(record => {
        pendingUpdatesRef.current.push({ type: 'ADD', elementId: record.id, data: record });
      });

      Object.values(updated).forEach(([from, to]) => {
        pendingUpdatesRef.current.push({ type: 'UPDATE', elementId: to.id, data: to });
      });

      Object.values(removed).forEach(record => {
        pendingUpdatesRef.current.push({ type: 'REMOVE', elementId: record.id, data: null });
      });

      // Send batched updates with 50ms debounce
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

      debounceTimerRef.current = setTimeout(() => {
        if (!stompClientRef.current || !stompClientRef.current.connected) {
          pendingUpdatesRef.current = [];
          return;
        }

        const updatesToSend = [...pendingUpdatesRef.current];
        pendingUpdatesRef.current = [];

        updatesToSend.forEach(operation => {
          stompClientRef.current.publish({
            destination: `/app/board/${topic.id}/update`,
            body: JSON.stringify({
              topicId: topic.id,
              version: Date.now(),
              operation
            })
          });
        });
      }, 50);
    };

    const cleanup = editor.store.listen(handleChange, { source: 'user', scope: 'document' });
    return () => cleanup();
  }, [editor, topic.id]);

  return (
    <div className="w-full h-full relative tldraw-wrapper">
      <Tldraw
        onMount={handleMount}
        inferDarkMode={true}
      />
    </div>
  );
}
