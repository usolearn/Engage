# Android v0.6.0 hardware pilot

Phase 1 uses the voice-first source from the pre-scanner revision and excludes the camera scanner and ML Kit dependency. Phase 2 visual work is preserved on the `phase-2-visual-intelligence` branch. Reintroduce only after a verified signed pilot APK and physical-device acceptance; validate visual components separately. The pilot is not a production release. CI checks command routing, Android lint, debug assembly, APK signature and SHA-256. Physical-device testing remains required.

## ENGAGE Lite — mechanics-first private pilot
- Required: install and launch, on-demand on-device speech, typed-command fallback through the same command rules, app handoff, safe stop and permission denial, and user-confirmed communication drafts.
- Defer voiceprint enrollment/authentication, camera/visual intelligence, background listening, advanced security analysis, Windows and deeper account integrations. Preserve existing optional functionality without expanding it or making it a private hardware pilot gate.
- Run command-rule tests, assemble, verify signature and archive APK with SHA-256 before lint. Lint failures still block wider release; they do not prevent archiving an otherwise verified private test artifact. Never distribute as production-ready until lint and device acceptance pass.
- Hardware smoke test: launch; type a supported and unsupported command; deny then grant microphone; listen and stop; test app handoff and draft review with dummy data; verify no automatic sending or background recording.
