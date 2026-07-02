import { observer } from 'mobx-react-lite';
import type { EditorState } from '../../../shared/types';
import { PipelinePanel, QuickForms } from './DashboardPanels';

export const PipelineScreen = observer(({ onEdit }: { onEdit: (editor: EditorState) => void }) => <>
  <QuickForms section="pipeline" />
  <section className="columns single-column" data-testid="section-pipeline">
    <PipelinePanel onEdit={onEdit} />
  </section>
</>);
