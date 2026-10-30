import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { documentApi, projectApi, aiApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import EmptyState from '../../components/common/EmptyState';
import {
  FileText,
  Upload,
  Search,
  Sparkles,
  BookOpen,
  Filter,
  Download,
  ExternalLink,
  ShieldCheck,
  FileCode,
  Folder
} from 'lucide-react';

const CATEGORIES = ['All', 'Architecture', 'API Documentation', 'Runbooks', 'Handover Reports', 'Meeting Notes'];

export default function KnowledgeBasePage() {
  const navigate = useNavigate();

  const [documents, setDocuments] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');

  // AI Semantic Search
  const [aiQuestion, setAiQuestion] = useState('');
  const [aiSearching, setAiSearching] = useState(false);
  const [aiAnswer, setAiAnswer] = useState(null);

  // Upload Modal
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [uploadFormData, setUploadFormData] = useState({
    title: '',
    category: 'Architecture',
    projectId: '',
    accessLevel: 'PUBLIC',
    description: '',
    content: ''
  });
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchDocuments();
  }, [selectedCategory]);

  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const params = {
        category: selectedCategory === 'All' ? undefined : selectedCategory
      };
      const [docsRes, projRes] = await Promise.all([
        documentApi.getDocuments(params),
        projectApi.getProjects()
      ]);
      setDocuments(docsRes.data || []);
      setProjects(projRes.data || []);
      if (projRes.data?.length > 0) {
        setUploadFormData((prev) => ({ ...prev, projectId: projRes.data[0].id }));
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch knowledge documents.');
    } finally {
      setLoading(false);
    }
  };

  const handleAiSearch = async (e) => {
    e?.preventDefault();
    if (!aiQuestion.trim()) return;
    setAiSearching(true);
    setAiAnswer(null);
    try {
      const res = await aiApi.semanticSearchKnowledge({ query: aiQuestion });
      setAiAnswer(res.data);
    } catch (err) {
      console.error(err);
      alert('AI knowledge search failed.');
    } finally {
      setAiSearching(false);
    }
  };

  const handleUploadDocument = async (e) => {
    e.preventDefault();
    setUploading(true);
    try {
      await documentApi.createDocument({
        ...uploadFormData,
        projectId: uploadFormData.projectId ? Number(uploadFormData.projectId) : null
      });
      setIsUploadModalOpen(false);
      setUploadFormData({
        title: '',
        category: 'Architecture',
        projectId: projects[0]?.id || '',
        accessLevel: 'PUBLIC',
        description: '',
        content: ''
      });
      fetchDocuments();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to upload document.');
    } finally {
      setUploading(false);
    }
  };

  const filteredDocs = documents.filter((d) => {
    if (!searchQuery) return true;
    const term = searchQuery.toLowerCase();
    return d.title?.toLowerCase().includes(term) || d.category?.toLowerCase().includes(term);
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
            Knowledge Repository & Architecture Base
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
            Central repository for architectural diagrams, API guides, handover records, and AI-grounded retrieval.
          </p>
        </div>
        <Button
          variant="primary"
          icon={Upload}
          onClick={() => setIsUploadModalOpen(true)}
        >
          Upload Document
        </Button>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* AI Semantic Knowledge Search Card */}
      <Card
        title="✨ AI Knowledge Search (RAG)"
        subtitle="Ask a natural language question; AI searches indexed documents and returns cited answers"
      >
        <form onSubmit={handleAiSearch} style={{ display: 'flex', gap: '8px' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              value={aiQuestion}
              onChange={(e) => setAiQuestion(e.target.value)}
              placeholder="e.g. What is our authentication token lifecycle? or How are AWS VPC subnets arranged?"
              style={{
                width: '100%',
                padding: '10px 12px 10px 36px',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                fontSize: '13.5px'
              }}
            />
          </div>
          <Button
            type="submit"
            variant="primary"
            icon={Sparkles}
            loading={aiSearching}
          >
            Ask AI
          </Button>
        </form>

        {/* AI Answer Box with Citations */}
        {aiAnswer && (
          <div style={{
            marginTop: '16px',
            background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(37, 99, 235, 0.05) 100%)',
            border: '1px solid rgba(79, 70, 229, 0.2)',
            borderRadius: 'var(--radius-md)',
            padding: '18px'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px', color: 'var(--primary-color)', fontWeight: 700, fontSize: '13px' }}>
              <Sparkles size={16} />
              <span>Grounded Knowledge Answer</span>
            </div>
            <p style={{ fontSize: '13.5px', lineHeight: 1.6, color: 'var(--text-primary)', margin: 0, whiteSpace: 'pre-wrap' }}>
              {aiAnswer.answer || aiAnswer.content || 'Relevant knowledge retrieved.'}
            </p>

            {aiAnswer.sources?.length > 0 && (
              <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid var(--border-color)' }}>
                <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  Cited Sources:
                </span>
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '6px' }}>
                  {aiAnswer.sources.map((s, idx) => (
                    <span
                      key={idx}
                      style={{
                        fontSize: '11.5px',
                        padding: '3px 8px',
                        borderRadius: '6px',
                        background: 'var(--surface-color)',
                        border: '1px solid var(--border-color)',
                        color: 'var(--text-secondary)'
                      }}
                    >
                      📄 {s.title || s}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Card>

      {/* Category Pills & Filter Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              style={{
                padding: '6px 14px',
                borderRadius: '999px',
                border: selectedCategory === cat ? '1px solid var(--primary-color)' : '1px solid var(--border-color)',
                background: selectedCategory === cat ? 'var(--primary-color)' : 'var(--surface-color)',
                color: selectedCategory === cat ? '#ffffff' : 'var(--text-secondary)',
                fontSize: '12.5px',
                fontWeight: 600,
                cursor: 'pointer',
                transition: 'all 0.15s ease'
              }}
            >
              {cat}
            </button>
          ))}
        </div>

        <div style={{ width: '220px' }}>
          <input
            type="text"
            placeholder="Search documents..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ width: '100%', padding: '7px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
          />
        </div>
      </div>

      {/* Document Library Grid */}
      {loading ? (
        <LoadingSpinner text="Loading knowledge documents..." />
      ) : filteredDocs.length === 0 ? (
        <EmptyState
          title="No documents in this category"
          description="Upload architectural notes, handover reports, or technical guides."
          icon={BookOpen}
          actionLabel="Upload Document"
          onAction={() => setIsUploadModalOpen(true)}
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {filteredDocs.map((doc) => (
            <div
              key={doc.id}
              onClick={() => navigate(`/knowledge/${doc.id}`)}
              style={{
                background: 'var(--surface-color)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                padding: '18px',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                gap: '12px',
                boxShadow: 'var(--shadow-sm)',
                transition: 'transform 0.15s ease, box-shadow 0.15s ease'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                    {doc.category || 'General'}
                  </span>
                  <Badge variant={doc.accessLevel === 'CONFIDENTIAL' ? 'danger' : 'info'}>
                    {doc.accessLevel || 'INTERNAL'}
                  </Badge>
                </div>
                <h4 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)', margin: '0 0 6px' }}>
                  {doc.title}
                </h4>
                <p style={{
                  fontSize: '12.5px',
                  color: 'var(--text-secondary)',
                  margin: 0,
                  display: '-webkit-box',
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}>
                  {doc.description || doc.summary || 'Click to view full contents, chunks, and citations.'}
                </p>
              </div>

              <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                <span>{doc.fileType || 'MARKDOWN'} • {doc.fileSizeBytes ? `${Math.round(doc.fileSizeBytes / 1024)} KB` : '4 KB'}</span>
                <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>Inspect →</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Upload Document Modal */}
      <Modal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        title="Upload Document to Knowledge Base"
        maxWidth="540px"
      >
        <form onSubmit={handleUploadDocument} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Document Title *
            </label>
            <input
              type="text"
              required
              value={uploadFormData.title}
              onChange={(e) => setUploadFormData({ ...uploadFormData, title: e.target.value })}
              placeholder="e.g. AWS Deployment Architecture & VPC Layout"
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Category *
              </label>
              <select
                value={uploadFormData.category}
                onChange={(e) => setUploadFormData({ ...uploadFormData, category: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              >
                {CATEGORIES.filter((c) => c !== 'All').map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Associated Project
              </label>
              <select
                value={uploadFormData.projectId}
                onChange={(e) => setUploadFormData({ ...uploadFormData, projectId: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              >
                <option value="">-- General Knowledge --</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Description / Executive Summary
            </label>
            <input
              type="text"
              value={uploadFormData.description}
              onChange={(e) => setUploadFormData({ ...uploadFormData, description: e.target.value })}
              placeholder="Brief summary of document topics..."
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Document Content (Markdown / Text) *
            </label>
            <textarea
              rows={6}
              required
              value={uploadFormData.content}
              onChange={(e) => setUploadFormData({ ...uploadFormData, content: e.target.value })}
              placeholder="Paste document markdown, runbook steps, or technical guidelines..."
              style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px', lineHeight: 1.5 }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '8px' }}>
            <Button variant="outline" type="button" onClick={() => setIsUploadModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" loading={uploading}>
              Index & Upload
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
