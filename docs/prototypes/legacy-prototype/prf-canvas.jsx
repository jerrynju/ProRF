// prf-canvas.jsx — Workflow Editor + Vertical Node Canvas

// ── Vertical Node Card (full-width horizontal) ─────────────────
function NodeCard({ node, result, selected, onSelect, onEdit }) {
  const meta = KIND_META[node.kind] || KIND_META.TX;
  const mod  = MODS[node.moduleId] || {};
  const tr   = result?.trace?.find(t => t.nodeId === node.id);

  return (
    <div
      onClick={() => selected ? onEdit() : onSelect()}
      data-nodeid={node.id}
      style={{
        borderRadius: 14, overflow: 'hidden', cursor: 'pointer',
        background: 'var(--c-surf)',
        border: `2px solid ${selected ? meta.col : 'var(--c-line2)'}`,
        boxShadow: selected ? `0 0 0 3px ${meta.col}28, var(--sh2)` : 'var(--sh1)',
        transform: selected ? 'scale(1.015)' : 'scale(1)',
        transition: 'all 180ms cubic-bezier(0.2,0.8,0.2,1)',
        display: 'flex', flexDirection: 'column',
      }}>
      {/* Colour stripe + header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '9px 14px', background: selected ? meta.bg : 'transparent', borderBottom: `1px solid ${meta.col}22` }}>
        <div style={{ width: 36, height: 36, borderRadius: 10, background: meta.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 20, border: `1.5px solid ${meta.col}44`, flexShrink: 0 }}>
          {mod.emoji || '○'}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 1 }}>
            <span style={{ fontSize: 9, fontWeight: 800, padding: '2px 6px', borderRadius: 4, background: meta.col, color: '#fff', letterSpacing: 0.6, textTransform: 'uppercase' }}>{meta.label}</span>
            <span style={{ fontWeight: 700, fontSize: 14, color: 'var(--c-txt1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{node.name}</span>
          </div>
          {/* Params row */}
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            {Object.entries(node.params).slice(0, 3).map(([k, v]) => {
              const spec = mod.params?.find(p => p.k === k);
              if (!spec) return null;
              return (
                <span key={k} style={{ fontSize: 11, color: 'var(--c-txt3)' }}>
                  {spec.l}:<span style={{ fontFamily: 'monospace', fontWeight: 700, color: 'var(--c-txt2)', marginLeft: 2 }}>{typeof v === 'number' ? (Number.isInteger(v) ? v : v.toFixed(1)) : v}{spec.u}</span>
                </span>
              );
            })}
          </div>
        </div>
        {/* Computed value badge */}
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          <div style={{ fontSize: 17, fontWeight: 800, color: meta.col, fontFamily: 'monospace', lineHeight: 1.1 }}>{tr?.lbl || '—'}</div>
          {tr?.pwr != null && <div style={{ fontSize: 10, color: 'var(--c-txt4)', marginTop: 1 }}>→ {tr.pwr.toFixed(1)} dBm</div>}
        </div>
      </div>
    </div>
  );
}

// ── Compact Node Connector: line + power badge + inline "+" ──────
function NodeConnector({ pwr, onAdd, isLast }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div style={{ width: 2, height: 8, background: 'var(--c-line)', flexShrink: 0 }} />
      {/* Middle row: power badge + tiny "+" button side-by-side */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
        {pwr != null && (
          <span style={{ fontSize: 10, fontFamily: 'monospace', color: 'var(--c-prim)', fontWeight: 700, background: 'var(--c-prim-tint)', padding: '1px 7px', borderRadius: 8, border: '1px solid var(--c-line)', whiteSpace: 'nowrap' }}>
            {pwr.toFixed(1)} dBm
          </span>
        )}
        <button
          onClick={e => { e.stopPropagation(); onAdd(); }}
          title="插入模块"
          style={{
            width: 18, height: 18, borderRadius: '50%',
            background: 'var(--c-bg)', border: '1.5px dashed var(--c-line)',
            color: 'var(--c-txt4)', fontSize: 13, fontWeight: 700, lineHeight: 1,
            cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
            padding: 0, flexShrink: 0, transition: 'all 150ms',
          }}
        >+</button>
      </div>
      {isLast ? (
        <div style={{ height: 4 }} />
      ) : (
        <>
          <div style={{ width: 2, height: 8, background: 'var(--c-line)', flexShrink: 0 }} />
          <div style={{ width: 0, height: 0, borderLeft: '5px solid transparent', borderRight: '5px solid transparent', borderTop: '7px solid var(--c-txt3)', flexShrink: 0 }} />
        </>
      )}
    </div>
  );
}

// ── Right-side Quick Nav Dots ───────────────────────────────────
function QuickNav({ nodes, selId, onJump }) {
  return (
    <div style={{
      position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)',
      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5,
      background: 'var(--c-surf)', borderRadius: 20,
      boxShadow: 'var(--sh2)', padding: '8px 5px', zIndex: 20,
    }}>
      {nodes.map((nd, i) => {
        const meta = KIND_META[nd.kind] || KIND_META.TX;
        const active = nd.id === selId;
        return (
          <button key={nd.id} onClick={() => onJump(nd.id)}
            title={nd.name}
            style={{
              width: active ? 10 : 8, height: active ? 10 : 8,
              borderRadius: '50%', border: 'none', cursor: 'pointer', padding: 0,
              background: active ? meta.col : meta.border || meta.col + '55',
              transition: 'all 180ms', flexShrink: 0,
            }} />
        );
      })}
      {/* Kind legend mini icons */}
      <div style={{ width: 18, height: 1, background: 'var(--c-line)', margin: '2px 0' }} />
      {[['TX','var(--tx)'],['LOSS','var(--loss)'],['PROPAGATION','var(--prop)'],['RX','var(--rx)']].map(([k, c]) => {
        const count = nodes.filter(n => n.kind === k).length;
        if (!count) return null;
        return (
          <div key={k} style={{ width: 8, height: 8, borderRadius: 2, background: c, opacity: 0.7 }} title={`${KIND_META[k]?.label} ×${count}`} />
        );
      })}
    </div>
  );
}

// ── Node Properties Sheet ───────────────────────────────────────
function NodePropsSheet({ node, onClose, onUpdate, onDelete, onMoveUp, onMoveDown, canUp, canDown }) {
  if (!node) return null;
  const mod  = MODS[node.moduleId] || {};
  const meta = KIND_META[node.kind] || KIND_META.TX;
  return (
    <Sheet open={!!node} onClose={onClose} title={null} height="62%">
      <div style={{ background: meta.col, padding: '12px 20px 14px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 10, color: 'rgba(255,255,255,.75)', fontWeight: 700, letterSpacing: 0.8, textTransform: 'uppercase' }}>{meta.label} 模块</div>
            <div style={{ fontSize: 19, fontWeight: 800, color: '#fff', marginTop: 2 }}>{node.name}</div>
            <div style={{ fontSize: 12, color: 'rgba(255,255,255,.7)', marginTop: 1 }}>{mod.en || ''}</div>
          </div>
          <span style={{ fontSize: 32 }}>{mod.emoji || '○'}</span>
        </div>
        <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
          <button onClick={canUp ? onMoveUp : undefined} style={{ flex: 1, padding: '7px', borderRadius: 8, background: canUp ? 'rgba(255,255,255,.2)' : 'rgba(255,255,255,.07)', border: 'none', color: canUp ? '#fff' : 'rgba(255,255,255,.3)', cursor: canUp ? 'pointer' : 'default', fontSize: 13, fontWeight: 600 }}>↑ 上移</button>
          <button onClick={canDown ? onMoveDown : undefined} style={{ flex: 1, padding: '7px', borderRadius: 8, background: canDown ? 'rgba(255,255,255,.2)' : 'rgba(255,255,255,.07)', border: 'none', color: canDown ? '#fff' : 'rgba(255,255,255,.3)', cursor: canDown ? 'pointer' : 'default', fontSize: 13, fontWeight: 600 }}>↓ 下移</button>
          <button onClick={onDelete} style={{ padding: '7px 14px', borderRadius: 8, background: 'rgba(255,80,80,.3)', border: 'none', color: '#fff', cursor: 'pointer', fontSize: 13, fontWeight: 600 }}>删除</button>
        </div>
      </div>
      <div style={{ padding: '0 20px 20px' }}>
        <div style={{ padding: '10px 0 4px', fontSize: 11, color: 'var(--c-txt3)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: 0.6 }}>参数设置</div>
        {mod.params?.length > 0
          ? mod.params.map(p => (
              <PRow key={p.k} label={p.l} value={node.params[p.k] ?? p.d} unit={p.u}
                min={p.r[0]} max={p.r[1]}
                onChange={v => onUpdate({ ...node, params: { ...node.params, [p.k]: v } })} />
            ))
          : <div style={{ padding: '20px 0', color: 'var(--c-txt3)', fontSize: 14, textAlign: 'center' }}>此模块的参数由全局设置决定</div>
        }
      </div>
    </Sheet>
  );
}

// ── Module Picker Sheet ─────────────────────────────────────────
function ModPickerSheet({ open, onClose, onPick, insertAfterIdx }) {
  const [cat, setCat] = React.useState('all');
  const cats = [{ id:'all',label:'全部' },{ id:'TX',label:'发射端' },{ id:'LOSS',label:'损耗' },{ id:'PROPAGATION',label:'传播' },{ id:'RX',label:'接收端' }];
  const mods = Object.values(MODS).filter(m => cat === 'all' || m.kind === cat);
  return (
    <Sheet open={open} onClose={onClose} title="选择模块" height="75%">
      <div style={{ padding: '0 16px' }}>
        <Chips items={cats} active={cat} onSelect={setCat} />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, padding: '4px 16px 24px' }}>
        {mods.map(m => {
          const meta = KIND_META[m.kind] || KIND_META.TX;
          return (
            <button key={m.id} onClick={() => { onPick(m, insertAfterIdx); onClose(); }} style={{
              background: 'var(--c-surf)', border: `1.5px solid ${meta.col}44`,
              borderRadius: 12, padding: '12px', textAlign: 'left', cursor: 'pointer',
              display: 'flex', flexDirection: 'column', gap: 5,
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ fontSize: 20 }}>{m.emoji}</span>
                <span style={{ fontSize: 9, fontWeight: 800, padding: '2px 6px', borderRadius: 4, background: meta.col, color: '#fff', letterSpacing: 0.5, textTransform: 'uppercase' }}>{meta.label}</span>
              </div>
              <div style={{ fontWeight: 700, fontSize: 13, color: 'var(--c-txt1)' }}>{m.name}</div>
              <div style={{ fontSize: 11, color: 'var(--c-txt3)' }}>{m.en}</div>
            </button>
          );
        })}
      </div>
    </Sheet>
  );
}

// ── Global Params Sheet ─────────────────────────────────────────
function GlobalParamsSheet({ open, onClose, globals, onChange }) {
  return (
    <Sheet open={open} onClose={onClose} title="全局参数" height="56%">
      <div style={{ padding: '0 20px 24px' }}>
        <PRow label="工作频率" value={globals.frequencyMHz} unit="MHz" min={1} max={300000} onChange={v => onChange({ ...globals, frequencyMHz: v })} />
        <PRow label="信号带宽" value={globals.bandwidthMHz} unit="MHz" min={0.001} max={10000} onChange={v => onChange({ ...globals, bandwidthMHz: v })} />
        <PRow label="传输距离" value={globals.distanceKm} unit="km" min={0.001} max={1000000} onChange={v => onChange({ ...globals, distanceKm: v })} />
        <PRow label="系统温度" value={globals.temperatureK} unit="K" min={10} max={1000} onChange={v => onChange({ ...globals, temperatureK: v })} />
        <div style={{ marginTop: 20 }}><PBtn onClick={onClose} wide>应用参数</PBtn></div>
      </div>
    </Sheet>
  );
}

// ── Results Preview Bar ─────────────────────────────────────────
function ResultsBar({ result, onViewFull }) {
  if (!result) return null;
  const { margin, rxPwr, eirp, snr, isValid } = result;
  return (
    <div style={{ background: 'var(--c-surf)', borderTop: '1px solid var(--c-line2)', padding: '10px 14px', flexShrink: 0 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontWeight: 700, fontSize: 13 }}>链路预算结果</span>
          <StatusBadge ok={isValid} label={isValid ? '正常' : '告警'} />
        </div>
        <button onClick={onViewFull} style={{ fontSize: 12, color: 'var(--c-prim)', background: 'none', border: 'none', cursor: 'pointer', fontWeight: 600 }}>查看详情 →</button>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 6 }}>
        {[
          { l:'链路余量', v: margin?.toFixed(2), u:'dB', c: isValid ? 'var(--c-ok)' : 'var(--c-err)' },
          { l:'接收功率', v: rxPwr?.toFixed(2),  u:'dBm', c:'var(--c-prim)' },
          { l:'EIRP',    v: eirp?.toFixed(1),   u:'dBm', c:'var(--c-txt1)' },
          { l:'SNR',     v: snr?.toFixed(1),    u:'dB',  c:'var(--c-sec)' },
        ].map(m => (
          <div key={m.l} style={{ background: 'var(--c-bg2)', borderRadius: 8, padding: '6px 8px' }}>
            <div style={{ fontSize: 9, color: 'var(--c-txt3)', marginBottom: 1, textTransform: 'uppercase' }}>{m.l}</div>
            <div style={{ fontSize: 13, fontWeight: 800, color: m.c, fontFamily: 'monospace' }}>{m.v ?? '—'}</div>
            <div style={{ fontSize: 9, color: 'var(--c-txt4)' }}>{m.u}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Workflow Editor Screen ──────────────────────────────────────
function WorkflowEditorScreen({ wf, onBack, onViewResults, onSave }) {
  const [nodes, setNodes]       = React.useState(() => wf.nodes.map(n => ({ ...n, params: { ...n.params } })));
  const [globals, setGlobals]   = React.useState(wf.globals);
  const [selId, setSelId]       = React.useState(null);
  const [sheet, setSheet]       = React.useState(null);
  const [addAfter, setAddAfter] = React.useState(-1);
  const canvasRef               = React.useRef(null);
  const nodeRefs                = React.useRef({});

  const result  = React.useMemo(() => RFE.evaluate(nodes, globals), [nodes, globals]);
  const selNode = nodes.find(n => n.id === selId);
  const selIdx  = nodes.findIndex(n => n.id === selId);

  const updateNode = nd => setNodes(ns => ns.map(n => n.id === nd.id ? nd : n));
  const deleteNode = () => { setNodes(ns => ns.filter(n => n.id !== selId)); setSelId(null); setSheet(null); };
  const moveNode = dir => {
    setNodes(ns => {
      const a = [...ns], i = a.findIndex(n => n.id === selId), j = i + dir;
      if (j < 0 || j >= a.length) return ns;
      [a[i], a[j]] = [a[j], a[i]]; return a;
    });
  };
  const addNode = (mod, afterIdx) => {
    const nd = { id: 'n' + Date.now(), kind: mod.kind, moduleId: mod.id, name: mod.name, params: Object.fromEntries((mod.params || []).map(p => [p.k, p.d])) };
    setNodes(ns => { const a = [...ns]; a.splice(afterIdx + 1, 0, nd); return a; });
  };

  // Quick nav: scroll to node
  const jumpToNode = (id) => {
    setSelId(id);
    const el = nodeRefs.current[id];
    if (el && canvasRef.current) {
      const top = el.offsetTop - 20;
      canvasRef.current.scrollTo({ top, behavior: 'smooth' });
    }
  };

  // Global params bar summary
  const gFreq = globals.frequencyMHz >= 1000 ? `${(globals.frequencyMHz/1000).toFixed(globals.frequencyMHz>=10000?0:1)}GHz` : `${globals.frequencyMHz}MHz`;
  const gDist = `${globals.distanceKm >= 10000 ? globals.distanceKm.toLocaleString('en') : globals.distanceKm}km`;

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', position: 'relative' }}>
      {/* Top Bar */}
      <TopBar title={wf.name} subtitle={wf.tags?.join(' · ')} onBack={onBack}
        right={[
          <IconBtn key="sav" icon={IC.save} onClick={onSave} color="var(--c-prim)" />,
          <IconBtn key="shr" icon={IC.share} />,
        ]} />

      {/* Global Params Quick Bar */}
      <button onClick={() => setSheet('params')} style={{
        display: 'flex', alignItems: 'center', gap: 10, padding: '7px 14px',
        background: 'var(--c-bg2)', border: 'none', borderBottom: '1px solid var(--c-line2)',
        cursor: 'pointer', width: '100%', textAlign: 'left',
      }}>
        {[['频率', gFreq], ['带宽', `${globals.bandwidthMHz}MHz`], ['距离', gDist], ['温度', `${globals.temperatureK}K`]].map(([l, v]) => (
          <div key={l} style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
            <span style={{ fontSize: 9, color: 'var(--c-txt4)', textTransform: 'uppercase', letterSpacing: 0.4 }}>{l}</span>
            <span style={{ fontSize: 12, fontWeight: 700, color: 'var(--c-txt1)', fontFamily: 'monospace' }}>{v}</span>
          </div>
        ))}
        <span style={{ color: 'var(--c-txt3)', fontSize: 12, flexShrink: 0 }}>{IC.chevD}</span>
      </button>

      {/* Vertical Node Canvas */}
      <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
        <div ref={canvasRef} style={{
          height: '100%', overflowY: 'auto', overflowX: 'hidden',
          padding: '14px 46px 14px 14px',
          background: 'var(--c-bg)',
          backgroundImage: 'radial-gradient(var(--c-line2) 1px, transparent 1px)',
          backgroundSize: '20px 20px',
        }}>
          {/* Compact add-before-first button */}
          <div style={{ display: 'flex', justifyContent: 'center', padding: '2px 0 0' }}>
            <button onClick={() => { setAddAfter(-1); setSheet('add'); }} title="在顶部插入模块" style={{
              width: 22, height: 22, borderRadius: '50%',
              background: 'var(--c-bg)', border: '1.5px dashed var(--c-line)',
              color: 'var(--c-txt4)', fontSize: 15, fontWeight: 600, lineHeight: 1,
              cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
              padding: 0, transition: 'all 150ms',
            }}>+</button>
          </div>

          {nodes.map((nd, i) => (
            <React.Fragment key={nd.id}>
              <div ref={el => { if (el) nodeRefs.current[nd.id] = el; }}>
                <NodeCard
                  node={nd} result={result}
                  selected={selId === nd.id}
                  onSelect={() => setSelId(nd.id)}
                  onEdit={() => { setSelId(nd.id); setSheet('node'); }}
                />
              </div>
              <NodeConnector
                pwr={result?.trace?.[i]?.pwr ?? null}
                onAdd={() => { setAddAfter(i); setSheet('add'); }}
                isLast={i === nodes.length - 1}
              />
            </React.Fragment>
          ))}
          <div style={{ height: 4 }} />
        </div>

        {/* Right quick-nav dots */}
        <QuickNav nodes={nodes} selId={selId} onJump={jumpToNode} />
      </div>

      {/* Results Bar */}
      <ResultsBar result={result} onViewFull={onViewResults} />

      {/* Sheets */}
      <NodePropsSheet
        node={sheet === 'node' ? selNode : null}
        onClose={() => { setSheet(null); setSelId(null); }}
        onUpdate={updateNode} onDelete={deleteNode}
        onMoveUp={() => moveNode(-1)} onMoveDown={() => moveNode(1)}
        canUp={selIdx > 0} canDown={selIdx < nodes.length - 1}
      />
      <ModPickerSheet open={sheet === 'add'} onClose={() => setSheet(null)} onPick={addNode} insertAfterIdx={addAfter} />
      <GlobalParamsSheet open={sheet === 'params'} onClose={() => setSheet(null)} globals={globals} onChange={setGlobals} />
    </div>
  );
}

Object.assign(window, { WorkflowEditorScreen });
