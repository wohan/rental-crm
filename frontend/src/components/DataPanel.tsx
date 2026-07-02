import React from 'react';

export function DataPanel({ title, icon, empty, children }: { title: string; icon: React.ReactNode; empty: string; children: React.ReactNode }) {
  const hasChildren = React.Children.count(children) > 0;
  return <div className='panel'><h2>{icon}{title}</h2>{hasChildren ? children : <p className='muted'>{empty}</p>}</div>;
}
