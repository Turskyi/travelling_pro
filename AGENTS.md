AGENTS POLICY

Rule: Never commit any changes.

- Automated agents MUST NOT commit changes directly to the repository.
- Agents should create patches, open pull requests, or raise issues for human reviewers to review and commit.
- Before making any persistent change, an agent must get explicit human approval.
- When uncertain, stop and ask a human.

This file documents the required safety policy for automation interacting with this repo.

### File Maintenance & Guidelines Limit

- The `AGENTS.md` file must never exceed 200 lines, and this rule must always be
  placed at the very end of the file (and must not be placed below line 200). If
  a new rule needs to be added, concise existing content or remove less critical
  rules to remain strictly under the 200 line limit.

### Naming Convention

- Kotlin source file names must match the name of the primary class declared in the file.
