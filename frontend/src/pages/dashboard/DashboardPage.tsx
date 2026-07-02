import { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { BadgePercent, Bell, Building2, CheckCircle2, Download, FileText, History, Home, LogOut, RefreshCw, RussianRuble, TrendingDown, Users, Wrench } from 'lucide-react';
import { store } from '../../stores/appStore';
import type { EditorState, Section } from '../../shared/types';
import { DashboardScreens, EditDrawer } from './screens/DashboardScreens';

export const DashboardPage = observer(() => {
  const [section, setSection] = useState<Section>('overview');
  const [editor, setEditor] = useState<EditorState>(null);
  useEffect(() => { store.loadAll(); }, []);
  const navItems: Array<[Section, string, React.ReactNode]> = [
    ['overview', 'Сводка', <Home size={18} />],
    ['objects', 'Объекты', <Building2 size={18} />],
    ['payments', 'Платежи', <RussianRuble size={18} />],
    ['maintenance', 'Заявки', <Wrench size={18} />],
    ['documents', 'Документы', <FileText size={18} />],
    ['finance', 'Финансы', <TrendingDown size={18} />],
    ['pipeline', 'Лиды', <Users size={18} />],
    ['notifications', 'Уведомления', <Bell size={18} />],
    ['billing', 'Подписка', <BadgePercent size={18} />],
    ['audit', 'Аудит', <History size={18} />]
  ];

  return <main className="app-shell">
    <aside>
      <span className="brand">Rental Management</span>
      <nav>
        {navItems.map(([id, label, icon]) => <button
          key={id}
          className={section === id ? 'active' : ''}
          onClick={() => setSection(id)}
          data-testid={`nav-${id}`}
        >
          {icon}<span>{label}</span>
        </button>)}
      </nav>
      <button className="ghost" onClick={() => store.logout()}><LogOut size={18} /> Выйти</button>
    </aside>
    <section className="workspace">
      <header>
        <div>
          <h1>{store.user?.account.name ?? 'Кабинет'}</h1>
          <p>Тариф {store.user?.account.tariff ?? 'FREE'} · лимит {store.user?.account.objectsLimit ?? 3} объектов</p>
        </div>
        <div className="actions">
          <button className="ghost" onClick={() => store.exportWorkbook()} disabled={Boolean(store.actionInFlight)} data-testid="export-button">
            <Download size={18} /> Экспорт
          </button>
          <button className="ghost" onClick={() => store.refresh()} disabled={Boolean(store.actionInFlight)} data-testid="refresh-button">
            <RefreshCw size={18} /> {store.actionInFlight === 'Обновляем' ? 'Обновляем...' : 'Обновить'}
          </button>
        </div>
      </header>
      {store.error && <p className="error">{store.error}</p>}
      {store.notice && <p className="notice" data-testid="notice"><CheckCircle2 size={18} /> {store.notice}</p>}
      {store.user && !store.user.emailVerified && <p className="warning" data-testid="email-warning">
        Email не подтвержден.
        {store.verificationLink
          ? <> <a href={store.verificationLink} data-testid="verify-email-link">Подтвердить email</a></>
          : ' Проверьте почту со ссылкой подтверждения.'}
      </p>}
      <DashboardScreens section={section} onNavigate={setSection} onEdit={setEditor} />
      <EditDrawer editor={editor} onClose={() => setEditor(null)} />
    </section>
  </main>;
});
