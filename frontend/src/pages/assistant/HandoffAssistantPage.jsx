import React, { useState, useRef, useEffect } from 'react';
import { aiApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  Sparkles,
  Send,
  Bot,
  User,
  ShieldCheck,
  HelpCircle,
  FileText,
  Clock,
  ArrowRight
} from 'lucide-react';

const QUICK_QUESTIONS = [
  'What happened in the last project meeting?',
  'What tasks are pending for me?',
  'Which decisions were made about authentication?',
  'What should I work on next?',
  'Who is responsible for the database module?',
  'Show me unresolved issues and blockers.',
  'Summarize the latest employee handover.'
];

export default function HandoffAssistantPage() {
  const { user } = useAuth();
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'assistant',
      text: `Hello ${user?.fullName || 'there'}! I am your Handoff Assistant. I have direct access to your meetings, decisions, active tasks, knowledge repository, and employee handover records. How can I help you today?`,
      sources: []
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSendMessage = async (textToSend) => {
    const query = textToSend || input;
    if (!query.trim() || loading) return;

    const userMsg = {
      id: Date.now(),
      sender: 'user',
      text: query
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await aiApi.askAssistant({
        question: query,
        conversationHistory: messages.slice(-4).map((m) => ({
          role: m.sender === 'user' ? 'user' : 'assistant',
          content: m.text
        }))
      });

      const assistantMsg = {
        id: Date.now() + 1,
        sender: 'assistant',
        text: res.data?.answer || res.data?.content || 'I processed your query based on current database state.',
        sources: res.data?.sources || []
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } catch (err) {
      console.error(err);
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now() + 1,
          sender: 'assistant',
          text: 'I apologize, but I encountered an error searching our records. Please try again or rephrase your question.',
          sources: []
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', height: 'calc(100vh - 120px)', maxWidth: '1000px', margin: '0 auto', width: '100%' }}>
      {/* Header */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
          <Sparkles size={20} color="var(--primary-color)" />
          <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Context-Aware AI Assistant
          </span>
        </div>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          Handoff Assistant
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '2px 0 0' }}>
          Grounded queries answered strictly from verified database records and knowledge documents.
        </p>
      </div>

      {/* Quick Prompts Bar */}
      <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '6px' }}>
        {QUICK_QUESTIONS.map((q, idx) => (
          <button
            key={idx}
            onClick={() => handleSendMessage(q)}
            style={{
              padding: '6px 12px',
              borderRadius: '999px',
              border: '1px solid var(--border-color)',
              background: 'var(--surface-color)',
              fontSize: '12px',
              color: 'var(--text-primary)',
              whiteSpace: 'nowrap',
              cursor: 'pointer',
              transition: 'all 0.15s ease'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.borderColor = 'var(--primary-color)';
              e.currentTarget.style.color = 'var(--primary-color)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.borderColor = 'var(--border-color)';
              e.currentTarget.style.color = 'var(--text-primary)';
            }}
          >
            {q}
          </button>
        ))}
      </div>

      {/* Chat Messages Container */}
      <div style={{
        flex: 1,
        background: 'var(--surface-color)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '20px',
        overflowY: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
        boxShadow: 'var(--shadow-sm)'
      }}>
        {messages.map((m) => {
          const isUser = m.sender === 'user';
          return (
            <div
              key={m.id}
              style={{
                display: 'flex',
                gap: '12px',
                alignSelf: isUser ? 'flex-end' : 'flex-start',
                maxWidth: '82%',
                flexDirection: isUser ? 'row-reverse' : 'row'
              }}
            >
              {/* Avatar */}
              <div style={{
                width: '34px',
                height: '34px',
                borderRadius: '50%',
                background: isUser ? '#4f46e5' : 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                color: 'white',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0
              }}>
                {isUser ? <User size={18} /> : <Bot size={18} />}
              </div>

              {/* Message Bubble */}
              <div style={{
                background: isUser ? '#4f46e5' : 'var(--bg-secondary)',
                color: isUser ? '#ffffff' : 'var(--text-primary)',
                padding: '14px 18px',
                borderRadius: isUser ? '16px 16px 2px 16px' : '16px 16px 16px 2px',
                fontSize: '13.5px',
                lineHeight: 1.6,
                boxShadow: '0 1px 3px rgba(0,0,0,0.06)'
              }}>
                <div style={{ whiteSpace: 'pre-wrap' }}>{m.text}</div>

                {/* Grounded Sources */}
                {m.sources && m.sources.length > 0 && (
                  <div style={{
                    marginTop: '10px',
                    paddingTop: '8px',
                    borderTop: '1px solid rgba(0,0,0,0.08)',
                    fontSize: '11px',
                    color: isUser ? 'rgba(255,255,255,0.85)' : 'var(--text-muted)'
                  }}>
                    <strong style={{ display: 'block', marginBottom: '4px' }}>Grounding Sources:</strong>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      {m.sources.map((s, idx) => (
                        <span
                          key={idx}
                          style={{
                            padding: '2px 8px',
                            borderRadius: '4px',
                            background: isUser ? 'rgba(255,255,255,0.2)' : 'var(--surface-color)',
                            border: '1px solid rgba(0,0,0,0.06)'
                          }}
                        >
                          📌 {typeof s === 'string' ? s : s.title}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {loading && (
          <div style={{ display: 'flex', gap: '12px', alignSelf: 'flex-start', alignItems: 'center' }}>
            <div style={{ width: '34px', height: '34px', borderRadius: '50%', background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bot size={18} />
            </div>
            <div style={{ background: 'var(--bg-secondary)', padding: '12px 16px', borderRadius: '16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
              Querying database records and analyzing context...
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Form */}
      <form onSubmit={(e) => { e.preventDefault(); handleSendMessage(); }} style={{ display: 'flex', gap: '10px' }}>
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask anything about meetings, deliverables, decisions, or handovers..."
          style={{
            flex: 1,
            padding: '12px 16px',
            borderRadius: 'var(--radius-md)',
            border: '1px solid var(--border-color)',
            fontSize: '14px',
            background: 'var(--surface-color)'
          }}
        />
        <Button
          type="submit"
          variant="primary"
          icon={Send}
          loading={loading}
          size="lg"
        >
          Send
        </Button>
      </form>
    </div>
  );
}
