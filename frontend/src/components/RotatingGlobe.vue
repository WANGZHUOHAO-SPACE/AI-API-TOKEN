<template>
  <div class="globe-scene" aria-hidden="true">
    <div class="globe-halo" />
    <svg class="globe" viewBox="0 0 600 600" role="presentation">
      <defs>
        <radialGradient id="sphereFill" cx="38%" cy="30%" r="72%">
          <stop offset="0" stop-color="#8799ff" stop-opacity=".18" />
          <stop offset=".58" stop-color="#4f62db" stop-opacity=".08" />
          <stop offset="1" stop-color="#17203b" stop-opacity=".22" />
        </radialGradient>
        <linearGradient id="sphereEdge" x1="0" y1="0" x2="1" y2="1">
          <stop stop-color="#a9c6ff" stop-opacity=".72" />
          <stop offset=".45" stop-color="#786cff" stop-opacity=".28" />
          <stop offset="1" stop-color="#ef66d6" stop-opacity=".55" />
        </linearGradient>
        <linearGradient id="orbitStroke" x1="0" y1="0" x2="1" y2="0">
          <stop stop-color="#498fff" />
          <stop offset=".52" stop-color="#8768ff" />
          <stop offset="1" stop-color="#ec55cf" />
        </linearGradient>
        <pattern id="continentDots" width="12" height="12" patternUnits="userSpaceOnUse">
          <circle cx="3" cy="3" r="2.2" fill="currentColor" />
        </pattern>
        <clipPath id="sphereClip">
          <circle cx="300" cy="300" r="224" />
        </clipPath>
        <filter id="softGlow" x="-60%" y="-60%" width="220%" height="220%">
          <feGaussianBlur stdDeviation="5" result="blur" />
          <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
        </filter>
      </defs>

      <circle class="sphere-fill" cx="300" cy="300" r="224" fill="url(#sphereFill)" />
      <g class="grid-lines" clip-path="url(#sphereClip)">
        <ellipse cx="300" cy="300" rx="224" ry="82" />
        <ellipse cx="300" cy="300" rx="224" ry="145" />
        <ellipse cx="300" cy="300" rx="86" ry="224" />
        <ellipse cx="300" cy="300" rx="153" ry="224" />
        <path d="M76 300h448" />
      </g>

      <g clip-path="url(#sphereClip)" class="continent-window">
        <g class="continent-strip">
          <g class="continent-set">
            <path d="M76 198l35-43 62-20 48 14 26 37-18 31-35 9-9 33-28 19-34-13-14-33-33-12z" />
            <path d="M183 283l42-9 39 29 7 44-24 40-13 65-30 31-20-49 8-45-24-35z" />
            <path d="M290 167l42-31 79 3 46 31 62 16 15 36-39 25-52-3-26 31-18 55-42-9-18-42-42-17-22-46z" />
            <path d="M367 313l49 7 41 38-5 57-35 58-43-16-20-58-20-43z" />
            <path d="M474 404l43-20 42 19-12 36-48 12-30-21z" />
            <path d="M122 102l36-20 39 14-17 31-42 7z" />
          </g>
          <g class="continent-set continent-set--copy" transform="translate(520 0)">
            <path d="M76 198l35-43 62-20 48 14 26 37-18 31-35 9-9 33-28 19-34-13-14-33-33-12z" />
            <path d="M183 283l42-9 39 29 7 44-24 40-13 65-30 31-20-49 8-45-24-35z" />
            <path d="M290 167l42-31 79 3 46 31 62 16 15 36-39 25-52-3-26 31-18 55-42-9-18-42-42-17-22-46z" />
            <path d="M367 313l49 7 41 38-5 57-35 58-43-16-20-58-20-43z" />
            <path d="M474 404l43-20 42 19-12 36-48 12-30-21z" />
            <path d="M122 102l36-20 39 14-17 31-42 7z" />
          </g>
        </g>
      </g>

      <circle class="sphere-edge" cx="300" cy="300" r="224" fill="none" stroke="url(#sphereEdge)" />

      <g class="orbit orbit--one">
        <ellipse cx="300" cy="300" rx="272" ry="102" transform="rotate(-17 300 300)" />
        <circle cx="45" cy="235" r="7" />
        <circle cx="548" cy="357" r="6" />
      </g>
      <g class="orbit orbit--two">
        <ellipse cx="300" cy="300" rx="255" ry="132" transform="rotate(48 300 300)" />
        <circle cx="192" cy="68" r="6" />
        <circle cx="423" cy="519" r="7" />
      </g>
      <g class="orbit orbit--three">
        <ellipse cx="300" cy="300" rx="246" ry="78" transform="rotate(19 300 300)" />
        <circle cx="78" cy="262" r="5" />
      </g>
    </svg>
  </div>
</template>

<style scoped>
.globe-scene { position: absolute; inset: 0; overflow: hidden; color: rgba(202,215,255,.76); pointer-events: none; }
.globe { position: absolute; top: 50%; left: 50%; width: min(76vw, 850px); max-width: none; transform: translate(-50%, -50%); overflow: visible; filter: drop-shadow(0 24px 70px rgba(54,70,190,.28)); }
.globe-halo { position: absolute; top: 50%; left: 50%; width: min(64vw, 720px); aspect-ratio: 1; border-radius: 50%; background: radial-gradient(circle, rgba(87,104,255,.2), rgba(85,61,180,.08) 48%, transparent 72%); transform: translate(-50%, -50%); animation: halo-pulse 5s ease-in-out infinite; }
.sphere-fill { filter: url(#softGlow); }
.sphere-edge { stroke-width: 2.2; opacity: .78; }
.grid-lines { fill: none; stroke: rgba(145,166,255,.17); stroke-width: 1; }
.continent-window { color: rgba(225,232,255,.55); }
.continent-set path { fill: url(#continentDots); }
.continent-strip { animation: continent-rotate 24s linear infinite; }
.orbit { fill: none; stroke: url(#orbitStroke); stroke-width: 1.6; transform-origin: 300px 300px; filter: url(#softGlow); }
.orbit circle { fill: #58a0ff; stroke: none; }
.orbit--one { animation: orbit-spin 16s linear infinite; }
.orbit--two { animation: orbit-spin-reverse 21s linear infinite; opacity: .82; }
.orbit--three { animation: orbit-spin 27s linear infinite; opacity: .62; }
@keyframes continent-rotate { from { transform: translateX(0); } to { transform: translateX(-520px); } }
@keyframes orbit-spin { to { transform: rotate(360deg); } }
@keyframes orbit-spin-reverse { to { transform: rotate(-360deg); } }
@keyframes halo-pulse { 0%, 100% { opacity: .65; transform: translate(-50%, -50%) scale(.96); } 50% { opacity: 1; transform: translate(-50%, -50%) scale(1.04); } }
@media (prefers-reduced-motion: reduce) {
  .continent-strip, .orbit, .globe-halo { animation: none; }
}
</style>
