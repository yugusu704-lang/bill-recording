---
name: ai-lab-three-rooms
description: Run the rigorous AI Lab Three-Room dual-blind review on architectural proposals or technical designs. Defines and invokes think_tank_room, dumb_tank_room, and committee_room subagents, measures Prose Effect, and converges on consensus. Use whenever evaluating architectural changes, tech stack selection, or breaking changes.
---

# AI Lab Three-Room Dual-Blind Review Skill

Use this skill whenever evaluating an architectural decision, technical redesign, stack selection, or major PR.
It coordinates three concurrent, dual-blind subagents to eliminate cognitive and prompt-framing bias.

## Core Process

### Step 1: Subagent Roster Verification
Verify or register the three rooms using `define_subagent` if not already registered:
- `think_tank_room`: Analyst, Strategist, and Adversary (who relentlessly challenges assumptions).
- `dumb_tank_room`: Control Room receiving ONLY stripped brief, NO project files, NO rhetoric.
- `committee_room`: Non-arguing evaluation and voting (Yay/Nay/Abstain) with factual evidence.

### Step 2: Mechanical Prose Stripping & Librarian Evidence
1. Mechanically strip emotional rhetoric, buzzwords, and persuasion from the user's proposal (preserve only facts, constraints, and scope).
2. Gather exact file quotes (line numbers and code snippets) from the workspace using `grep_search` or `view_file` to support the brief.

### Step 3: Sealed Parallel Subagent Invocation
Call `invoke_subagent` in a single tool call with all three rooms concurrently:
- **Think Tank**: Receives full brief, project context, and evidence. Must produce First Position and Adversarial Concerns.
- **Dumb Tank**: Receives ONLY the stripped, neutral brief. Must evaluate from first principles without repository context.
- **Committee**: Receives the brief and exact quotes. Votes Yay/Nay/Abstain without debate.

### Step 4: Consensus Gate & Prose Effect Calculation
When subagents return:
1. **Compare Think Tank vs Dumb Tank**:
   - If their conclusions align -> `Prose Effect = SAME` (Robust, objective design).
   - If their conclusions diverge -> `Prose Effect = DIFFERENT` (Warning: Framing bias detected; proposal relies on persuasion).
2. **Stopping Rule**:
   - 3/3 Supermajority -> Unanimous approval to proceed.
   - 2/3 Majority -> Approved with safeguard monitoring.
   - Not Met -> Reject or trigger redesign.

### Step 5: Deliver Final Consensus Package
Report the verdict, Prose Effect, and specific adversarial points clearly to the user.
