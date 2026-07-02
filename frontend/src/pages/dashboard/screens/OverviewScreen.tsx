import { observer } from 'mobx-react-lite';
import { BadgePercent, CalendarDays, Home, RussianRuble, TrendingDown, Wrench } from 'lucide-react';
import { store } from '../../../stores/appStore';
import { Metric } from '../../../components/Metric';
import type { EditorState, Section } from '../../../shared/types';
import { money } from '../../../shared/constants';
import { ContractsPanel, DocumentsPanel, ExpensesPanel, MaintenancePanel, ObjectsPanel, Onboarding, PaymentsPanel, PipelinePanel, QuickForms, TenantsPanel } from './DashboardPanels';

export const OverviewScreen = observer(({ onNavigate, onEdit }: { onNavigate: (section: Section) => void; onEdit: (editor: EditorState) => void }) => {
  const d = store.dashboard;
  return <>
    <div className="metrics" data-testid="metrics">
      <Metric icon={<Home />} label="Объекты" value={`${d?.objectsOccupied ?? 0}/${d?.objectsTotal ?? 0}`} />
      <Metric icon={<RussianRuble />} label="Оплачено за месяц" value={money(d?.paidThisMonth)} />
      <Metric icon={<TrendingDown />} label="Расходы за месяц" value={money(d?.expensesThisMonth)} />
      <Metric icon={<BadgePercent />} label="Чистый доход" value={money(d?.netThisMonth)} />
      <Metric icon={<CalendarDays />} label="Просрочка" value={money(d?.overdueAmount)} />
      <Metric icon={<Wrench />} label="Заявки в работе" value={String(d?.openMaintenance.length ?? 0)} />
    </div>
    <Onboarding onNavigate={onNavigate} />
    <QuickForms section="overview" />
    <section className="columns" data-testid="section-overview">
      <PaymentsPanel compact onEdit={onEdit} />
      <ObjectsPanel onEdit={onEdit} />
      <TenantsPanel onEdit={onEdit} />
      <ContractsPanel onEdit={onEdit} />
      <MaintenancePanel compact onEdit={onEdit} />
      <DocumentsPanel compact onEdit={onEdit} />
      <ExpensesPanel onEdit={onEdit} />
      <PipelinePanel compact onEdit={onEdit} />
    </section>
  </>;
});
