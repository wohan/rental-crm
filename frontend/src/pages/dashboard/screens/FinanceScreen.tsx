import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { ExpensesPanel, QuickForms } from './DashboardPanels';

export const FinanceScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="finance" />
  <section className="columns" data-testid="section-finance">
    <ExpensesPanel onEdit={onEdit} />
  </section>
</>);
