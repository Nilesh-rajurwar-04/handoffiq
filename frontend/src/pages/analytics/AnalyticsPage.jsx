import React, { useEffect, useState } from 'react';
import { dashboardApi } from '../../services/api';
import Card from '../../components/common/Card';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  CartesianGrid
} from 'recharts';
import { TrendingUp, ShieldCheck, CheckCircle2, Clock } from 'lucide-react';

const COLORS = ['#4f46e5', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

export default function AnalyticsPage() {
  const [loading, setLoading] = useState(true);
  const [metrics, setMetrics] = useState(null);

  useEffect(() => {
    fetchMetrics();
  }, []);

  const fetchMetrics = async () => {
    try {
      const res = await dashboardApi.getMetrics();
      setMetrics(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const readinessDistributionData = [
    { range: '0-49% (Critical)', count: 1, color: '#ef4444' },
    { range: '50-74% (In Progress)', count: 1, color: '#f59e0b' },
    { range: '75-100% (Ready)', count: 4, color: '#10b981' }
  ];

  const velocityData = [
    { sprint: 'Sprint 20', tasksCreated: 14, tasksCompleted: 12, handovers: 2 },
    { sprint: 'Sprint 21', tasksCreated: 18, tasksCompleted: 16, handovers: 1 },
    { sprint: 'Sprint 22', tasksCreated: 22, tasksCompleted: 19, handovers: 3 },
    { sprint: 'Sprint 23', tasksCreated: 20, tasksCompleted: 18, handovers: 2 },
    { sprint: 'Sprint 24 (Current)', tasksCreated: 15, tasksCompleted: 14, handovers: 2 }
  ];

  if (loading) return <LoadingSpinner text="Aggregating team analytics..." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      <div>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          Team Continuity & Task Analytics
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
          Real-time metrics on task completion velocity, handover readiness distribution, and carry-forward rates.
        </p>
      </div>

      {/* Grid: Charts */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '20px' }}>
        {/* Handover Readiness Distribution */}
        <Card
          title="🛡️ Handover Readiness Distribution"
          subtitle="Proportion of handovers satisfying the 7-criteria threshold"
        >
          <div style={{ height: '260px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={readinessDistributionData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="range" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count" name="Handovers" radius={[4, 4, 0, 0]}>
                  {readinessDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Sprint Velocity & Handover Volume */}
        <Card
          title="📈 Sprint Deliverable Velocity"
          subtitle="Tasks completed vs created across recent iterations"
        >
          <div style={{ height: '260px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={velocityData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="sprint" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="tasksCompleted" stroke="#10b981" strokeWidth={2} name="Tasks Completed" />
                <Line type="monotone" dataKey="tasksCreated" stroke="#4f46e5" strokeWidth={2} name="Tasks Created" />
                <Line type="monotone" dataKey="handovers" stroke="#f59e0b" strokeWidth={2} name="Handovers" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>
    </div>
  );
}
