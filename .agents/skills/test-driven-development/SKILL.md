---
name: test-driven-development
description: TDD: enforce RED-GREEN-REFACTOR, tests before code.
---

# Test-Driven Development (TDD)

## Core principle

**If you didn't watch the test fail, you don't know if it tests the right thing.**

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```

Write code before the test? Delete it. Start over.

## Red-Green-Refactor Cycle

### RED — Write Failing Test

Write one minimal test showing what should happen.

Requirements:
- One behavior per test
- Clear descriptive name ("and" in name? Split it)
- Real code, not mocks (unless truly unavoidable)
- Name describes behavior, not implementation

### Verify RED — Watch It Fail

**MANDATORY. Never skip.**

```bash
npm run test -- --testNamePattern="test_name"
```

Confirm:
- Test fails (not errors from typos)
- Failure message is expected
- Fails because the feature is missing

### GREEN — Minimal Code

Write the simplest code to pass the test. Nothing more.

Don't add features, refactor other code, or "improve" beyond the test.

**Cheating is OK in GREEN:**
- Hardcode return values
- Copy-paste
- Duplicate code
- Skip edge cases

We'll fix it in REFACTOR.

### Verify GREEN — Watch It Pass

**MANDATORY.**

```bash
npm run test -- --testNamePattern="test_name"
npm run test  # Full suite for regressions
```

### REFACTOR — Clean Up

After green only:
- Remove duplication
- Improve names
- Extract helpers
- Simplify expressions

Keep tests green throughout. Don't add behavior.

## Avoid Horizontal Slices

Do NOT write all tests first and then all implementation.

Use vertical tracer bullets instead:
```
RIGHT:
  RED→GREEN: test1→impl1
  RED→GREEN: test2→impl2
  RED→GREEN: test3→impl3
```

## Verification Checklist

- [ ] Every new function has a test
- [ ] Watched each test fail before implementing
- [ ] Each test failed for expected reason
- [ ] Wrote minimal code to pass each test
- [ ] All tests pass
- [ ] Output pristine (no errors, warnings)
- [ ] Edge cases and errors covered