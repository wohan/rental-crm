import React, { useEffect } from 'react';

export function AutoTextarea({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  const ref = React.useRef<HTMLTextAreaElement | null>(null);
  useEffect(() => {
    const textarea = ref.current;
    if (!textarea) return;
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
  }, [value]);
  return <textarea ref={ref} className='auto-textarea' aria-label={label} rows={2} value={value} onChange={event => onChange(event.target.value)} />;
}
