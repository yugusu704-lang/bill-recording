---
name: external-skill-installation
description: Manual install + verify workflow for dependencies.
---

# Manual Install & Verify Workflow

**Core principle:** Install each dependency and immediately verify it works. Never batch-install everything and test at the end.

## When to Use

- Installing Capacitor or other build tools
- Adding new npm packages
- Configuring Android Studio / SDK
- Any setup with multiple sequential steps

## The Method

For EACH step:

1. **Install** — run the install command
2. **Verify** — check that the expected output exists
3. **Commit** — if in a git repo, commit the working state
4. **Proceed** — only move to next step after verification passes

## Verification Examples

```bash
# After npm install
cat package.json | grep "package-name"  # Confirm dependency exists

# After cap init
ls capacitor.config.ts  # Confirm config file generated

# After cap add android
ls android/  # Confirm android directory exists

# After cap sync
ls android/app/src/main/assets/public/  # Confirm web assets synced
```

## When Things Fail

- Don't guess. Read the error message.
- Search the specific error online.
- Try the manual approach (clone/configure by hand) if CLI tools fail.
- Document the workaround for future reference.