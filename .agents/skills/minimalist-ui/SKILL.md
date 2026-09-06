---
name: minimalist-ui
description: Clean editorial interfaces. Warm monochrome, no gradients.
---

# Premium Utilitarian Minimalism UI

## 2. Banned Elements

- DO NOT use "Inter", "Roboto", or "Open Sans" typefaces
- DO NOT use generic thin-line icon libraries
- DO NOT use heavy drop shadows (`shadow-md`, `shadow-lg`)
- DO NOT use primary colored backgrounds for large elements
- DO NOT use gradients, neon colors, or 3D glassmorphism
- DO NOT use `rounded-full` for large containers
- DO NOT use emojis anywhere
- DO NOT use placeholder names ("John Doe", "Lorem Ipsum")
- DO NOT use AI clichés ("Elevate", "Seamless", "Unleash")

## 3. Typographic Architecture

- **Primary Sans:** `'SF Pro Display', 'Geist Sans', 'Helvetica Neue', sans-serif`
- **Editorial Serif (Hero only):** `'Lyon Text', 'Newsreader', 'Playfair Display', serif`
- **Monospace:** `'Geist Mono', 'SF Mono', 'JetBrains Mono', monospace`
- **Text Colors:** Off-black `#111111` or `#2F3437`, never `#000000`. Secondary: `#787774`
- **Line-height:** `1.6` for body text

## 4. Color Palette

- **Canvas:** Pure White `#FFFFFF` or Warm Bone `#F7F6F3` / `#FBFBFA`
- **Cards:** `#FFFFFF` or `#F9F9F8`
- **Borders:** `#EAEAEA` or `rgba(0,0,0,0.06)`
- **Pastel Accents (desaturated only):**
  - Pale Red: `#FDEBEC` (Text: `#9F2F2D`)
  - Pale Blue: `#E1F3FE` (Text: `#1F6C9F`)
  - Pale Green: `#EDF3EC` (Text: `#346538`)
  - Pale Yellow: `#FBF3DB` (Text: `#956400`)

## 5. Component Specifications

- **Cards:** `border: 1px solid #EAEAEA`, border-radius `8px` or `12px`, padding `24-40px`
- **Buttons:** Solid `#111111`, text `#FFFFFF`, radius `4-6px`, no shadow. Hover: `#333333` or `scale(0.98)`
- **Tags:** Pill-shaped, `text-xs`, uppercase, wide tracking, pastel backgrounds
- **Dividers:** `border-bottom: 1px solid #EAEAEA` only

## 7. Subtle Motion

- **Scroll Entry:** `translateY(12px)` + `opacity: 0` over `600ms`, `cubic-bezier(0.16, 1, 0.3, 1)`
- **Hover:** Ultra-subtle shadow shift `0 2px 8px rgba(0,0,0,0.04)` over `200ms`
- **Active:** `scale(0.98)`
- **Performance:** Animate ONLY `transform` and `opacity`

## 8. Execution Protocol

1. Establish macro-whitespace first (`py-24` or `py-32`)
2. Constrain typography to `max-w-4xl` or `max-w-5xl`
3. Apply custom typographic hierarchy and monochromatic colors
4. Every card/divider/border = `1px solid #EAEAEA`
5. Add scroll-entry animations to all major blocks