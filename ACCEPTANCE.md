# Android private-pilot acceptance checklist
Record device model, Android version, APK SHA-256, build run ID, signing identity, date, result and defect notes. Use test contacts and dummy messages, not sensitive production data.

- [ ] Install verified debug pilot; launch without crash; icon and app title read ENGAGE.
- [ ] No requested microphone permission until Listen/Dictate is tapped. Deny first; normal controls still work. Then grant microphone and test voice.
- [ ] Confirm portrait/landscape layout, scrolling, keyboard and system bar insets on the user's device.
- [ ] Confirm assistant-role screen availability; select ENGAGE if available; verify gesture; confirm rollback to prior assistant. Not a Google Chat replacement.
- [ ] Contact picker returns selected phone; manual number entry works; cancel leaves draft unchanged. Try local Philippine and +63 formats, plus a +1 test number.
- [ ] Phone: dialer opens with correct number; do not actually call unless approved.
- [ ] SMS: Google Messages receives correct draft, recipient and body; nothing is sent automatically. Test default-app fallback and missing app handling.
- [ ] WhatsApp: correct international number and draft; no automatic send. Missing app fails safely.
- [ ] Google Chat: opens app, or offers official site; Google Workspace and Microsoft apps open under correct accounts.
- [ ] Gmail/Outlook drafts and calendar event handoff; validate exact recipient, start/end, local timezone and selected calendar before saving. No automatic send or save.
- [ ] Voice: each supported command, arbitrary speech, denial and cancellation; no background microphone after leaving app. Check on-device recognizer availability and offline behavior.
- [ ] Security baseline: save only after reviewing visible apps; install/remove a known test app; compare. Ordinary app updates are not evidence of compromise.
- [ ] Share a dummy text into ENGAGE; import clears recipient; no auto-send. Confirm cancellation retains previous draft. Sensitive text is not needed for test.
- [ ] Test uninstall/rollback; note the debug signing update limitation. Keep existing communication apps available.

## Release gate
All core cases must pass or have documented noncritical exceptions; any unexpected message transmission, account access, crash on startup, permission escalation or incorrect recipient blocks wider release. Owner approves the next phase. Windows is secondary and not part of Android acceptance.

## Voice calibration acceptance
- [ ] On first launch, voice setup is visible; it can be skipped and never requests the microphone until the user taps a listening control.
- [ ] Select English recognition locale (US, UK, Philippines, Australia, India, Canada); test available on-device language packs, including unavailable-locale failure handling.
- [ ] Read all eight prompts. Check recognized text against expected phrase, verify microphone signal display, retry mismatches, and confirm calibration never routes a command.
- [ ] Confirm locale and completion flag persist, locale change resets calibration, and no voiceprint, raw audio, or training transcript is saved by ENGAGE.
- [ ] Test low-confidence commands, unknown confidence, background noise, accents, Bluetooth microphone and interrupted sessions; ensure no automatic sends. The 0.60 confidence threshold is provisional, not an accuracy guarantee.
- [ ] Android vocabulary bias is provider-dependent; calibration tests recognition, not acoustic model retraining or identity verification.

## Visual scanner acceptance
- [ ] Opening the scanner does not activate camera or request permission until Front or Rear is selected. Deny permission; other features remain usable.
- [ ] Test front and rear camera, orientation, framing, mirrored front view, switching, Stop, Back, Home, screen lock, incoming interruption, rotation and camera-in-use error. No camera active after leaving the scanner; explicit restart required.
- [ ] Verify original scan cue plays only after preview starts, respects silent/DND and toggle, and stops promptly on exit.
- [ ] Optional face indicator is off by default; test hardware-supported face count and mouth landmark availability, unsupported mode and disabling. No identity, lip reading, emotion inference, recording, storage, upload or model access. Obtain consent before pointing camera at others.
- [ ] Live AI visual reasoning and speech/gesture fusion are separate future features requiring explicit user-approved frames and a validated model integration.

## Visual intelligence acceptance (source candidate)
- [ ] Analyze a user-selected frame using the bundled image-labeling model in airplane mode; test several ordinary objects, a person, poor lighting, and unknown objects. Verify results are phrased as tentative, generic observations; no identity or sensitive inference.
- [ ] Check preview orientation and model input on both cameras. Confirm no file is created, no frame upload occurs, and merged manifest excludes INTERNET permission. Review dependency licenses and privacy before commercial distribution.
- [ ] Check offline English speech voice selection, speech-rate/pitch, mute toggle, missing voice fallback, rapid scan switching, and stop/back/home during inference. No spoken output after leaving the scanner.
- [ ] Verify complete build, lint and independent device acceptance before public release. Image labeling is not a general vision-language model or material-composition analysis.
