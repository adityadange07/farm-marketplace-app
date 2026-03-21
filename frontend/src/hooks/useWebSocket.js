import { useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useAuthStore } from '../store/authStore';
import toast from 'react-hot-toast';

export function useWebSocket() {
  const clientRef = useRef(null);
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);

  useEffect(() => {
    if (!accessToken || !user) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      onConnect: () => {
        console.log('WebSocket connected');

        // Subscribe to personal notifications
        client.subscribe(
          `/user/${user.userId}/queue/notifications`,
          (message) => {
            const notification = JSON.parse(message.body);
            toast(notification.message, {
              icon: notification.type === 'NEW_ORDER' ? '📦' : '🔔',
              duration: 5000,
            });
          }
        );
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
      },
      reconnectDelay: 5000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [accessToken, user]);

  const sendMessage = useCallback((destination, body) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({
        destination,
        body: JSON.stringify(body),
      });
    }
  }, []);

  return { sendMessage };
}