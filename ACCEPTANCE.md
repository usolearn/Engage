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
