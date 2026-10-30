import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { handoverApi, projectApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import EmptyState from '../../components/common/EmptyState';
import HandoverStatusBadge from '../../components/handover/HandoverStatusBadge';
import {
  ArrowRightLeft,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  ShieldCheck,
  UserCheck,
  Calendar
} from 'lucide-react';

export default function HandoverListPage() {
  const navigate = useNavigate();

  const [handovers, setHandovers] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [statusFilter, setStatusFilter] = useState('');
  const [projectFilter, setProjectFilter] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    fetchHandovers();
  }, [statusFilter, projectFilter]);

  const fetchHandovers = async () => {
    setLoading(true);
    try {
      const [handoversRes, projectsRes] = await Promise.allSettled([
        handoverApi.getHandovers({
          status: statusFilter || undefined,
          projectId: projectFilter || undefined
        }),
        projectApi.getProjects()
      ]);
      if (handoversRes.status === 'fulfilled') {
        setHandovers(handoversRes.value.data || []);
      } else {
        console.error('Failed to fetch handovers:', handoversRes.reason);
        setError('Failed to fetch handovers.');
      }
      if (projectsRes.status === 'fulfilled') {
        setProjects(projectsRes.value.data || []);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch handovers.');
    } finally {
      setLoading(false);
    }
  };

  const filteredHandovers = handovers.filter((h) => {
    if (!search) return true;
    const term = search.toLowerCase();
    return (
      h.title?.toLowerCase().includes(term) ||
      h.departingEmployeeName?.toLowerCase().includes(term) ||
      h.receivingEmployeeName?.toLowerCase().includes(term) ||
      h.projectName?.toLowerCase().includes(term)
    );
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
            <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Special Innovation Feature
            </span>
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
            Smart Employee Handovers
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
            Zero-loss team continuity powered by AI synthesis and the 7-factor Handover Readiness Score.
          </p>
        </div>
        <Button
          variant="primary"
          icon={Plus}
          onClick={() => navigate('/handovers/create')}
        >
          Initiate New Handover
        </Button>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Filter Bar */}
      <Card>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ flex: 1, minWidth: '220px', position: 'relative' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Search by employee, project, or title..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{
                width: '100%',
                padding: '8px 12px 8px 34px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                fontSize: '13px'
              }}
            />
          </div>

          <div style={{ minWidth: '170px' }}>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">All Statuses</option>
              <option value="DRAFT">Draft</option>
              <option value="AWAITING_REVIEW">Awaiting Review</option>
              <option value="AWAITING_ACCEPTANCE">Awaiting Acceptance</option>
              <option value="ACCEPTED">Accepted</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              value={projectFilter}
              onChange={(e) => setProjectFilter(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">All Projects</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      {/* Handover Cards Grid */}
      {loading ? (
        <LoadingSpinner text="Loading handover packages..." />
      ) : filteredHandovers.length === 0 ? (
        <EmptyState
          title="No handovers found"
          description="Initiate a handover when an employee changes projects, goes on leave, or transfers tasks."
          icon={ArrowRightLeft}
          actionLabel="Initiate Handover"
          onAction={() => navigate('/handovers/create')}
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(370px, 1fr))', gap: '18px' }}>
          {filteredHandovers.map((h) => {
            const score = h.readinessScore ?? 0;
            const scoreColor = score >= 80 ? '#10b981' : score >= 60 ? '#f59e0b' : '#ef4444';
            const scoreBg = score >= 80 ? '#ecfdf5' : score >= 60 ? '#fffbeb' : '#fef2f2';

            return (
              <div
                key={h.id}
                onClick={() => navigate(`/handovers/${h.id}`)}
                style={{
                  background: 'var(--surface-color)',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-lg)',
                  padding: '20px',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  gap: '14px',
                  boxShadow: 'var(--shadow-sm)',
                  transition: 'transform 0.15s ease, box-shadow 0.15s ease'
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                    <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                      {h.projectName || 'Project Handover'}
                    </span>
                    <HandoverStatusBadge status={h.status} />
                  </div>

                  <h3 style={{ fontSize: '16px', fontWeight: 800, color: 'var(--text-primary)', margin: '0 0 8px' }}>
                    {h.title || 'Knowledge Transfer'}
                  </h3>

                  {/* Transfer Route */}
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    padding: '10px 12px',
                    borderRadius: '8px',
                    background: 'var(--bg-secondary)',
                    margin: '8px 0 12px'
                  }}>
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <span style={{ display: 'block', fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>From</span>
                      <strong style={{ fontSize: '12.5px', color: 'var(--text-primary)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', display: 'block' }}>
                        {h.departingEmployeeName || 'Departing Employee'}
                      </strong>
                    </div>
                    <ArrowRight size={16} color="var(--primary-color)" />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <span style={{ display: 'block', fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>To</span>
                      <strong style={{ fontSize: '12.5px', color: 'var(--text-primary)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', display: 'block' }}>
                        {h.receivingEmployeeName || 'Receiving Employee'}
                      </strong>
                    </div>
                  </div>

                  <p style={{ fontSize: '12.5px', color: 'var(--text-secondary)', margin: 0, fontStyle: 'italic' }}>
                    <strong>Reason:</strong> {h.handoverReason || 'Role Transition'}
                  </p>
                </div>

                {/* Footer: Readiness Score & Link */}
                <div style={{
                  borderTop: '1px solid var(--border-color)',
                  paddingTop: '12px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <div style={{
                      padding: '4px 10px',
                      borderRadius: '999px',
                      background: scoreBg,
                      color: scoreColor,
                      fontSize: '12px',
                      fontWeight: 800,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}>
                      <ShieldCheck size={14} />
                      <span>{score}% Readiness</span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', fontWeight: 700, color: 'var(--primary-color)' }}>
                    <span>Inspect Package</span>
                    <ArrowRight size={14} />
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
