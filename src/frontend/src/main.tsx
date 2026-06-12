import React, { useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import { observer } from 'mobx-react-lite';
import { Building2, Check, CreditCard, Home, RefreshCw, Wrench } from 'lucide-react';
import { rootStore } from './stores/RootStore';
import { MetricCard } from './components/MetricCard';
import './styles.css';

const currency = new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 });

const App = observer(() => {
  useEffect(() => {
    rootStore.load();
  }, []);

  const dashboard = rootStore.dashboard;

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p>Rental CRM</p>
          <h1>Кабинет арендодателя</h1>
        </div>
        <button className="icon-button" onClick={() => rootStore.load()} title="Обновить">
          <RefreshCw size={18} />
        </button>
      </header>

      {rootStore.error && <div className="alert">{rootStore.error}</div>}
      {rootStore.loading && !dashboard && <div className="loading">Загрузка данных...</div>}

      {dashboard && (
        <>
          <section className="metrics-grid">
            <MetricCard label="Объекты" value={dashboard.propertiesTotal} hint={`${dashboard.occupiedProperties} занято`} />
            <MetricCard label="Активные договоры" value={dashboard.activeLeases} hint="Продления в ближайшие 45 дней" />
            <MetricCard label="Просрочки" value={dashboard.overduePayments} hint="Платежи требуют внимания" />
            <MetricCard label="Заявки" value={dashboard.openMaintenanceRequests} hint="Открытые ремонты" />
            <MetricCard label="Получено" value={currency.format(dashboard.paidRevenue)} hint="Зафиксированные оплаты" />
            <MetricCard label="К получению" value={currency.format(dashboard.receivable)} hint="План и долги" />
          </section>

          <section className="workspace">
            <div className="panel wide">
              <div className="panel-title">
                <CreditCard size={18} />
                <h2>Платежи</h2>
              </div>
              <div className="table">
                {rootStore.payments.map((payment) => (
                  <div className="row" key={payment.id}>
                    <div>
                      <strong>{payment.lease.property.name}</strong>
                      <span>{payment.lease.tenant.fullName} · до {payment.dueDate}</span>
                    </div>
                    <b>{currency.format(payment.amount)}</b>
                    <span className={`badge ${payment.status.toLowerCase()}`}>{payment.status}</span>
                    {payment.status !== 'PAID' && (
                      <button className="small-button" onClick={() => rootStore.markPaymentPaid(payment.id)} title="Отметить оплату">
                        <Check size={15} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>

            <div className="panel">
              <div className="panel-title">
                <Wrench size={18} />
                <h2>Ремонт</h2>
              </div>
              {rootStore.maintenance.map((item) => (
                <article className="list-item" key={item.id}>
                  <strong>{item.title}</strong>
                  <span>{item.property.name}</span>
                  <small>{item.priority} · {item.status}</small>
                </article>
              ))}
            </div>

            <div className="panel">
              <div className="panel-title">
                <Home size={18} />
                <h2>Объекты</h2>
              </div>
              {rootStore.properties.map((property) => (
                <article className="list-item" key={property.id}>
                  <strong>{property.name}</strong>
                  <span>{property.city}, {currency.format(property.monthlyRent)}</span>
                  <small>{property.type} · {property.status}</small>
                </article>
              ))}
            </div>

            <div className="panel wide">
              <div className="panel-title">
                <Building2 size={18} />
                <h2>Арендаторы</h2>
              </div>
              <div className="tenant-grid">
                {rootStore.tenants.map((tenant) => (
                  <article className="tenant-card" key={tenant.id}>
                    <strong>{tenant.fullName}</strong>
                    <span>{tenant.phone}</span>
                    <small>{tenant.email || 'email не указан'} · {tenant.legalType}</small>
                  </article>
                ))}
              </div>
            </div>
          </section>
        </>
      )}
    </main>
  );
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
