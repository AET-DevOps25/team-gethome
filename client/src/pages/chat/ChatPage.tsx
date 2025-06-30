import React, { useState, useRef, useEffect } from 'react';
import { UserCircleIcon, CpuChipIcon } from '@heroicons/react/24/solid';
import { Bot } from 'lucide-react';
import BottomTabBar from '../../components/BottomTabBar';

interface Message {
  id: number;
  text: string;
  sender: 'me' | 'other';
}

const mockedMessages: Message[] = [
  { id: 1, text: "Hey! How are you?", sender: 'other' },
  { id: 2, text: "I'm good, thanks! How about you?", sender: 'me' },
  { id: 3, text: "Doing well! Ready for the trip?", sender: 'other' },
  { id: 4, text: "Absolutely, can't wait!", sender: 'me' },
];

const ChatPage: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>(mockedMessages);
  const [input, setInput] = useState('');
  const [activeTab, setActiveTab] = useState(2); // 2 for Chat
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = () => {
    if (input.trim() === '') return;
    setMessages([
      ...messages,
      { id: messages.length + 1, text: input, sender: 'me' }
    ]);
    setInput('');
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <div className="flex-1 flex flex-col justify-end pb-16"> {/* pb-16 for BottomTabBar */}
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
        <div className="flex items-center px-4 py-2 bg-white border-t">
          <input
            type="text"
            className="flex-1 rounded-full border px-4 py-2 mr-2 focus:outline-none"
            placeholder="Type a message..."
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleSend()}
          />
          <button
            className="bg-indigo-500 text-white rounded-full px-4 py-2 font-semibold"
            onClick={handleSend}
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