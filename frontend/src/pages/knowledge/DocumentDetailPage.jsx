import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { documentApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  ArrowLeft,
  FileText,
  Calendar,
  User,
  ShieldCheck,
  FolderGit2,
  Database
} from 'lucide-react';

export default function DocumentDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [doc, setDoc] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDoc();
  }, [id]);

  const fetchDoc = async () => {
    setLoading(true);
    try {
      const res = await documentApi.getDocument(id);
      setDoc(res.data);
    } catch (err) {
      console.error(err);
      setError('Could not load knowledge document.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading document..." />;
  if (!doc) return <AlertBanner type="error" message="Document not found." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Button
          variant="outline"
          size="sm"
          icon={ArrowLeft}
          onClick={() => navigate('/knowledge')}
        >
          Back to Knowledge Base
        </Button>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Header */}
      <div style={{
        background: 'var(--surface-color)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '24px',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '12px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
              <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                {doc.category || 'General'}
              </span>
              <Badge variant={doc.accessLevel === 'CONFIDENTIAL' ? 'danger' : 'info'}>
                {doc.accessLevel || 'INTERNAL'}
              </Badge>
            </div>
            <h1 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
              {doc.title}
            </h1>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', borderTop: '1px solid var(--border-color)', paddingTop: '16px', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>Project: </span>
            <strong>{doc.projectName || 'General Knowledge'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>Uploaded By: </span>
            <strong>{doc.uploadedByName || 'System'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>Format: </span>
            <strong>{doc.fileType || 'MARKDOWN'}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)' }}>Size: </span>
            <strong>{doc.fileSizeBytes ? `${Math.round(doc.fileSizeBytes / 1024)} KB` : '4 KB'}</strong>
          </div>
        </div>
      </div>

      {/* Full Document Content */}
      <Card title="📖 Document Content & Runbook">
        <div style={{
          background: '#f8fafc',
          border: '1px solid var(--border-color)',
          borderRadius: '8px',
          padding: '20px',
          fontSize: '13.5px',
          lineHeight: 1.7,
          color: 'var(--text-primary)',
          whiteSpace: 'pre-wrap',
          fontFamily: 'monospace'
        }}>
          {doc.content || doc.summary || 'No text content available.'}
        </div>
      </Card>
    </div>
  );
}
