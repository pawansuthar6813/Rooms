import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

const colors = [
  'bg-red-500',
  'bg-orange-500',
  'bg-amber-500',
  'bg-green-500',
  'bg-emerald-500',
  'bg-teal-500',
  'bg-cyan-500',
  'bg-blue-500',
  'bg-indigo-500',
  'bg-violet-500',
  'bg-purple-500',
  'bg-fuchsia-500',
  'bg-pink-500',
  'bg-rose-500',
];

export function Avatar({ name, className }) {
  const getInitials = (name) => {
    if (!name) return '?';
    return name.substring(0, 2).toUpperCase();
  };

  const getColorIndex = (name) => {
    if (!name) return 0;
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return Math.abs(hash) % colors.length;
  };

  const colorClass = colors[getColorIndex(name)];

  return (
    <div
      className={twMerge(
        clsx(
          'flex items-center justify-center rounded-full text-white font-semibold flex-shrink-0',
          colorClass,
          className
        )
      )}
      style={{
        width: '40px',
        height: '40px',
        fontSize: '16px'
      }}
    >
      {getInitials(name)}
    </div>
  );
}
