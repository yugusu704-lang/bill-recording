---
name: sketch
description: Throwaway HTML mockups: 2-3 design variants to compare.
---

# Sketch

Use this skill when the user wants to **see a design direction before committing** to one — exploring a UI/UX idea as disposable HTML mockups. The point is to generate 2-3 interactive variants so the user can compare visual directions side-by-side, not to produce shippable code.

## Core method

```
intake  →  variants  →  head-to-head  →  pick winner (or iterate)
```

### 1. Intake

Before generating variants, get three things — one question at a time:

1. **Feel.** "What should this feel like? Adjectives, emotions, a vibe."
2. **References.** "What apps, sites, or products capture the feel you're imagining?"
3. **Core action.** "What's the single most important thing a user does on this screen?"

### 2. Variants (2-3, never 1, rarely 4+)

Produce **2-3 variants** in one go. Each variant is a complete, standalone HTML file. Each variant should take a **different design stance**, not different pixel values.

### 3. Make them real HTML

Each variant is a **single self-contained HTML file**:
- Inline `<style>` — no build step, no external CSS
- System fonts or one Google Font via `<link>`
- Tailwind via CDN is fine
- Realistic fake content — actual sentences, not "Lorem ipsum"
- **Interactive**: links clickable, hovers real, at least one state transition

### 4. Variant README

Each variant's `README.md` answers:
- Design stance
- Key choices (Layout, Typography, Color, Interaction)
- Trade-offs (Strong at, Weak at)
- Best for

### 5. Head-to-head

After all variants are built, present them as a comparison table. **Opinionate** — don't just list.

## Output

- Create `prototypes/` in the repo root
- One subdir per variant: `variant-a/`, `variant-b/`, `variant-c/`
- Tell the user how to open them