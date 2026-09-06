# Antigravity Rules for DevFlow Project

This workspace follows the DevFlow workflow with native Subagent AI Lab arbitration.

1. **Subagents Available**:
   - `think_tank_room`: Analyst + Strategist + Adversary (red-team stress testing).
   - `dumb_tank_room`: First-principles control room (stripped brief, no project context).
   - `committee_room`: Non-arguing voter ballots.
   When the user asks for a proposal review or architectural evaluation, invoke these subagents concurrently to conduct a dual-blind review.

2. **Quality Standards**:
   - Use `grill-me` when clarifying ambiguous user requirements.
   - Use `test-driven-development` to write failing tests before implementing business logic.
   - Respect OCS limits (~24k chars) to avoid identity drift or context exhaustion.
