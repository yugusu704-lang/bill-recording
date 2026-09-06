---
name: requesting-code-review
description: Pre-commit review: security scan, quality gates, auto-fix.
---

# Pre-Commit Code Verification

**Core principle:** No agent should verify its own work. Fresh context finds what you miss.

## Step 1 — Get the diff

```bash
git diff --cached
```

## Step 2 — Static security scan

Scan added lines for:
- Hardcoded secrets (api_key, secret, password, token)
- Shell injection (os.system, subprocess shell=True)
- Dangerous eval/exec
- SQL injection (string formatting in queries)

## Step 3 — Tests and linting

```bash
npm run test
```

Only NEW failures introduced by your changes block the commit.

## Step 4 — Self-review checklist

- [ ] No hardcoded secrets, API keys, or credentials
- [ ] Input validation on user-provided data
- [ ] External calls have error handling (try/catch)
- [ ] No debug console.log left behind
- [ ] No commented-out code
- [ ] New code has tests

## Step 5 — Independent review

Have Claude review the diff with fresh context:
- Security concerns → auto-fail
- Logic errors → auto-fail
- Suggestions → non-blocking

## Step 6 — Evaluate results

All passed → commit. Any failures → fix and reverify.

## Step 7 — Auto-fix loop

Maximum 2 fix-and-reverify cycles. If still failing after 2, escalate.

## Step 8 — Commit

```bash
git add -A && git commit -m "feat: description"
```