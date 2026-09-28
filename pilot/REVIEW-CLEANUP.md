# Projection API review cleanup

The renderer requires Java 25 for its pinned Paper 26.2 API. Version pilot.4 introduces owner-bound project/celebrate methods; update companion providers together. Deprecated ownerless methods fail closed rather than silently bypassing ownership. No server deployment is performed by this PR.

The entire progress map is snapshotted and null-checked before root grants, tab display, or node mutation. Existing unavailable/out-of-range sentinel handling is retained. Tests exercise wrong-owner rejection for projection and celebrations and no display mutation after a malformed map. Root and child automatic notification flags are independently checked.

Validation: renderer-boundary-red.log reproduced all four newly asserted API cases on the old implementation. Java 25 clean install now passes 7 tests, zero failures/errors/skips. Hosted verification is defined in .github/workflows/pilot-verify.yml and checks the exact head. Hosted success is not inferred from local success.

No reward execution, changes to player data, auto-merge, release, or custom-icon troubleshooting.

## September 26 CodeRabbit follow-up (SPEAR)

Spec:

- REQ-PILOT-REC-01: If replacement tab creation or registration fails, the renderer shall rebuild the previous tree from snapshotted inputs under its original owner and namespace, then propagate the replacement failure.
- REQ-PILOT-REC-02: If recovery fails, the renderer shall preserve both failures and shall not index a disposed tree.
- REQ-PILOT-VAL-01: If node descriptions, description entries, or frames are null, the API shall reject them with IllegalArgumentException.
- REQ-PILOT-NOTIFY-01: Historical projection displays shall disable automatic toast and chat flags for the root and every supported child frame.

Prove: The old implementation failed five focused assertions: the three null-input exception contracts and both replacement recovery paths. The UAA 2.8.1 frame adapter is stubbed only in test scope so the tests can construct real AdvancementDisplay objects without a Minecraft runtime; display flags are not mocked.

Engine/architecture: Private registration snapshots retain the cloned root icon and immutable definitions. All display inputs are constructed/validated before old-tab removal. New-tab registration failure disposes the failed tab; recovery creates a fresh tab, never reuses a disposed UAA tab. Cleanup/recovery failures remain suppressed on the original exception. Providers still own progress/rewards; no database migrations, new tracks, or deployments are included.

Refine: Java 25/Paper 26.2 clean verify passes 25 tests. Coverage includes creation/registration failure recovery, mutated provider inputs, recovery failure, first registration failure, pre-removal display validation, null fields, and actual automatic notification flags for TASK/GOAL/CHALLENGE. Hosted fork review/checks remain separate gates. Live Minecraft tab replacement/reload rendering and 26.3 are not verified by these unit tests.

## September 27 remaining Codacy findings (SPEAR)

Spec: Preserve provider binary/source compatibility, registration recovery ordering, owner checks, and silent projection while reducing implementation and fixture complexity. Do not add tracks, execute rewards, migrate data, or deploy.

Prove/engine/architecture: Keep the existing 25 behavioral and failure-path tests. Extract notification-silent display/icon construction to PilotDisplays, separate key validation from title validation, isolate replacement recovery from registration prevalidation, and move shared registration test setup to RegistrationTestSupport. The boundary fixture no longer needs a six-argument constructor; wrong-owner celebration still calls the real API directly.

Refine: Java 25 clean verify passes all 25 tests after extraction. Review the final hosted reports separately. The public ProjectionService.Node record deliberately retains its ten components and all four constructor signatures: EnthusiaTags and other already-built providers link to those JVM descriptors. Changing this API solely to satisfy constructor argument-count/length metrics would require a separately coordinated provider migration. These compatibility exceptions are documented, not suppressed; remaining Codacy status must not be described as clean. No new runtime acceptance is claimed.

## September 27 registration-size follow-up

Spec: Reduce the plugin file and six recovery-test methods while retaining the same pre-removal validation, snapshot, cleanup, recovery, and suppressed-exception behavior. Keep all public Node JVM descriptors and record components.

Engine/architecture: Registration input validation now lives with its snapshot record. Recovery tests use shared JUnit-managed construction mocks and small scenario helpers; JUnit AutoClose releases each initialized mock after every test. Tests still verify restored progress, disposal counts, original exception identity, failed recovery, and mutation-resistant snapshots. Compact formatting makes the short delegating Node overloads readable without changing their signatures.

Prove/refine: Added reflection checks for all four public constructor descriptors and ten record components, plus legacy-overload metadata defaults. Java 25 clean verify passes 27 tests. An initial nested Mockito stubbing error introduced during extraction was caught and fixed before publication. The canonical Node still has ten arguments; reducing that count is an API migration, not a safe cleanup. No rules are suppressed and no clean Codacy verdict is claimed without the exact-head hosted result. No new tracks, data changes, upstream merge, or deployment.
