import { AgentChat } from '@text-agent/react';
import '@text-agent/react/style.css';
import { api } from './api/client';
import { observer } from 'mobx-react-lite';
import { store } from './stores/appStore';
import { AuthPage } from './pages/auth/AuthPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { PublicMaintenancePage } from './pages/public-maintenance/PublicMaintenancePage';

export const App = observer(() => (
  <div data-testid="app-ready">
    {window.location.pathname === '/maintenance-request' ? <PublicMaintenancePage /> :
      store.authenticated ? <>
        <DashboardPage />
        {import.meta.env.VITE_TEXT_AGENT_ENABLED === 'true' && <AgentChat
          key={store.token}
          baseUrl={`${(import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '')}/text-agent`}
          fieldLabels={{ fullName: 'ФИО', phone: 'Телефон', email: 'Email', notes: 'Примечание' }}
          getHeaders={() => ({ Authorization: `Bearer ${api.token}` })}
          onExecuted={() => { void store.loadAll().catch(() => {}); }}
        />}
      </> : <AuthPage />}
  </div>
));
