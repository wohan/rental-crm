export type Section = 'overview' | 'objects' | 'payments' | 'maintenance' | 'documents' | 'finance' | 'pipeline' | 'notifications' | 'billing' | 'audit';
export type Field = {
  key: string;
  label: string;
  value: string;
  type?: 'text' | 'number' | 'date' | 'checkbox' | 'select';
  options?: Array<[string, string]>;
};
export type EditorState = null | {
  type: 'object' | 'tenant' | 'contract' | 'payment' | 'maintenance' | 'document' | 'expense' | 'listing' | 'lead';
  title: string;
  path: string;
  item: Record<string, unknown>;
};
