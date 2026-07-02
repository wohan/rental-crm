import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { PaymentsPanel, QuickForms } from './DashboardPanels';

export const PaymentsScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="payments" />
  <section className="columns single-column" data-testid="section-payments">
    <PaymentsPanel onEdit={onEdit} />
  </section>
</>);
