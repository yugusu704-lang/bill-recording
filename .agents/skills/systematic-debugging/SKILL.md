---
name: systematic-debugging
description: 4-phase root cause debugging: understand bugs before fixing.
---

# Systematic Debugging

## Core principle

**ALWAYS find root cause before attempting fixes. Symptom fixes are failure.**

## The Iron Law

```
NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST
```

## The Four Phases

### Phase 1: Root Cause Investigation

**BEFORE attempting ANY fix:**

1. **Read Error Messages Carefully** — don't skip, read stack traces completely
2. **Build a Tight Feedback Loop** — one command that triggers the exact symptom
3. **Check Recent Changes** — `git log`, `git diff`, new deps, config changes
4. **Gather Evidence** — log what enters/exits each component boundary
5. **Trace Data Flow** — where does the bad value originate? Trace upstream

**STOP:** Do not proceed until you understand WHY it's happening.

### Phase 2: Pattern Analysis

1. **Minimize the Reproduction** — shrink to smallest scenario that still fails
2. **Find Working Examples** — locate similar working code
3. **Compare Against References** — read reference implementations completely
4. **Identify Differences** — list every difference, however small

### Phase 3: Hypothesis and Testing

1. **Form 3-5 Ranked Hypotheses** — state testable predictions
2. **Test Minimally** — one variable at a time
3. **Verify Before Continuing** — worked? → Phase 4. Didn't? → new hypothesis

### Phase 4: Implementation

1. **Create Failing Test Case** — reproduces the bug
2. **Implement Single Fix** — ONE change, no "while I'm here" improvements
3. **Verify Fix** — run test, run full suite
4. **Rule of Three** — if 3+ fixes failed, question the ARCHITECTURE, not the code

## Red Flags — STOP and Follow Process

- "Quick fix for now, investigate later"
- "Just try changing X and see if it works"
- "I don't fully understand but this might work"
- Proposing solutions before tracing data flow
- "One more fix attempt" (when already tried 2+)

**ALL of these mean: STOP. Return to Phase 1.**