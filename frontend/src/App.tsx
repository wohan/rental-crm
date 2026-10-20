import {AgentChat} from '@text-agent/react';
import '@text-agent/react/style.css';
import {api} from './api/client';
import {observer} from 'mobx-react-lite';
import {store} from './stores/appStore';
import {AuthPage} from './pages/auth/AuthPage';
import {DashboardPage} from './pages/dashboard/DashboardPage';
import {PublicMaintenancePage} from './pages/public-maintenance/PublicMaintenancePage';

export const App = observer(() => (
  <div data-testid="app-ready">
    {window.location.pathname === '/maintenance-request' ? (
      <PublicMaintenancePage />
    ) : store.authenticated ? (
      <>
        <DashboardPage />
        {import.meta.env.VITE_TEXT_AGENT_ENABLED === 'true' && store.user && (
          <AgentChat
            key={store.user.email}
            storageKey={`rental-agent:${import.meta.env.VITE_API_URL ?? '/api'}:${store.user.email}`}
            baseUrl={`${(import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '')}/text-agent`}
            fieldLabels={{
              fullName: 'ФИО',
              phone: 'Телефон',
              email: 'Email',
              notes: 'Примечание',
              id: 'ID',
              objectId: 'ID объекта',
              tenantId: 'ID арендатора',
              contractId: 'ID договора',
              title: 'Название',
              address: 'Адрес',
              amount: 'Сумма',
              rentAmount: 'Арендная плата',
              startDate: 'Начало',
              endDate: 'Окончание',
              paymentDay: 'День оплаты',
              dueDate: 'Срок',
              status: 'Статус',
              items: 'Записи',
              total: 'Всего записей',
              truncated: 'Показаны первые 50',
              number: 'Номер',
              paidAmount: 'Оплачено',
              paidDate: 'Дата оплаты',
              monthlyRent: 'Арендная плата',
              type: 'Тип',
              name: 'Название',
              fileUrl: 'Ссылка',
            }}
            getHeaders={() => ({Authorization: `Bearer ${api.token}`})}
            onExecuted={() => {
              void store.loadAll().catch(() => {});
            }}
          />
        )}
      </>
    ) : (
      <AuthPage />
    )}
  </div>
));
