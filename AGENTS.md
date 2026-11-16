AGENTS POLICY

Rule: Never commit any changes.

- Automated agents MUST NOT commit changes directly to the repository.
- Agents should create patches, open pull requests, or raise issues for human reviewers to review and commit.
- Before making any persistent change, an agent must get explicit human approval.
- When uncertain, stop and ask a human.

This file documents the required safety policy for automation interacting with this repo.

### Naming Convention

- Kotlin source file names must match the name of the primary class declared in the file.

### Cost & Financial Safety

- **Zero/Low Cost Constraint**: Agents must prioritize keeping cloud infrastructure (Firebase, Firestore, Storage) completely free within free-tier limits.
- **Strict $1.00 Liability Cap**: Accidental mistakes or bugs must never exceed a maximum cost cap of $1.00. Agents must ensure network and storage operations are robust and free of infinite loops.

### Target Device Policy

- **Physical Devices Only**: The app is built exclusively for production release on physical Android devices targeting the Google Play Store. Agents must never design, test, or consider emulator/simulator behavior or emulator-specific workarounds.

### File Maintenance & Guidelines Limit

- The `AGENTS.md` file must never exceed 200 lines, and this rule must always be
  placed at the very end of the file (and must not be placed below line 200). If
  a new rule needs to be added, concise existing content or remove less critical
  rules to remain strictly under the 200 line limit.