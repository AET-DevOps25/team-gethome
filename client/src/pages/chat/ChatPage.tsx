import React, { useEffect, useRef, useState } from 'react';
import { chatService } from '../../services/chatService';
import BottomTabBar from '../../components/BottomTabBar';
import { UserCircleIcon } from '@heroicons/react/24/solid';
import { Bot } from 'lucide-react';

interface Message {
  id: string;
  sender: 'me' | 'bot';
  text: string;
}

const ChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(2);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const start = async () => {
      try {
        const id = await chatService.startSession();
        setSessionId(id);
      } catch (err) {
        setMessages([{ id: 'error', sender: 'bot', text: 'Failed to start chat session.' }]);
      }
    };
    start();
    return () => {
      if (sessionId) chatService.closeSession(sessionId).catch(() => {});
    };
    // eslint-disable-next-line
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || !sessionId) return;
    const userMsg: Message = { id: Date.now() + '', sender: 'me', text: input };
    setMessages((msgs) => [...msgs, userMsg]);
    setInput('');
    setLoading(true);
    try {
      const res = await chatService.sendMessage(sessionId, userMsg.text);
      setMessages((msgs) => [
        ...msgs,
        {
          id: Date.now() + '-bot',
          sender: 'bot',
          text: res.reply,
        },
      ]);
    } catch (err) {
      setMessages((msgs) => [
        ...msgs,
        { id: Date.now() + '-err', sender: 'bot', text: 'Failed to get reply.' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <div className="flex-1 flex flex-col justify-end pb-16">
        <div className="flex-1 overflow-y-auto px-4 py-4">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex flex-col mb-2 ${msg.sender === 'me' ? 'items-end' : 'items-start'}`}
            >
              <div
                className={`rounded-lg px-4 py-2 max-w-xs break-words ${
                  msg.sender === 'me'
                    ? 'bg-indigo-500 text-white'
                    : 'bg-gray-200 text-gray-900'
                }`}
              >
                {msg.text}
              </div>
              <div className="mt-1">
                {msg.sender === 'me' ? (
                  <UserCircleIcon className="h-7 w-7 text-indigo-400" />
                ) : (
                  <Bot className="h-7 w-7 text-gray-400" />
                )}
              </div>
            </div>
          ))}
          <div ref={messagesEndRef} />
        </div>
        <div className="flex px-4 py-2 bg-white border-t">
          <input
            className="flex-1 border rounded px-3 py-2 mr-2"
            type="text"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSend()}
            placeholder="Type your message..."
            disabled={loading || !sessionId}
          />
          <button
            className="bg-indigo-600 text-white px-4 py-2 rounded font-semibold"
            onClick={handleSend}
            disabled={loading || !sessionId}
          >
            Send
          </button>
        </div>
      </div>
      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default ChatPage;