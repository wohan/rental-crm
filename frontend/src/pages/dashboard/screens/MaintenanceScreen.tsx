import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { MaintenancePanel, QuickForms } from './DashboardPanels';

export const MaintenanceScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="maintenance" />
  <section className="columns single-column" data-testid="section-maintenance">
    <MaintenancePanel onEdit={onEdit} />
  </section>
</>);
