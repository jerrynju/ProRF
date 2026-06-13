// prf-data.jsx — RF Engine, Module Catalog, Sample Data

// ── RF Calculation Engine ──────────────────────────────────────
const RFE = {
  fspl(freqMHz, distKm) {
    if (distKm <= 0 || freqMHz <= 0) return 0;
    return 32.44 + 20 * Math.log10(Math.max(freqMHz, 1)) + 20 * Math.log10(Math.max(distKm, 0.001));
  },
  noiseFloor(bwMHz, nfDb = 0) {
    return -174 + 10 * Math.log10(Math.max(bwMHz, 0.001) * 1e6) + nfDb;
  },
  evaluate(nodes, globals) {
    const { frequencyMHz = 14000, bandwidthMHz = 36, distanceKm = 38000 } = globals;
    let pwr = 0;
    const trace = [];
    nodes.forEach((nd) => {
      let gain = 0, loss = 0, lbl = '';
      switch (nd.moduleId) {
        case 'tx_source': pwr = nd.params.powerDbm ?? 0; lbl = `${pwr.toFixed(1)} dBm`; break;
        case 'pa': gain = nd.params.gainDb ?? 30; pwr += gain; lbl = `+${gain.toFixed(1)} dB`; break;
        case 'cable': case 'attenuator': loss = nd.params.lossDb ?? 2; pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; break;
        case 'filter': loss = nd.params.lossDb ?? 1; pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; break;
        case 'ant_tx': gain = nd.params.gainDbi ?? 45; pwr += gain; lbl = `+${gain.toFixed(0)} dBi`; break;
        case 'fspl': loss = RFE.fspl(frequencyMHz, distanceKm); pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; nd._fspl = loss; break;
        case 'rain': loss = nd.params.lossDb ?? 2; pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; break;
        case 'atmospheric': loss = nd.params.lossDb ?? 1; pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; break;
        case 'ant_rx': gain = nd.params.gainDbi ?? 45; pwr += gain; lbl = `+${gain.toFixed(0)} dBi`; break;
        case 'lna': gain = nd.params.gainDb ?? 20; pwr += gain; lbl = `+${gain.toFixed(1)} dB`; break;
        case 'mixer': loss = nd.params.lossDb ?? 6; pwr -= loss; lbl = `−${loss.toFixed(1)} dB`; break;
        case 'receiver': {
          const nf = nd.params.nfDb ?? 3;
          nd._rxPwr = pwr; nd._nf = RFE.noiseFloor(bandwidthMHz, nf);
          nd._margin = pwr - nd._nf; lbl = `${pwr.toFixed(1)} dBm`; break;
        }
        default: break;
      }
      trace.push({ nodeId: nd.id, name: nd.name, kind: nd.kind, gain, loss, pwr, lbl });
    });
    const rx = nodes.find(n => n._rxPwr != null);
    const rxPwr = rx?._rxPwr ?? pwr, nf = rx?._nf ?? -100, margin = rx?._margin ?? 0;
    // Find EIRP (power after last TX node)
    let eirp = 0;
    for (let i = trace.length - 1; i >= 0; i--) {
      if (['TX'].includes(nodes[i]?.kind)) { eirp = trace[i].pwr; break; }
    }
    // Chart: SNR vs distance
    const bwNf = rx ? (rx.params?.nfDb ?? 3) : 3;
    const nfFloor = RFE.noiseFloor(bandwidthMHz, bwNf);
    const totalGain = trace.reduce((s, t) => s + t.gain, 0);
    const totalFixedLoss = trace.filter(t => t.nodeId !== nodes.find(n => n.moduleId === 'fspl')?.id).reduce((s, t) => s + t.loss, 0);
    const chartData = Array.from({ length: 32 }, (_, i) => {
      const dMin = Math.log10(0.01), dMax = Math.log10(distanceKm * 8);
      const d = Math.pow(10, dMin + (dMax - dMin) * i / 31);
      const fsplD = RFE.fspl(frequencyMHz, d);
      const rxP = eirp - totalFixedLoss - fsplD + (rx ? (rx.params?.gainDbi ?? 0) : 0);
      return { d, snr: rxP - nfFloor, margin: rxP - nf };
    });
    return { eirp, rxPwr, nf, margin, snr: rxPwr - nfFloor, isValid: margin > 0, trace, chartData, nfFloor };
  }
};

// ── Module Catalog ─────────────────────────────────────────────
const MODS = {
  // TX
  tx_source:   { id:'tx_source',  kind:'TX',          name:'信号源',    en:'Signal Source',     emoji:'〇', params:[{k:'powerDbm',   l:'输出功率',   u:'dBm', d:0,   r:[-40,60]}] },
  pa:          { id:'pa',         kind:'TX',          name:'功率放大器', en:'Power Amplifier',   emoji:'⚡', params:[{k:'gainDb',     l:'增益',       u:'dB',  d:30,  r:[0,60]},{k:'p1dbDbm',l:'P1dB',u:'dBm',d:33,r:[10,50]}] },
  ant_tx:      { id:'ant_tx',     kind:'TX',          name:'天线(发射)', en:'TX Antenna',        emoji:'△', params:[{k:'gainDbi',    l:'增益',       u:'dBi', d:45,  r:[0,65]}] },
  // LOSS
  cable:       { id:'cable',      kind:'LOSS',        name:'电缆/波导',  en:'Cable',             emoji:'―', params:[{k:'lossDb',     l:'损耗',       u:'dB',  d:2,   r:[0,20]},{k:'lengthM',l:'长度',u:'m',d:10,r:[0,1000]}] },
  filter:      { id:'filter',     kind:'LOSS',        name:'滤波器',     en:'Filter',            emoji:'◫', params:[{k:'lossDb',     l:'插损',       u:'dB',  d:1,   r:[0,10]},{k:'bwMHz',l:'带宽',u:'MHz',d:100,r:[0.1,1000]}] },
  attenuator:  { id:'attenuator', kind:'LOSS',        name:'衰减器',     en:'Attenuator',        emoji:'▽', params:[{k:'lossDb',     l:'衰减量',     u:'dB',  d:6,   r:[0,60]}] },
  // PROPAGATION
  fspl:        { id:'fspl',       kind:'PROPAGATION', name:'自由空间损耗',en:'Free Space Loss',  emoji:'○', params:[] },
  rain:        { id:'rain',       kind:'PROPAGATION', name:'雨衰',       en:'Rain Attenuation',  emoji:'◎', params:[{k:'lossDb',     l:'雨衰量',     u:'dB',  d:2,   r:[0,30]}] },
  atmospheric: { id:'atmospheric',kind:'PROPAGATION', name:'大气吸收',   en:'Atmospheric Loss',  emoji:'◑', params:[{k:'lossDb',     l:'损耗量',     u:'dB',  d:0.5, r:[0,10]}] },
  // RX
  ant_rx:      { id:'ant_rx',     kind:'RX',          name:'天线(接收)', en:'RX Antenna',        emoji:'▽', params:[{k:'gainDbi',    l:'增益',       u:'dBi', d:45,  r:[0,65]}] },
  lna:         { id:'lna',        kind:'RX',          name:'低噪放LNA',  en:'LNA',               emoji:'◎', params:[{k:'gainDb',     l:'增益',       u:'dB',  d:20,  r:[5,40]},{k:'nfDb',l:'噪声系数',u:'dB',d:1.5,r:[0.5,10]},{k:'oip3',l:'OIP3',u:'dBm',d:30,r:[5,50]}] },
  mixer:       { id:'mixer',      kind:'RX',          name:'混频器',     en:'Mixer',             emoji:'⊕', params:[{k:'lossDb',     l:'转换损耗',   u:'dB',  d:6,   r:[3,15]},{k:'nfDb',l:'噪声系数',u:'dB',d:8,r:[1,20]}] },
  receiver:    { id:'receiver',   kind:'RX',          name:'接收机',     en:'Receiver',          emoji:'◉', params:[{k:'nfDb',       l:'系统NF',     u:'dB',  d:3,   r:[0.5,15]},{k:'bwMHz',l:'带宽',u:'MHz',d:36,r:[0.1,1000]}] },
};

const MCAT = [
  { id:'all',   label:'全部' },
  { id:'TX',    label:'发射端' },
  { id:'LOSS',  label:'传输损耗' },
  { id:'PROPAGATION', label:'传播介质' },
  { id:'RX',    label:'接收端' },
];

const KIND_META = {
  TX:          { col:'var(--tx)',   bg:'var(--tx-t)',   border:'var(--tx-b)',   label:'发射' },
  LOSS:        { col:'var(--loss)', bg:'var(--loss-t)', border:'var(--loss-b)', label:'损耗' },
  PROPAGATION: { col:'var(--prop)', bg:'var(--prop-t)', border:'var(--prop-b)', label:'传播' },
  RX:          { col:'var(--rx)',   bg:'var(--rx-t)',   border:'var(--rx-b)',   label:'接收' },
  ANALYSIS:    { col:'var(--ana)',  bg:'var(--ana-t)',  border:'var(--ana-b)',  label:'分析' },
};

// ── Sample Workflows ───────────────────────────────────────────
const WORKFLOWS = [
  {
    id:'wf-sat', name:'卫星通信下行链路示例', tags:['卫星','Ku频段'], fav:false, updatedAt:'2024-05-20 14:30',
    globals:{ frequencyMHz:14000, bandwidthMHz:36, distanceKm:38000, temperatureK:290 },
    nodes:[
      { id:'s1', kind:'TX',          moduleId:'pa',       name:'发射机',    params:{ gainDb:43, p1dbDbm:45 } },
      { id:'s2', kind:'TX',          moduleId:'ant_tx',   name:'天线(发射)',params:{ gainDbi:45 } },
      { id:'s3', kind:'PROPAGATION', moduleId:'fspl',     name:'自由空间',  params:{} },
      { id:'s4', kind:'RX',          moduleId:'ant_rx',   name:'天线(接收)',params:{ gainDbi:45 } },
      { id:'s5', kind:'RX',          moduleId:'receiver', name:'接收机',    params:{ nfDb:3, bwMHz:36 } },
    ]
  },
  {
    id:'wf-5g', name:'5G宏站基站链路', tags:['5G','3.5GHz'], fav:true, updatedAt:'2024-05-18 09:45',
    globals:{ frequencyMHz:3500, bandwidthMHz:100, distanceKm:0.5, temperatureK:290 },
    nodes:[
      { id:'g1', kind:'TX',          moduleId:'tx_source',name:'信号源',    params:{ powerDbm:20 } },
      { id:'g2', kind:'TX',          moduleId:'pa',       name:'PA放大器',  params:{ gainDb:28, p1dbDbm:33 } },
      { id:'g3', kind:'TX',          moduleId:'ant_tx',   name:'阵列天线',  params:{ gainDbi:20 } },
      { id:'g4', kind:'PROPAGATION', moduleId:'fspl',     name:'自由空间',  params:{} },
      { id:'g5', kind:'RX',          moduleId:'ant_rx',   name:'手机天线',  params:{ gainDbi:2 } },
      { id:'g6', kind:'RX',          moduleId:'lna',      name:'低噪放',    params:{ gainDb:15, nfDb:2, oip3:25 } },
      { id:'g7', kind:'RX',          moduleId:'receiver', name:'接收机',    params:{ nfDb:5, bwMHz:100 } },
    ]
  },
  {
    id:'wf-mw', name:'微波回传链路', tags:['微波','18GHz'], fav:false, updatedAt:'2024-05-15 16:22',
    globals:{ frequencyMHz:18000, bandwidthMHz:28, distanceKm:20, temperatureK:290 },
    nodes:[
      { id:'m1', kind:'TX',          moduleId:'pa',       name:'发射机',    params:{ gainDb:25, p1dbDbm:28 } },
      { id:'m2', kind:'LOSS',        moduleId:'cable',    name:'馈线损耗',  params:{ lossDb:2, lengthM:15 } },
      { id:'m3', kind:'TX',          moduleId:'ant_tx',   name:'抛物面天线',params:{ gainDbi:38 } },
      { id:'m4', kind:'PROPAGATION', moduleId:'fspl',     name:'自由空间',  params:{} },
      { id:'m5', kind:'RX',          moduleId:'ant_rx',   name:'抛物面天线',params:{ gainDbi:38 } },
      { id:'m6', kind:'RX',          moduleId:'receiver', name:'接收机',    params:{ nfDb:4, bwMHz:28 } },
    ]
  },
];

// ── Subscription Plans ────────────────────────────────────────
const SUBSCRIPTION_PLANS = [
  {
    id: 'free',
    name: '基础版',
    price: 0,
    period: '永久免费',
    description: '适合个人学习和基础应用',
    features: [
      '完整的RF计算引擎',
      '最多保存5个链路',
      '基础模块库（发射、接收、损耗）',
      '结果导出（PNG）',
      '社区模板访问',
    ],
    limitations: [
      '模板市场只读',
      '无高级分析工具',
      '无云端同步',
    ],
    badge: '免费',
    color: 'var(--c-ok)',
  },
  {
    id: 'pro',
    name: 'Pro版',
    price: 99,
    period: '年订阅',
    priceMonth: 9.9,
    periodMonth: '月订阅',
    description: '为专业工程师和团队定制',
    features: [
      '完整的RF计算引擎',
      '无限保存链路',
      '完整模块库（所有模块）',
      '高级分析工具（Sankey、参数扫描）',
      '结果导出（PDF、Excel、SVG）',
      '社区模板分享和销售',
      '云端同步和版本管理',
      '优先技术支持',
      '自定义企业品牌',
    ],
    limitations: [],
    badge: '推荐',
    color: 'var(--c-prim)',
  },
];

// ── Module Marketplace ────────────────────────────────────────
const MODULES_MARKET = [
  { id:'mi1', name:'卫星通信下行链路',  cat:'卫星通信', price:29.90, rating:4.9, dl:3100, badge:'专业版', author:'RF Lab', desc:'完整的卫星通信下行链路模板' },
  { id:'mi2', name:'微波回传链路模板',  cat:'微波链路', price:19.90, rating:4.8, dl:1890, badge:'推荐', author:'通信组', desc:'5G微波回传链路' },
  { id:'mi3', name:'5G基站到核心网链路',cat:'5G/6G',   price:24.90, rating:4.7, dl:2640, badge:'热门', author:'5G Team', desc:'端到端链路分析' },
  { id:'mi4', name:'深空通信链路',      cat:'卫星通信', price:49.90, rating:4.9, dl:740,  badge:'专业版', author:'Space Division', desc:'深空探测通信' },
  { id:'mi5', name:'LTE小区覆盖分析',  cat:'移动通信', price:0,     rating:4.5, dl:8200, badge:'免费', author:'官方', desc:'4G覆盖规划工具' },
  { id:'mi6', name:'雷达系统链路',      cat:'雷达',     price:39.90, rating:4.6, dl:980,  badge:'专业版', author:'雷达组', desc:'军用雷达系统设计' },
];

// ── User's Owned Modules ───────────────────────────────────────
const MY_MODULES = [
  { id:'mm1', moduleId:'mi5', name:'LTE小区覆盖分析', installedAt:'2024-05-10', version:'1.2.1', size:'2.3 MB', canDelete:true },
  { id:'mm2', moduleId:'mi2', name:'微波回传链路模板', installedAt:'2024-06-01', version:'2.0.0', size:'4.1 MB', canDelete:true },
];

Object.assign(window, { RFE, MODS, MCAT, KIND_META, WORKFLOWS, MODULES_MARKET, MY_MODULES, SUBSCRIPTION_PLANS });
