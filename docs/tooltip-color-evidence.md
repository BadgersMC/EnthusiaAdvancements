# SPEAR: advancement tooltip color parsing

Canonical BadgersMC/EnthusiaAdvancements main fetched at 2736261; clean new isolated worktree codex/advancement-tooltip-colors. Spec REQ-PILOT-HOVER-001. No project-local EARS/state helpers available; this record supplies task evidence.

Prove: real UAA 2.8.1 AdvancementDisplay getChatDescription includes raw section-code text. New parsed-display regression initially failed to compile without the adapter; now confirms one root component, exact title/description text, parsed pale title and grey RGB description, no raw codes, and independent return values. All 41 pilot tests pass.

Engine: ParsedAdvancementDisplay extends the existing UAA display and overrides chat title/description component getters. Parsed RGB components have one root as UAA requires. The native display strings and toast path remain unchanged. DTO constructor descriptors, namespace ownership, progress and reward behavior are unchanged. No provider API change or migration. Compiles against the actual recorded TEST companions (Paper 26.2 and UAA 2.8.1).

Refine: canonical pilot Maven verify succeeds with version 1.0.0-pilot.6-tooltip-colors-test.5. Existing colored-announcement, ownership, compatibility, Discord and recovery tests pass. Native client tooltips and integration acceptance pending TEST deployment, which was not requested in this turn. No production change.

Upstream implementation evidence: https://github.com/frengor/UltimateAdvancementAPI/blob/master/Common/src/main/java/com/fren_gor/ultimateAdvancementAPI/advancement/display/AdvancementDisplay.java constructs TextComponent from the unparsed strings; local dependency regression establishes the behavior without relying on latest upstream drift.
