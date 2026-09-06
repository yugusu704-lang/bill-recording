---
name: plan
description: Plan mode: write an actionable markdown plan, no execution.
---

# Plan Mode

Use this skill when the user wants a plan instead of execution.

## Core behavior

For this turn, you are planning only.

- Do not implement code.
- Do not edit project files except the plan markdown file.
- Do not run mutating terminal commands, commit, push, or perform external actions.
- You may inspect the repo or other context with read-only commands/tools when needed.
- Your deliverable is a markdown plan saved inside the active workspace under `docs/`.

## Output requirements

Write a markdown plan that is concrete and actionable.

Include, when relevant:
- Goal
- Current context / assumptions
- Proposed approach
- Step-by-step plan
- Files likely to change
- Tests / validation
- Risks, tradeoffs, and open questions

## Bite-Sized Task Granularity

**Each task = 2-5 minutes of focused work.**

Every step is one action:
- "Write the failing test" — step
- "Run it to make sure it fails" — step
- "Implement the minimal code to make the test pass" — step
- "Run the tests and make sure they pass" — step
- "Commit" — step

## Plan Document Structure

### Header (Required)

```markdown
# [Feature Name] Implementation Plan

**Goal:** [One sentence describing what this builds]
**Architecture:** [2-3 sentences about approach]
**Tech Stack:** [Key technologies/libraries]
---
```

### Task Structure

Each task follows this format:

```markdown
### Task N: [Descriptive Name]

**Objective:** What this task accomplishes (one sentence)

**Files:**
- Create: `exact/path/to/new_file.py`
- Modify: `exact/path/to/existing.py`
- Test: `tests/path/to/test_file.py`

**Steps:**
1. Write failing test
2. Run test to verify failure
3. Write minimal implementation
4. Run test to verify pass
5. Commit
```

## Principles

### DRY (Don't Repeat Yourself)
Extract shared logic, don't copy-paste.

### YAGNI (You Aren't Gonna Need It)
Implement only what's needed now. No premature abstractions.

### TDD (Test-Driven Development)
Every task that produces code should include the full TDD cycle.

### Frequent Commits
Commit after every task.