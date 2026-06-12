// prf-ui.jsx — Shared UI primitives

// ── SVG Icons ──────────────────────────────────────────────────
const IC = {
  back:     <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>,
  close:    <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/></svg>,
  add:      <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>,
  more:     <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>,
  search:   <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>,
  star:     <svg viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/></svg>,
  starO:    <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z"/></svg>,
  chevD:    <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z"/></svg>,
  chevR:    <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/></svg>,
  check:    <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>,
  share:    <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.61 1.31 2.92 2.92 2.92 1.61 0 2.92-1.31 2.92-2.92s-1.31-2.92-2.92-2.92z"/></svg>,
  save:     <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20"><path d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/></svg>,
  edit:     <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>,
  trash:    <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>,
  calc:     <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/></svg>,
  chart:    <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>,
  home:     <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>,
  modules:  <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M20.5 11H19V7c0-1.1-.9-2-2-2h-4V3.5C13 2.12 11.88 1 10.5 1S8 2.12 8 3.5V5H4c-1.1 0-1.99.9-1.99 2v3.8H3.5c1.49 0 2.7 1.21 2.7 2.7s-1.21 2.7-2.7 2.7H2V20c0 1.1.9 2 2 2h3.8v-1.5c0-1.49 1.21-2.7 2.7-2.7 1.49 0 2.7 1.21 2.7 2.7V22H17c1.1 0 2-.9 2-2v-4h1.5c1.38 0 2.5-1.12 2.5-2.5S21.88 11 20.5 11z"/></svg>,
  market:   <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M19 6H17V5c0-1.1-.9-2-2-2H9c-1.1 0-2 .9-2 2v1H5c-1.1 0-2 .9-2 2v11c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-8 10H9v-4h2v4zm0-6H9V8h2v2zm4 6h-2v-4h2v4zm0-6h-2V8h2v2zm2-5H9V5h8v1z"/></svg>,
  person:   <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>,
  drag:     <svg viewBox="0 0 24 24" fill="currentColor" width="16" height="16"><path d="M11 18c0 1.1-.9 2-2 2s-2-.9-2-2 .9-2 2-2 2 .9 2 2zm-2-8c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm6 4c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/></svg>,
};

// ── Icon Button ────────────────────────────────────────────────
function IconBtn({ icon, onClick, color, size = 40, bg, round = true }) {
  return (
    <button onClick={onClick} style={{
      width: size, height: size, flexShrink: 0,
      borderRadius: round ? '50%' : 10,
      background: bg || 'transparent',
      border: 'none', cursor: 'pointer',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: color || 'var(--c-txt2)',
    }}>{icon}</button>
  );
}

// ── Top App Bar ────────────────────────────────────────────────
function TopBar({ title, subtitle, onBack, right }) {
  return (
    <div style={{
      height: 56, display: 'flex', alignItems: 'center', gap: 2,
      padding: '0 6px 0 4px', background: 'var(--c-bg)',
      borderBottom: '1px solid var(--c-line2)', flexShrink: 0, position: 'relative', zIndex: 10,
    }}>
      {onBack && <IconBtn icon={IC.back} onClick={onBack} />}
      <div style={{ flex: 1, minWidth: 0, paddingLeft: onBack ? 2 : 14 }}>
        <div style={{ fontWeight: 700, fontSize: 17, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
        {subtitle && <div style={{ fontSize: 11, color: 'var(--c-txt3)', marginTop: -2 }}>{subtitle}</div>}
      </div>
      {right && <div style={{ display: 'flex', gap: 2 }}>{right}</div>}
    </div>
  );
}

// ── Bottom Sheet ───────────────────────────────────────────────
function Sheet({ open, onClose, title, height = '70%', children, noPad }) {
  if (!open) return null;
  return (
    <div style={{ position: 'absolute', inset: 0, zIndex: 200, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
      <div onClick={onClose} style={{ position: 'absolute', inset: 0, background: 'rgba(0,0,0,.45)', backdropFilter: 'blur(3px)', animation: 'fadeIn 200ms' }} />
      <div style={{
        position: 'relative', zIndex: 1, background: 'var(--c-surf)',
        borderRadius: '20px 20px 0 0', height, boxShadow: 'var(--sh-sheet)',
        display: 'flex', flexDirection: 'column', overflow: 'hidden',
        animation: 'slideUp 260ms cubic-bezier(0.25, 0.8, 0.25, 1)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'center', padding: '10px 0 6px' }}>
          <div style={{ width: 36, height: 4, borderRadius: 2, background: 'var(--c-line)' }} />
        </div>
        {title && (
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '2px 20px 12px', borderBottom: '1px solid var(--c-line2)' }}>
            <span style={{ fontWeight: 700, fontSize: 16 }}>{title}</span>
            <IconBtn icon={IC.close} onClick={onClose} size={32} bg="var(--c-surf2)" />
          </div>
        )}
        <div style={{ flex: 1, overflowY: 'auto', padding: noPad ? 0 : '4px 0' }}>{children}</div>
      </div>
    </div>
  );
}

// ── Metric Tile ────────────────────────────────────────────────
function MTile({ label, value, unit, accent, big, sub }) {
  return (
    <div style={{
      background: 'var(--c-surf)', borderRadius: 14, padding: big ? '16px 18px' : '12px 14px',
      boxShadow: 'var(--sh1)', borderLeft: `3px solid ${accent || 'var(--c-prim)'}`,
      display: 'flex', flexDirection: 'column', gap: 2,
    }}>
      <div style={{ fontSize: 11, color: 'var(--c-txt3)', textTransform: 'uppercase', letterSpacing: 0.6 }}>{label}</div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 4 }}>
        <span style={{ fontSize: big ? 28 : 22, fontWeight: 800, color: accent || 'var(--c-txt1)', fontFamily: 'monospace', lineHeight: 1.1 }}>{value}</span>
        {unit && <span style={{ fontSize: 12, color: 'var(--c-txt3)', fontWeight: 400 }}>{unit}</span>}
      </div>
      {sub && <div style={{ fontSize: 11, color: 'var(--c-txt3)' }}>{sub}</div>}
    </div>
  );
}

// ── Param Row ──────────────────────────────────────────────────
function PRow({ label, value, unit, onChange, min = -200, max = 200, readOnly }) {
  const [editing, setEditing] = React.useState(false);
  const [draft, setDraft] = React.useState('');
  const startEdit = () => { if (!readOnly) { setDraft(String(value)); setEditing(true); } };
  const commit = () => {
    setEditing(false);
    const v = parseFloat(draft);
    if (!isNaN(v)) onChange?.(Math.min(max, Math.max(min, v)));
  };
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '11px 0', borderBottom: '1px solid var(--c-line2)' }}>
      <span style={{ color: 'var(--c-txt2)', fontSize: 14 }}>{label}</span>
      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        {editing
          ? <input autoFocus value={draft} onChange={e => setDraft(e.target.value)}
              onBlur={commit} onKeyDown={e => e.key === 'Enter' && commit()}
              style={{ width: 72, textAlign: 'right', padding: '4px 8px', background: 'var(--c-prim-tint)', border: '1px solid var(--c-prim)', borderRadius: 6, color: 'var(--c-prim)', fontSize: 14, fontFamily: 'monospace', outline: 'none' }} />
          : <button onClick={startEdit} style={{ background: readOnly ? 'transparent' : 'var(--c-surf2)', border: readOnly ? 'none' : '1px solid var(--c-line)', borderRadius: 6, padding: '4px 9px', cursor: readOnly ? 'default' : 'text', fontFamily: 'monospace', fontSize: 14, color: 'var(--c-txt1)', fontWeight: 600 }}>
              {typeof value === 'number' ? (Number.isInteger(value) ? value : value.toFixed(1)) : value}
            </button>
        }
        {unit && <span style={{ fontSize: 12, color: 'var(--c-txt3)', minWidth: 28 }}>{unit}</span>}
      </div>
    </div>
  );
}

// ── Chip Row ───────────────────────────────────────────────────
function Chips({ items, active, onSelect }) {
  return (
    <div style={{ display: 'flex', gap: 8, overflowX: 'auto', padding: '2px 0 8px' }}>
      {items.map(c => (
        <button key={c.id} onClick={() => onSelect(c.id)} style={{
          padding: '6px 14px', borderRadius: 9999,
          border: active === c.id ? 'none' : '1px solid var(--c-line)',
          background: active === c.id ? 'var(--c-prim)' : 'var(--c-surf)',
          color: active === c.id ? 'var(--c-on-prim)' : 'var(--c-txt2)',
          fontSize: 13, fontWeight: 500, cursor: 'pointer', whiteSpace: 'nowrap', flexShrink: 0,
          transition: 'all 150ms',
        }}>{c.label}</button>
      ))}
    </div>
  );
}

// ── Section Label ──────────────────────────────────────────────
function SLabel({ title, action, onAction }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '14px 0 6px' }}>
      <span style={{ fontWeight: 700, fontSize: 15 }}>{title}</span>
      {action && <button onClick={onAction} style={{ fontSize: 13, color: 'var(--c-prim)', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 500 }}>{action}</button>}
    </div>
  );
}

// ── Primary Button ─────────────────────────────────────────────
function PBtn({ children, onClick, wide, small, tonal, outlined }) {
  return (
    <button onClick={onClick} style={{
      borderRadius: 9999, border: outlined ? '1.5px solid var(--c-prim)' : 'none',
      background: tonal ? 'var(--c-prim-tint)' : outlined ? 'transparent' : 'var(--c-prim)',
      color: (tonal || outlined) ? 'var(--c-prim)' : 'var(--c-on-prim)',
      padding: small ? '8px 16px' : '12px 24px',
      fontSize: small ? 13 : 15, fontWeight: 600, cursor: 'pointer',
      width: wide ? '100%' : 'auto', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 6,
      transition: 'all 150ms',
    }}>{children}</button>
  );
}

// ── Status Badge ───────────────────────────────────────────────
function StatusBadge({ ok, label }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 4,
      padding: '3px 9px', borderRadius: 9999, fontSize: 12, fontWeight: 600,
      background: ok ? 'var(--c-ok-tint)' : 'var(--c-err-tint)',
      color: ok ? 'var(--c-ok)' : 'var(--c-err)',
    }}>
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: ok ? 'var(--c-ok)' : 'var(--c-err)', flexShrink: 0 }} />
      {label}
    </span>
  );
}

// ── Kind Badge ─────────────────────────────────────────────────
function KindBadge({ kind }) {
  const m = KIND_META[kind] || KIND_META.TX;
  return (
    <span style={{ fontSize: 10, fontWeight: 700, padding: '2px 6px', borderRadius: 4, background: m.bg, color: m.col, letterSpacing: 0.5 }}>
      {m.label}
    </span>
  );
}

// ── Bottom Nav Bar ─────────────────────────────────────────────
function BottomNav({ active, onNav }) {
  const tabs = [
    { id:'home',    label:'首页',  icon: IC.home },
    { id:'editor',  label:'工作区', icon: IC.calc },
    { id:'market',  label:'市场',  icon: IC.market },
    { id:'profile', label:'我的',  icon: IC.person },
  ];
  // Normalize 'results' → 'editor' for highlighting
  const currentTab = (active === 'results') ? 'editor' : (active || 'home');
  return (
    <div style={{
      height: 62, display: 'flex', borderTop: '1px solid var(--c-line2)',
      background: 'var(--c-bg)', flexShrink: 0,
    }}>
      {tabs.map(t => {
        const on = (currentTab === t.id);
        return (
          <button key={t.id} onClick={() => onNav(t.id)} style={{
            flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
            gap: 3, background: 'none', border: 'none', cursor: 'pointer',
            color: on ? 'var(--c-prim)' : 'var(--c-txt3)',
          }}>
            <div style={{
              width: 42, height: 28, borderRadius: 14,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              background: on ? 'var(--c-prim-tint)' : 'transparent',
            }}>
              {t.icon}
            </div>
            <span style={{ fontSize: 10, fontWeight: on ? 700 : 400 }}>{t.label}</span>
          </button>
        );
      })}
    </div>
  );
}

Object.assign(window, { IC, IconBtn, TopBar, Sheet, MTile, PRow, Chips, SLabel, PBtn, StatusBadge, KindBadge, BottomNav });
