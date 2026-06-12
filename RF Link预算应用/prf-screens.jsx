// prf-screens.jsx — Home, Results, Market, Library, Settings

// ── Home Screen ────────────────────────────────────────────────
function HomeScreen({ workflows, onOpen, onCreate }) {
  const [search, setSearch] = React.useState('');
  const [currentPlan, setCurrentPlan] = React.useState('free'); // 'free' or 'pro'
  const filtered = workflows.filter(w =>
    w.name.includes(search) || w.tags?.some(t => t.includes(search))
  );
  
  return (
    <div style={{ flex:1, overflowY:'auto', background:'var(--c-bg)' }}>
      {/* Hero */}
      <div style={{ background:'linear-gradient(135deg, var(--c-prim-dim) 0%, var(--c-prim) 100%)', padding:'20px 20px 24px', color:'#fff' }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:12 }}>
          <div>
            <div style={{ fontSize:22, fontWeight:800, letterSpacing:-0.5 }}>ProRF</div>
            <div style={{ fontSize:12, opacity:.75, marginTop:1 }}>射频链路预算计算平台</div>
          </div>
          <div style={{ width:44, height:44, borderRadius:14, background:'rgba(255,255,255,.15)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:22 }}>📡</div>
        </div>
        {/* Search */}
        <div style={{ display:'flex', alignItems:'center', gap:8, background:'rgba(255,255,255,.18)', borderRadius:12, padding:'8px 12px' }}>
          <span style={{ opacity:.7 }}>{IC.search}</span>
          <input value={search} onChange={e => setSearch(e.target.value)} placeholder="搜索工作流、标签…"
            style={{ flex:1, background:'none', border:'none', outline:'none', color:'#fff', fontSize:14, '::placeholder':{ color:'rgba(255,255,255,.6)' } }} />
        </div>
      </div>

      <div style={{ padding:'0 16px 24px' }}>
        {/* Subscription Status Banner */}
        {currentPlan === 'free' && (
          <div style={{
            background:'linear-gradient(135deg, var(--c-warn-tint) 0%, var(--c-gold-tint) 100%)',
            borderRadius:14, padding:'14px 16px', marginTop:16, marginBottom:16,
            border:'1px solid var(--c-gold)',
          }}>
            <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:12 }}>
              <div style={{ flex:1 }}>
                <div style={{ fontWeight:700, fontSize:13, color:'var(--c-gold)', marginBottom:3 }}>升级到 Pro 版 🚀</div>
                <div style={{ fontSize:12, color:'var(--c-txt2)' }}>无限保存链路、高级分析、云端同步等功能</div>
              </div>
              <button style={{
                padding:'6px 12px', borderRadius:8,
                background:'var(--c-gold)', color:'#fff',
                border:'none', fontWeight:700, fontSize:11, cursor:'pointer',
                whiteSpace:'nowrap',
              }}>
                立即升级
              </button>
            </div>
          </div>
        )}

        {/* My Projects */}
        <SLabel title="我的链路" action="+ 新建" onAction={onCreate} />
        <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
          {filtered.map(wf => {
            const res = RFE.evaluate(wf.nodes, wf.globals);
            return (
              <button key={wf.id} onClick={() => onOpen(wf)} style={{
                background:'var(--c-surf)', border:'1px solid var(--c-line2)', borderRadius:16,
                padding:'14px 16px', cursor:'pointer', textAlign:'left', width:'100%',
                boxShadow:'var(--sh1)', transition:'all 150ms',
              }}>
                <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:8 }}>
                  <div style={{ flex:1 }}>
                    <div style={{ fontWeight:700, fontSize:15, color:'var(--c-txt1)', marginBottom:3 }}>{wf.name}</div>
                    <div style={{ display:'flex', gap:6, flexWrap:'wrap' }}>
                      {wf.tags?.map(t => (
                        <span key={t} style={{ fontSize:11, padding:'2px 8px', borderRadius:9999, background:'var(--c-prim-tint)', color:'var(--c-prim)', fontWeight:600 }}>{t}</span>
                      ))}
                    </div>
                  </div>
                  <StatusBadge ok={res.isValid} label={res.isValid ? '正常' : '告警'} />
                </div>
                <div style={{ display:'grid', gridTemplateColumns:'repeat(4,1fr)', gap:8 }}>
                  {[
                    { l:'链路余量', v: res.margin?.toFixed(1), u:'dB', c: res.isValid ? 'var(--c-ok)' : 'var(--c-err)' },
                    { l:'接收功率', v: res.rxPwr?.toFixed(1), u:'dBm', c:'var(--c-prim)' },
                    { l:'EIRP', v: res.eirp?.toFixed(1), u:'dBm', c:'var(--c-txt1)' },
                    { l:'节点数', v: wf.nodes.length, u:'个', c:'var(--c-txt2)' },
                  ].map(m => (
                    <div key={m.l} style={{ background:'var(--c-bg2)', borderRadius:8, padding:'6px 8px' }}>
                      <div style={{ fontSize:9, color:'var(--c-txt4)', textTransform:'uppercase', marginBottom:2 }}>{m.l}</div>
                      <div style={{ fontSize:14, fontWeight:800, color:m.c, fontFamily:'monospace' }}>{m.v}</div>
                      <div style={{ fontSize:9, color:'var(--c-txt4)' }}>{m.u}</div>
                    </div>
                  ))}
                </div>
                <div style={{ fontSize:11, color:'var(--c-txt4)', marginTop:8 }}>最近修改 · {wf.updatedAt}</div>
              </button>
            );
          })}
        </div>

        {/* Templates */}
        <SLabel title="推荐模板" action="查看全部" onAction={() => {}} />
        <div style={{ display:'flex', gap:10, overflowX:'auto', padding:'2px 0 4px' }}>
          {['卫星通信','5G NR','微波链路','雷达系统'].map((t,i) => (
            <button key={t} onClick={() => onCreate(t)} style={{
              flexShrink:0, background:'var(--c-surf)', border:'1px solid var(--c-line2)', borderRadius:14,
              padding:'12px 16px', cursor:'pointer', minWidth:110, textAlign:'center',
            }}>
              <div style={{ fontSize:24, marginBottom:6 }}>{['🛰️','📶','📡','🎯'][i]}</div>
              <div style={{ fontSize:12, fontWeight:700, color:'var(--c-txt1)' }}>{t}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Results Screen ─────────────────────────────────────────────
function ResultsScreen({ wf, onBack }) {
  const [tab, setTab] = React.useState('summary');
  const nodes  = wf.nodes;
  const globals = wf.globals;
  const result = React.useMemo(() => RFE.evaluate(nodes, globals), [nodes, globals]);
  const tabs = [{ id:'summary', l:'摘要' },{ id:'table', l:'明细' },{ id:'chart', l:'图表' },{ id:'sankey', l:'Sankey' }];

  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>
      <TopBar title="链路分析" subtitle={wf.name} onBack={onBack}
        right={[<IconBtn key="s" icon={IC.share} />, <IconBtn key="m" icon={IC.more} />]} />
      {/* Tab Bar */}
      <div style={{ display:'flex', background:'var(--c-bg)', borderBottom:'1px solid var(--c-line2)', padding:'0 12px' }}>
        {tabs.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)} style={{
            flex:1, padding:'12px 4px', background:'none', border:'none', cursor:'pointer',
            fontSize:13, fontWeight: tab===t.id ? 700 : 400,
            color: tab===t.id ? 'var(--c-prim)' : 'var(--c-txt3)',
            borderBottom: tab===t.id ? '2.5px solid var(--c-prim)' : '2.5px solid transparent',
            transition:'all 150ms',
          }}>{t.l}</button>
        ))}
      </div>
      <div style={{ flex:1, overflowY:'auto', background:'var(--c-bg2)', padding:'16px' }}>
        {tab === 'summary' && <SummaryTab result={result} />}
        {tab === 'table'   && <TableTab   result={result} />}
        {tab === 'chart'   && <ChartTab   result={result} globals={globals} />}
        {tab === 'sankey'  && <SankeyTab  result={result} />}
      </div>
    </div>
  );
}

function SummaryTab({ result }) {
  const { eirp, rxPwr, nf, margin, snr, isValid, nfFloor } = result;
  return (
    <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12 }}>
        <MTile label="链路余量" value={margin?.toFixed(2)} unit="dB" accent={isValid?'var(--c-ok)':'var(--c-err)'} big />
        <MTile label="接收功率" value={rxPwr?.toFixed(2)} unit="dBm" accent="var(--c-prim)" big />
        <MTile label="EIRP" value={eirp?.toFixed(1)} unit="dBm" accent="var(--c-txt2)" />
        <MTile label="SNR" value={snr?.toFixed(1)} unit="dB" accent="var(--c-sec)" />
        <MTile label="噪声底" value={nfFloor?.toFixed(1)} unit="dBm" accent="var(--c-warn)" />
        <MTile label="灵敏度门限" value={nf?.toFixed(1)} unit="dBm" accent="var(--c-txt3)" />
      </div>
      <div style={{ background:'var(--c-surf)', borderRadius:14, padding:'14px 16px', boxShadow:'var(--sh1)' }}>
        <div style={{ fontWeight:700, fontSize:14, marginBottom:8 }}>链路状态</div>
        <div style={{ display:'flex', alignItems:'center', gap:10 }}>
          <StatusBadge ok={isValid} label={isValid ? '链路正常 ✓' : '链路告警 !'} />
          <span style={{ fontSize:13, color:'var(--c-txt2)' }}>{isValid ? `余量充足 ${margin?.toFixed(1)} dB` : `余量不足 ${margin?.toFixed(1)} dB`}</span>
        </div>
        {!isValid && <div style={{ marginTop:8, fontSize:12, color:'var(--c-err)', background:'var(--c-err-tint)', padding:'8px 10px', borderRadius:8 }}>建议增加发射功率、提高天线增益或缩短传输距离。</div>}
      </div>
    </div>
  );
}

function TableTab({ result }) {
  const { trace } = result;
  return (
    <div style={{ background:'var(--c-surf)', borderRadius:14, overflow:'hidden', boxShadow:'var(--sh1)' }}>
      <div style={{ display:'grid', gridTemplateColumns:'1fr 60px 60px 80px', padding:'10px 12px', borderBottom:'1px solid var(--c-line2)', background:'var(--c-bg2)' }}>
        {['模块','增益','损耗','输出功率'].map(h => <span key={h} style={{ fontSize:11, fontWeight:700, color:'var(--c-txt3)', textTransform:'uppercase' }}>{h}</span>)}
      </div>
      {trace.map((t, i) => {
        const meta = KIND_META[t.kind] || KIND_META.TX;
        return (
          <div key={t.nodeId} style={{ display:'grid', gridTemplateColumns:'1fr 60px 60px 80px', padding:'10px 12px', borderBottom: i < trace.length-1 ? '1px solid var(--c-line2)' : 'none', alignItems:'center' }}>
            <div style={{ display:'flex', alignItems:'center', gap:8 }}>
              <div style={{ width:8, height:8, borderRadius:'50%', background:meta.col, flexShrink:0 }} />
              <span style={{ fontSize:13, fontWeight:600, color:'var(--c-txt1)' }}>{t.name}</span>
            </div>
            <span style={{ fontSize:13, fontFamily:'monospace', color:'var(--c-ok)', fontWeight:600 }}>{t.gain > 0 ? `+${t.gain.toFixed(1)}` : '—'}</span>
            <span style={{ fontSize:13, fontFamily:'monospace', color:'var(--c-err)', fontWeight:600 }}>{t.loss > 0 ? `−${t.loss.toFixed(1)}` : '—'}</span>
            <span style={{ fontSize:13, fontFamily:'monospace', color:'var(--c-prim)', fontWeight:700 }}>{t.pwr.toFixed(1)}<span style={{ fontSize:10, color:'var(--c-txt4)', fontWeight:400 }}> dBm</span></span>
          </div>
        );
      })}
    </div>
  );
}

function ChartTab({ result, globals }) {
  const { chartData, nfFloor, nf } = result;
  if (!chartData || chartData.length === 0) return <div style={{ padding:40, textAlign:'center', color:'var(--c-txt3)' }}>无图表数据</div>;
  const W = 330, H = 200, PL = 48, PR = 16, PT = 16, PB = 32;
  const cw = W - PL - PR, ch = H - PT - PB;
  const snrs = chartData.map(d => d.snr), margins = chartData.map(d => d.margin);
  const yMin = Math.min(...snrs, ...margins, -20), yMax = Math.max(...snrs, ...margins, 40);
  const dists = chartData.map(d => d.d);
  const xMin = Math.log10(dists[0]), xMax = Math.log10(dists[dists.length-1]);
  const tx = d => PL + (Math.log10(d) - xMin) / (xMax - xMin) * cw;
  const ty = v => PT + (1 - (v - yMin) / (yMax - yMin)) * ch;
  const line = arr => arr.map((v, i) => `${i===0?'M':'L'}${tx(dists[i]).toFixed(1)},${ty(v).toFixed(1)}`).join(' ');
  const yTicks = [-20, 0, 10, 20, 30, 40].filter(v => v >= yMin && v <= yMax);
  return (
    <div style={{ background:'var(--c-surf)', borderRadius:14, padding:'16px', boxShadow:'var(--sh1)' }}>
      <div style={{ fontWeight:700, fontSize:14, marginBottom:12 }}>SNR / 余量 vs 传输距离</div>
      <svg width={W} height={H} style={{ overflow:'visible', display:'block', maxWidth:'100%' }}>
        {/* Grid */}
        {yTicks.map(v => (
          <g key={v}>
            <line x1={PL} y1={ty(v)} x2={W-PR} y2={ty(v)} stroke="var(--c-line2)" strokeWidth="1"/>
            <text x={PL-4} y={ty(v)+4} textAnchor="end" fontSize="9" fill="var(--c-txt4)">{v}</text>
          </g>
        ))}
        {/* Zero line */}
        {yMin < 0 && yMax > 0 && <line x1={PL} y1={ty(0)} x2={W-PR} y2={ty(0)} stroke="var(--c-err)" strokeWidth="1" strokeDasharray="4 3" opacity=".5"/>}
        {/* Curves */}
        <path d={line(snrs)} fill="none" stroke="var(--c-prim)" strokeWidth="2" strokeLinejoin="round"/>
        <path d={line(margins)} fill="none" stroke="var(--c-ok)" strokeWidth="2" strokeLinejoin="round" strokeDasharray="5 3"/>
        {/* Current distance marker */}
        <line x1={tx(globals.distanceKm)} y1={PT} x2={tx(globals.distanceKm)} y2={H-PB} stroke="var(--c-warn)" strokeWidth="1.5" strokeDasharray="3 2"/>
        {/* Axes */}
        <line x1={PL} y1={PT} x2={PL} y2={H-PB} stroke="var(--c-line)" strokeWidth="1.5"/>
        <line x1={PL} y1={H-PB} x2={W-PR} y2={H-PB} stroke="var(--c-line)" strokeWidth="1.5"/>
        <text x={PL-36} y={PT+ch/2} textAnchor="middle" fontSize="9" fill="var(--c-txt3)" transform={`rotate(-90,${PL-36},${PT+ch/2})`}>dB</text>
        <text x={PL+cw/2} y={H-2} textAnchor="middle" fontSize="9" fill="var(--c-txt3)">距离 (km)</text>
      </svg>
      <div style={{ display:'flex', gap:16, marginTop:8 }}>
        {[['var(--c-prim)','SNR (dB)','solid'],['var(--c-ok)','链路余量 (dB)','dashed'],['var(--c-warn)','当前距离','solid']].map(([c,l,d]) => (
          <div key={l} style={{ display:'flex', alignItems:'center', gap:5, fontSize:11, color:'var(--c-txt3)' }}>
            <svg width="20" height="8"><line x1="0" y1="4" x2="20" y2="4" stroke={c} strokeWidth="2" strokeDasharray={d==='dashed'?'4 3':'none'}/></svg>
            {l}
          </div>
        ))}
      </div>
    </div>
  );
}

function SankeyTab({ result }) {
  const { trace } = result;
  if (!trace || trace.length === 0) return <div style={{ padding:40, textAlign:'center', color:'var(--c-txt3)' }}>无数据</div>;
  const W = 340, H = 220;
  const maxP = Math.max(...trace.map(t => Math.abs(t.pwr)), 10);
  const barH = (v) => Math.max(4, Math.abs(v) / maxP * 80);
  const nodeW = Math.floor((W - 32) / trace.length - 8);
  return (
    <div style={{ background:'var(--c-surf)', borderRadius:14, padding:'16px', boxShadow:'var(--sh1)' }}>
      <div style={{ fontWeight:700, fontSize:14, marginBottom:12 }}>功率流向 (Sankey)</div>
      <svg width={W} height={H} style={{ overflow:'visible', display:'block', maxWidth:'100%' }}>
        {trace.map((t, i) => {
          const meta = KIND_META[t.kind] || KIND_META.TX;
          const x = 16 + i * (nodeW + 8);
          const pwrH = barH(t.pwr);
          const gainH = barH(t.gain);
          const lossH = barH(t.loss);
          const cy = H / 2;
          return (
            <g key={t.nodeId}>
              {/* Connecting band */}
              {i > 0 && (
                <rect x={x - 8} y={cy - pwrH / 2 - 2} width={8} height={pwrH + 4} fill={meta.col} opacity=".25"/>
              )}
              {/* Node bar */}
              <rect x={x} y={cy - pwrH / 2} width={nodeW} height={pwrH} rx="4" fill={meta.col} opacity=".8"/>
              {/* Gain indicator */}
              {t.gain > 0 && <rect x={x} y={cy - pwrH / 2 - gainH - 2} width={nodeW} height={gainH} rx="3" fill="var(--c-ok)" opacity=".6"/>}
              {/* Loss indicator */}
              {t.loss > 0 && <rect x={x} y={cy + pwrH / 2 + 2} width={nodeW} height={lossH} rx="3" fill="var(--c-err)" opacity=".4"/>}
              {/* Power label */}
              <text x={x + nodeW / 2} y={cy - pwrH / 2 - (t.gain > 0 ? gainH + 8 : 4)} textAnchor="middle" fontSize="9" fill="var(--c-prim)" fontWeight="700" fontFamily="monospace">
                {t.pwr.toFixed(0)}
              </text>
              {/* Name label */}
              <text x={x + nodeW / 2} y={H - 6} textAnchor="middle" fontSize="8" fill="var(--c-txt3)">
                {t.name.slice(0, 5)}
              </text>
            </g>
          );
        })}
        {/* Y axis label */}
        <text x={8} y={H/2+4} textAnchor="middle" fontSize="9" fill="var(--c-txt4)">dBm</text>
      </svg>
      <div style={{ display:'flex', gap:14, marginTop:8 }}>
        {[['var(--c-ok)','增益'],['var(--c-err)','损耗'],['var(--c-prim)','输出功率']].map(([c,l]) => (
          <div key={l} style={{ display:'flex', alignItems:'center', gap:5, fontSize:11, color:'var(--c-txt3)' }}>
            <div style={{ width:10, height:10, borderRadius:2, background:c, opacity:.8 }} />{l}
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Module Library Screen (Market) ────────────────────────────
function MarketScreen() {
  const [tab, setTab] = React.useState('market'); // 'market' or 'owned'
  const [cat, setCat] = React.useState('all');
  const cats = [{ id:'all',label:'全部' },{ id:'卫星通信',label:'卫星通信' },{ id:'5G/6G',label:'5G/6G' },{ id:'微波链路',label:'微波链路' },{ id:'雷达',label:'雷达' }];
  const items = MODULES_MARKET.filter(m => cat === 'all' || m.cat === cat);

  return (
    <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden' }}>
      {/* Header */}
      <div style={{ background:'linear-gradient(135deg,var(--c-sec) 0%,var(--c-prim) 100%)', padding:'20px 20px 16px', color:'#fff' }}>
        <div style={{ fontSize:18, fontWeight:800, marginBottom:12 }}>模块库</div>
        {/* Tab buttons */}
        <div style={{ display:'flex', gap:8 }}>
          <button onClick={() => { setTab('market'); setCat('all'); }} style={{
            flex:1, padding:'10px 12px', borderRadius:10,
            background: tab==='market' ? 'rgba(255,255,255,.25)' : 'rgba(255,255,255,.1)',
            border:'none', color:'#fff', fontWeight:700, fontSize:12, cursor:'pointer',
            transition:'all 150ms',
          }}>
            📥 可下载市场
          </button>
          <button onClick={() => setTab('owned')} style={{
            flex:1, padding:'10px 12px', borderRadius:10,
            background: tab==='owned' ? 'rgba(255,255,255,.25)' : 'rgba(255,255,255,.1)',
            border:'none', color:'#fff', fontWeight:700, fontSize:12, cursor:'pointer',
            transition:'all 150ms',
          }}>
            ✓ 我已拥有 ({MY_MODULES.length})
          </button>
        </div>
      </div>

      {/* Content */}
      <div style={{ flex:1, overflowY:'auto', background:'var(--c-bg)' }}>
        {tab === 'market' && (
          <div style={{ padding:'16px' }}>
            {/* Search */}
            <div style={{ display:'flex', alignItems:'center', gap:8, background:'var(--c-surf)', borderRadius:12, padding:'8px 12px', marginBottom:16, border:'1px solid var(--c-line2)' }}>
              <span style={{ opacity:.6 }}>{IC.search}</span>
              <input placeholder="搜索模块…" style={{ flex:1, background:'none', border:'none', outline:'none', fontSize:14 }} />
            </div>

            {/* Category filter */}
            <Chips items={cats} active={cat} onSelect={setCat} />

            {/* Module list */}
            <div style={{ display:'flex', flexDirection:'column', gap:12, marginTop:12 }}>
              {items.map(item => (
                <div key={item.id} style={{ background:'var(--c-surf)', border:'1px solid var(--c-line2)', borderRadius:16, padding:'14px 16px', boxShadow:'var(--sh1)' }}>
                  <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:8 }}>
                    <div style={{ display:'flex', alignItems:'center', gap:10, flex:1 }}>
                      <div style={{ width:44, height:44, borderRadius:12, background:'var(--c-prim-tint)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:22 }}>
                        {['🛰️','📡','📶','🔭','📻','🎯'][MODULES_MARKET.indexOf(item) % 6]}
                      </div>
                      <div style={{ flex:1 }}>
                        <div style={{ fontWeight:700, fontSize:14, color:'var(--c-txt1)', marginBottom:2 }}>{item.name}</div>
                        <div style={{ fontSize:11, color:'var(--c-txt3)' }}>by {item.author}</div>
                      </div>
                    </div>
                    <div style={{ textAlign:'right' }}>
                      <div style={{ fontSize:16, fontWeight:800, color: item.price===0 ? 'var(--c-ok)' : 'var(--c-warn)' }}>
                        {item.price === 0 ? '免费' : `¥${item.price}`}
                      </div>
                    </div>
                  </div>
                  <div style={{ fontSize:12, color:'var(--c-txt2)', marginBottom:8 }}>{item.desc}</div>
                  <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
                    <div style={{ display:'flex', alignItems:'center', gap:6 }}>
                      <span style={{ color:'#F5A623', fontSize:12 }}>{IC.star}</span>
                      <span style={{ fontSize:11, fontWeight:700, color:'var(--c-txt2)' }}>{item.rating}</span>
                      <span style={{ fontSize:11, color:'var(--c-txt4)' }}>({item.dl.toLocaleString()} 次下载)</span>
                    </div>
                    <button style={{
                      padding:'6px 12px', borderRadius:8,
                      background:'var(--c-prim)', color:'#fff',
                      border:'none', fontWeight:700, fontSize:11, cursor:'pointer',
                    }}>
                      {item.price === 0 ? '下载' : '购买'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {tab === 'owned' && (
          <div style={{ padding:'16px' }}>
            {MY_MODULES.length === 0 ? (
              <div style={{ textAlign:'center', padding:'40px 20px', color:'var(--c-txt3)' }}>
                <div style={{ fontSize:32, marginBottom:8 }}>📦</div>
                <div style={{ fontSize:14, fontWeight:600, marginBottom:4 }}>还没有安装任何模块</div>
                <div style={{ fontSize:12 }}>从市场中下载模块来扩展功能</div>
              </div>
            ) : (
              <div style={{ display:'flex', flexDirection:'column', gap:12 }}>
                {MY_MODULES.map(mod => (
                  <div key={mod.id} style={{ background:'var(--c-surf)', border:'1px solid var(--c-line2)', borderRadius:16, padding:'14px 16px', boxShadow:'var(--sh1)' }}>
                    <div style={{ display:'flex', alignItems:'flex-start', justifyContent:'space-between', marginBottom:8 }}>
                      <div style={{ flex:1 }}>
                        <div style={{ fontWeight:700, fontSize:14, color:'var(--c-txt1)', marginBottom:3 }}>{mod.name}</div>
                        <div style={{ display:'flex', gap:12, fontSize:11, color:'var(--c-txt4)' }}>
                          <span>版本 {mod.version}</span>
                          <span>•</span>
                          <span>{mod.size}</span>
                          <span>•</span>
                          <span>装于 {mod.installedAt}</span>
                        </div>
                      </div>
                      <div style={{ display:'flex', gap:6 }}>
                        <button style={{
                          padding:'6px 10px', borderRadius:8,
                          background:'var(--c-bg2)', color:'var(--c-txt2)',
                          border:'1px solid var(--c-line2)', fontWeight:600, fontSize:11, cursor:'pointer',
                        }}>
                          更新
                        </button>
                        {mod.canDelete && (
                          <button style={{
                            padding:'6px 10px', borderRadius:8,
                            background:'var(--c-err-tint)', color:'var(--c-err)',
                            border:'none', fontWeight:600, fontSize:11, cursor:'pointer',
                          }}>
                            删除
                          </button>
                        )}
                      </div>
                    </div>
                    <div style={{ fontSize:12, color:'var(--c-txt3)', paddingTop:10, borderTop:'1px solid var(--c-line2)' }}>
                      点击下方按钮可以更新或卸载此模块
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

// ── Settings / Profile Screen ──────────────────────────────────
function ProfileScreen({ theme, setTheme }) {
  const [currentPlan, setCurrentPlan] = React.useState('free'); // 'free' or 'pro'
  const [showSubscription, setShowSubscription] = React.useState(false); // Show subscription page
  const plan = SUBSCRIPTION_PLANS.find(p => p.id === currentPlan);
  const [billingCycle, setBillingCycle] = React.useState('yearly');

  if (showSubscription) {
    // Subscription modal/page
    return (
      <div style={{ flex:1, display:'flex', flexDirection:'column', overflow:'hidden', background:'var(--c-bg)' }}>
        {/* Back header */}
        <div style={{ display:'flex', alignItems:'center', gap:12, padding:'14px 16px', borderBottom:'1px solid var(--c-line2)', background:'var(--c-surf)' }}>
          <button onClick={() => setShowSubscription(false)} style={{ background:'none', border:'none', cursor:'pointer', fontSize:20 }}>⬅</button>
          <div style={{ fontWeight:700, fontSize:15, color:'var(--c-txt1)' }}>订阅管理</div>
        </div>

        <div style={{ flex:1, overflowY:'auto', padding:'16px' }}>
          {/* Billing Cycle Toggle */}
          <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:20, justifyContent:'center' }}>
            <button onClick={() => setBillingCycle('yearly')} style={{
              padding:'8px 16px', borderRadius:12,
              background: billingCycle==='yearly' ? 'var(--c-prim)' : 'var(--c-surf)',
              color: billingCycle==='yearly' ? '#fff' : 'var(--c-txt2)',
              border: '1px solid var(--c-line2)', cursor:'pointer', fontWeight:600, fontSize:13,
              transition:'all 150ms',
            }}>
              按年计费 💰
            </button>
            <button onClick={() => setBillingCycle('monthly')} style={{
              padding:'8px 16px', borderRadius:12,
              background: billingCycle==='monthly' ? 'var(--c-prim)' : 'var(--c-surf)',
              color: billingCycle==='monthly' ? '#fff' : 'var(--c-txt2)',
              border: '1px solid var(--c-line2)', cursor:'pointer', fontWeight:600, fontSize:13,
              transition:'all 150ms',
            }}>
              按月计费 📅
            </button>
          </div>

          {/* Plan Cards */}
          <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
            {SUBSCRIPTION_PLANS.map(subplan => {
              const isCurrent = currentPlan === subplan.id;
              const displayPrice = billingCycle === 'yearly' ? subplan.price : subplan.priceMonth;
              const displayPeriod = billingCycle === 'yearly' ? subplan.period : subplan.periodMonth;
              
              return (
                <div key={subplan.id} style={{
                  background:'var(--c-surf)',
                  border: isCurrent ? `2px solid ${subplan.color}` : '1px solid var(--c-line2)',
                  borderRadius:16, padding:'16px',
                  boxShadow: isCurrent ? 'var(--sh2)' : 'var(--sh1)',
                  transition:'all 150ms',
                  position:'relative',
                }}>
                  {/* Current Badge */}
                  {isCurrent && (
                    <div style={{
                      position:'absolute', top:12, right:12,
                      padding:'4px 10px', borderRadius:9999,
                      background:subplan.color, color:'#fff',
                      fontSize:10, fontWeight:700,
                    }}>
                      当前套餐
                    </div>
                  )}

                  {/* Plan Header */}
                  <div style={{ marginBottom:14 }}>
                    <div style={{ display:'flex', alignItems:'baseline', gap:8, marginBottom:2 }}>
                      <div style={{ fontSize:18, fontWeight:800, color:'var(--c-txt1)' }}>{subplan.name}</div>
                      {subplan.badge && (
                        <span style={{ fontSize:10, padding:'2px 8px', borderRadius:6, background:subplan.color, color:'#fff', fontWeight:700 }}>
                          {subplan.badge}
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize:11, color:'var(--c-txt3)' }}>{subplan.description}</div>
                  </div>

                  {/* Pricing */}
                  <div style={{ marginBottom:14, paddingBottom:14, borderBottom:'1px solid var(--c-line2)' }}>
                    {subplan.price === 0 ? (
                      <div style={{ fontSize:20, fontWeight:800, color:subplan.color }}>永久免费</div>
                    ) : (
                      <div style={{ display:'flex', alignItems:'baseline', gap:2 }}>
                        <span style={{ fontSize:22, fontWeight:800, color:subplan.color }}>¥{displayPrice}</span>
                        <span style={{ fontSize:11, color:'var(--c-txt3)' }}>/{displayPeriod}</span>
                      </div>
                    )}
                  </div>

                  {/* Features */}
                  <div style={{ marginBottom:14 }}>
                    <div style={{ fontSize:12, fontWeight:700, color:'var(--c-txt2)', marginBottom:8 }}>功能包含：</div>
                    <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
                      {subplan.features.map((f, i) => (
                        <div key={i} style={{ display:'flex', alignItems:'flex-start', gap:8, fontSize:12, color:'var(--c-txt2)' }}>
                          <span style={{ color:subplan.color, fontWeight:800, marginTop:1 }}>✓</span>
                          <span>{f}</span>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* CTA Button */}
                  <button style={{
                    width:'100%', padding:'12px 16px', borderRadius:12,
                    background: isCurrent ? 'var(--c-bg2)' : subplan.color,
                    color: isCurrent ? 'var(--c-txt2)' : '#fff',
                    border:'none', cursor: isCurrent ? 'default' : 'pointer',
                    fontWeight:700, fontSize:14,
                    transition:'all 150ms',
                    opacity: isCurrent ? 0.6 : 1,
                  }}>
                    {isCurrent ? '当前订阅' : subplan.price === 0 ? '立即使用' : '立即升级'}
                  </button>
                </div>
              );
            })}
          </div>

          {/* FAQ Section */}
          <div style={{ marginTop:24, padding:'16px', background:'var(--c-bg2)', borderRadius:14 }}>
            <div style={{ fontSize:13, fontWeight:700, color:'var(--c-txt1)', marginBottom:10 }}>常见问题</div>
            <div style={{ fontSize:12, color:'var(--c-txt3)', lineHeight:1.6 }}>
              <p style={{ marginBottom:8 }}>• <strong>可以随时升级或降级吗？</strong> 可以。升级立即生效，降级在当前周期结束时生效。</p>
              <p style={{ marginBottom:8 }}>• <strong>有试用期吗？</strong> Pro 版提供 7 天免费试用，无需绑定信用卡。</p>
              <p>• <strong>支持哪些支付方式？</strong> 支持支付宝、微信支付和银行卡。</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Main Profile view
  return (
    <div style={{ flex:1, overflowY:'auto', background:'var(--c-bg)' }}>
      {/* Avatar header */}
      <div style={{ background:'var(--c-prim)', padding:'28px 20px 32px', color:'#fff', textAlign:'center' }}>
        <div style={{ width:72, height:72, borderRadius:'50%', background:'rgba(255,255,255,.2)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:32, margin:'0 auto 12px' }}>👤</div>
        <div style={{ fontWeight:800, fontSize:18 }}>RF 工程师</div>
        <div style={{ fontSize:12, opacity:.75, marginTop:4 }}>ProRF {plan?.name || '用户'}</div>
      </div>

      <div style={{ padding:'16px' }}>
        {/* Subscription Card */}
        <div style={{
          background: `linear-gradient(135deg, ${plan?.color || 'var(--c-prim)'} 0%, ${plan?.color || 'var(--c-prim)'}dd 100%)`,
          borderRadius:16, padding:'16px',
          color:'#fff', marginBottom:20,
          boxShadow:'var(--sh2)',
        }}>
          <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:12 }}>
            <div style={{ fontSize:18, fontWeight:800 }}>{plan?.name || '基础版'}</div>
            <button onClick={() => setShowSubscription(true)} style={{
              padding:'6px 14px', borderRadius:8,
              background:'rgba(255,255,255,.25)', border:'none',
              color:'#fff', fontWeight:700, fontSize:12, cursor:'pointer',
            }}>
              {currentPlan === 'free' ? '升级' : '管理'}
            </button>
          </div>
          <div style={{ fontSize:12, opacity:.9, marginBottom:8 }}>
            {currentPlan === 'free' 
              ? '开始使用 Pro 版，解锁所有功能。'
              : '感谢你的信任！您正在享受所有 Pro 功能。'
            }
          </div>
          <div style={{ fontSize:11, opacity:.8 }}>
            {currentPlan === 'free'
              ? '免费版永久可用'
              : '续费日期：2025-06-15'
            }
          </div>
        </div>

        {/* Current Plan Details */}
        {plan && (
          <div style={{ background:'var(--c-surf)', borderRadius:14, padding:'14px 16px', marginBottom:20 }}>
            <div style={{ fontSize:12, fontWeight:700, color:'var(--c-txt2)', marginBottom:10 }}>当前套餐包含：</div>
            <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
              {plan.features.slice(0, 4).map((f, i) => (
                <div key={i} style={{ display:'flex', alignItems:'center', gap:8, fontSize:11, color:'var(--c-txt2)' }}>
                  <span style={{ color:plan.color, fontWeight:800 }}>✓</span>
                  <span>{f}</span>
                </div>
              ))}
              {plan.features.length > 4 && (
                <div style={{ fontSize:11, color:'var(--c-txt3)', fontStyle:'italic' }}>
                  +{plan.features.length - 4} 项功能
                </div>
              )}
            </div>
          </div>
        )}

        {/* Theme */}
        <SLabel title="主题风格" />
        <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:10, marginBottom:20 }}>
          {[['light','☀️ 浅色'],['dark','🌙 深色'],['carbon','⚡ Carbon']].map(([id, lbl]) => (
            <button key={id} onClick={() => setTheme(id)} style={{
              padding:'10px 6px', borderRadius:12, border: theme===id ? '2px solid var(--c-prim)' : '1.5px solid var(--c-line2)',
              background: theme===id ? 'var(--c-prim-tint)' : 'var(--c-surf)',
              color: theme===id ? 'var(--c-prim)' : 'var(--c-txt2)', fontWeight:700, fontSize:12, cursor:'pointer',
            }}>{lbl}</button>
          ))}
        </div>

        {/* Settings rows */}
        <SLabel title="其他设置" />
        {[
          ['语言', '中文 / English', ''],
          ['计算单位', '国际单位 SI', ''],
          ['自动保存', '已开启', ''],
          ['云端同步', currentPlan === 'pro' ? '已连接' : '仅限 Pro 版', ''],
          ['导出格式', currentPlan === 'pro' ? 'PDF / Excel / SVG' : 'PNG 仅', ''],
          ['关于 ProRF', 'v2.3.0', ''],
        ].map(([l, v]) => (
          <div key={l} style={{ display:'flex', alignItems:'center', justifyContent:'space-between', padding:'14px 0', borderBottom:'1px solid var(--c-line2)' }}>
            <span style={{ fontSize:14, color:'var(--c-txt1)', fontWeight:500 }}>{l}</span>
            <div style={{ display:'flex', alignItems:'center', gap:6 }}>
              <span style={{ fontSize:13, color:'var(--c-txt3)' }}>{v}</span>
              {IC.chevR}
            </div>
          </div>
        ))}

        <div style={{ marginTop:24 }}>
          <PBtn wide tonal>退出登录</PBtn>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { HomeScreen, ResultsScreen, MarketScreen, ProfileScreen });
