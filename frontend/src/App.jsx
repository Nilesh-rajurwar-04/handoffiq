import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/common/ProtectedRoute';
import MainLayout from './components/layout/MainLayout';

// Pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import MeetingListPage from './pages/meetings/MeetingListPage';
import MeetingDetailPage from './pages/meetings/MeetingDetailPage';
import AiMeetingIntelligencePage from './pages/meetings/AiMeetingIntelligencePage';
import TaskListPage from './pages/tasks/TaskListPage';
import TaskDetailPage from './pages/tasks/TaskDetailPage';
import HandoverListPage from './pages/handovers/HandoverListPage';
import CreateHandoverPage from './pages/handovers/CreateHandoverPage';
import HandoverDetailPage from './pages/handovers/HandoverDetailPage';
import CatchUpBriefPage from './pages/catchup/CatchUpBriefPage';
import KnowledgeBasePage from './pages/knowledge/KnowledgeBasePage';
import DocumentDetailPage from './pages/knowledge/DocumentDetailPage';
import HandoffAssistantPage from './pages/assistant/HandoffAssistantPage';
import NotificationsPage from './pages/notifications/NotificationsPage';
import AnalyticsPage from './pages/analytics/AnalyticsPage';
import EmployeeWorkloadPage from './pages/employees/EmployeeWorkloadPage';
import ProjectContinuityPage from './pages/projects/ProjectContinuityPage';
import ProfileSettingsPage from './pages/settings/ProfileSettingsPage';

export default function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected Routes inside Main Layout */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />

        {/* Meetings & AI Intelligence */}
        <Route path="meetings" element={<MeetingListPage />} />
        <Route path="meetings/ai-intelligence" element={<AiMeetingIntelligencePage />} />
        <Route path="meetings/:id" element={<MeetingDetailPage />} />

        {/* Task Management */}
        <Route path="tasks" element={<TaskListPage />} />
        <Route path="tasks/:id" element={<TaskDetailPage />} />

        {/* Smart Employee Handover */}
        <Route path="handovers" element={<HandoverListPage />} />
        <Route path="handovers/create" element={<CreateHandoverPage />} />
        <Route path="handovers/:id" element={<HandoverDetailPage />} />

        {/* Catch-Up Brief */}
        <Route path="catchup" element={<CatchUpBriefPage />} />

        {/* Knowledge Base */}
        <Route path="knowledge" element={<KnowledgeBasePage />} />
        <Route path="knowledge/:id" element={<DocumentDetailPage />} />

        {/* Context-Aware AI Assistant */}
        <Route path="assistant" element={<HandoffAssistantPage />} />

        {/* Notifications & Follow-Up */}
        <Route path="notifications" element={<NotificationsPage />} />

        {/* Analytics & Continuity */}
        <Route path="analytics" element={<AnalyticsPage />} />
        <Route path="employees" element={<EmployeeWorkloadPage />} />
        <Route path="projects/continuity" element={<ProjectContinuityPage />} />

        {/* Profile & Settings */}
        <Route path="settings" element={<ProfileSettingsPage />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
