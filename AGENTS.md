# DevFlow Agent Governance & Subagent Architecture

This workspace is managed under the **DevFlow Rigorous Development Workflow**.
All agents interacting in this workspace MUST adhere to the following principles:

## 1. Native Subagent Three-Room Dual-Blind Review
For critical architectural decisions, tech stack migrations, or major refactors, the lead agent MUST initiate an **AI Lab Three-Room Review** using the registered subagents in `.agents/subagents/subagents.json`:
- 🟢 `think_tank_room`: In-depth reasoning with an active **Adversary (Red Team)** hunting for worst-case edge cases and failure modes.
- 🟣 `dumb_tank_room`: Control room receiving **ONLY** mechanically stripped technical constraints (no repository files, no rhetoric).
- 🔵 `committee_room`: Non-arguing voter ballots evaluating constraints and exact file quotes.

**Rules**:
1. **Sealed Isolation**: The three subagents must be invoked concurrently via `invoke_subagent` and must not cross-pollinate during deliberation.
2. **Prose Effect**: If `dumb_tank_room` and `think_tank_room` diverge meaningfully, the lead agent must flag **Framing Bias** and halt or iterate the design.

## 2. Evidence Over Opinions
- Never claim a behavior or bug without quoting the exact file path and line number (`Librarian` rule).
- Speculation is strictly forbidden during architectural debate.

## 3. Execution Topologies
- **Exploratory Tasks / Unknown bugs**: Use the `slime` topology (flood paths, benchmark score, prune unviable ones).
- **Feature Development**: Use the `octopus` topology (Head defines contracts; Arms implement isolated tasks; Head merges).
- **Code Review & Release**: Use the `bee` topology (independent scouts for security, performance, and contract verification).

## 4. Operational Core State (OCS) & Memory
- Always preserve active identity, constraints, and tasks under the ~24,400 character hard limit.
- Before committing any ADR, verify write-side findability (FOUND? + WINS?).
