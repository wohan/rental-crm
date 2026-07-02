import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { DocumentsPanel, QuickForms } from './DashboardPanels';

export const DocumentsScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="documents" />
  <section className="columns single-column" data-testid="section-documents">
    <DocumentsPanel onEdit={onEdit} />
  </section>
</>);
