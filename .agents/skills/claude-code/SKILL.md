---
name: claude-code
description: Coding conventions for Claude in VS Code.
---

# Claude Code — Development Conventions

## Environment
- VS Code with Claude plugin
- Terminal: bash (git-bash on Windows)
- Package manager: npm

## Code Generation Rules

1. **Read CLAUDE.md first** before any coding task
2. **One feature at a time** — implement, test, verify, then move on
3. **No extra dependencies** unless explicitly needed
4. **Tailwind className** for styling — no separate CSS files (except index.css)
5. **Functional components only** — no class components
6. **Chinese comments** for key logic
7. **Ask when uncertain** — don't guess requirements

## File Operations
- Create files with exact paths from the plan
- Run `npm run test` after each feature
- Run `npm run dev` to verify visual output

## Git Workflow
- Commit after each completed task
- Commit message format: `feat: description` or `fix: description`
- Never commit with failing tests

## Terminal Commands
```bash
npm run dev          # Dev server
npm run build        # Production build
npm run test         # Run tests
npm run test:watch   # Watch mode
```