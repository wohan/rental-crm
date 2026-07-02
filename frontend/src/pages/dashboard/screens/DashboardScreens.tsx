import { observer } from 'mobx-react-lite';
import type { EditorState, Section } from '../../../shared/types';
import { AuditPanel, BillingPanel, NotificationsPanel } from './DashboardPanels';
import { DocumentsScreen } from './DocumentsScreen';
import { FinanceScreen } from './FinanceScreen';
import { MaintenanceScreen } from './MaintenanceScreen';
import { ObjectsScreen } from './ObjectsScreen';
import { OverviewScreen } from './OverviewScreen';
import { PaymentsScreen } from './PaymentsScreen';
import { PipelineScreen } from './PipelineScreen';

export { EditDrawer } from './DashboardPanels';

type DashboardScreensProps = {
  section: Section;
  onNavigate: (section: Section) => void;
  onEdit: (editor: EditorState) => void;
};

export const DashboardScreens = observer(({ section, onNavigate, onEdit }: DashboardScreensProps) => {
  switch (section) {
    case 'overview': return <OverviewScreen onNavigate={onNavigate} onEdit={onEdit} />;
    case 'objects': return <ObjectsScreen onEdit={onEdit} />;
    case 'payments': return <PaymentsScreen onEdit={onEdit} />;
    case 'maintenance': return <MaintenanceScreen onEdit={onEdit} />;
    case 'documents': return <DocumentsScreen onEdit={onEdit} />;
    case 'finance': return <FinanceScreen onEdit={onEdit} />;
    case 'pipeline': return <PipelineScreen onEdit={onEdit} />;
    case 'notifications': return <section data-testid="section-notifications"><NotificationsPanel /></section>;
    case 'billing': return <section data-testid="section-billing"><BillingPanel /></section>;
    case 'audit': return <section data-testid="section-audit"><AuditPanel /></section>;
  }
});
