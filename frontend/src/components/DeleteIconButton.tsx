import { Trash2 } from 'lucide-react';

export function DeleteIconButton({ label, onConfirm, disabled }: { label: string; onConfirm: () => void; disabled?: boolean }) {
  return <button className='danger icon-button' type='button' title={label} aria-label={label} onClick={onConfirm} disabled={disabled}><Trash2 size={17} /></button>;
}
