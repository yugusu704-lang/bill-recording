---
name: design-taste-frontend
description: Anti-slop frontend design. Audit-first, real design systems.
---

# Anti-Slop Frontend Skill

> Every rule below is **contextual**. None of it fires automatically. First read the brief, then pull only what fits.

## 0. BRIEF INFERENCE (Read the Room Before Anything Else)

### 0.A Read these signals first
1. **Page kind** — landing, portfolio, redesign, editorial
2. **Vibe words** — "minimalist", "calm", "Linear-style", "brutalist", "premium"
3. **Reference signals** — URLs, screenshots, products named
4. **Audience** — who will use this
5. **Brand assets** — logo, color, type that already exist

### 0.B Output a one-line "Design Read" before generating
Example: *"Reading this as: to-do app for personal use, with a clean minimal language, leaning toward warm monochrome."*

## 1. THE THREE DIALS

* **DESIGN_VARIANCE: 8** — 1 = Perfect Symmetry, 10 = Artsy Chaos
* **MOTION_INTENSITY: 6** — 1 = Static, 10 = Cinematic
* **VISUAL_DENSITY: 4** — 1 = Art Gallery, 10 = Cockpit

## 4. DESIGN ENGINEERING DIRECTIVES

### 4.1 Typography
- **Display:** `text-4xl md:text-6xl tracking-tighter leading-none`
- **Body:** `text-base text-gray-600 leading-relaxed max-w-[65ch]`
- **AVOID Inter as default.** Pick Geist, Outfit, Cabinet Grotesk, Satoshi first.
- **Serif VERY DISCOURAGED as default.** Only for genuinely editorial/luxury briefs.

### 4.2 Color Calibration
- Max 1 accent color. Saturation < 80%.
- **NO AI Purple / Blue glow** as default.
- One palette per project. Don't fluctuate between warm and cool grays.

### 4.3 Layout Diversification
- **ANTI-CENTER BIAS:** Force split screen, left-aligned, or asymmetric layouts when variance > 4.

### 4.4 Materiality
- Use cards ONLY when elevation communicates real hierarchy.
- Tint shadows to background hue. No pure-black drop shadows.

### 4.5 Interactive UI States
Always implement full cycles:
- **Loading:** Skeletal loaders, not generic spinners
- **Empty States:** Beautifully composed
- **Error States:** Clear, inline
- **Tactile Feedback:** `-translate-y-[1px]` or `scale-[0.98]` on `:active`

## 9. AI TELLS (Forbidden Patterns)

- NO neon/outer glows
- NO pure black (#000000)
- NO oversaturated accents
- NO generic names ("John Doe", "Acme Corp")
- NO AI clichés ("Elevate", "Seamless", "Unleash")
- NO em-dash (—) anywhere
- NO div-based fake screenshots
- NO generic three-equal feature cards

## 14. PRE-FLIGHT CHECK

Run before outputting code:
- [ ] Brief inference declared?
- [ ] ZERO em-dashes anywhere?
- [ ] One accent color across all sections?
- [ ] One corner-radius system?
- [ ] All CTAs readable (WCAG AA)?
- [ ] Hero fits viewport?
- [ ] No AI Tells from Section 9?