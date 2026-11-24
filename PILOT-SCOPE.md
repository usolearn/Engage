# Android v0.6.0 hardware pilot

Phase 1 uses the voice-first source from the pre-scanner revision and excludes the camera scanner and ML Kit dependency. Phase 2 visual work is preserved on the `phase-2-visual-intelligence` branch. Reintroduce only after a verified signed pilot APK and physical-device acceptance; validate visual components separately. The pilot is not a production release. CI checks command routing, Android lint, debug assembly, APK signature and SHA-256. Physical-device testing remains required.
