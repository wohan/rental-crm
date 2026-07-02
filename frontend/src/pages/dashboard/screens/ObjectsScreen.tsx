import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { ContractsPanel, ObjectsPanel, QuickForms, TenantsPanel } from './DashboardPanels';

export const ObjectsScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="objects" />
  <section className="columns single-column" data-testid="section-objects">
    <ObjectsPanel onEdit={onEdit} />
    <TenantsPanel onEdit={onEdit} />
    <ContractsPanel onEdit={onEdit} />
  </section>
</>);
