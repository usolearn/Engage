# ENGAGE 0.6.0 — Android-first pilot candidate

<p align="center"><img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="144" alt="ENGAGE Android app icon — gold and dark navy" /></p>


MIT-licensed original source; no additional subscription required by this companion. Philippines/APAC initial pilot, international number support. Windows remains phase two. This is not a compiled APK, certified security product or approved app-store release. Existing Google/Microsoft apps handle their own authentication. No direct cloud APIs or automatic synchronization are implemented.

## Obtain an installation file
This repository contains the Android source. Choose one build path:
* GitHub Actions: enable Actions, open **ENGAGE Android pilot APK**, Run workflow. Download artifact `ENGAGE-0.6.0-Android-pilot` from a successful run.
* Azure DevOps: create a YAML pipeline against `azure-pipelines.yml` in the private repo, authorize only the necessary repo/build resources, and run. Download the matching pilot artifact. Microsoft-hosted runner availability and quotas vary by account. No paid resources are authorized by this package.
* Local Android Studio: JDK 17, SDK 35, Gradle 8.9 and AGP 8.7.3. From this directory run `gradle :app:lintDebug :app:assembleDebug` after configuration.

The cloud workflows build a debug-signed APK, verify its signature and write SHA-256 checksums. This is a debug pilot for controlled testing, not a production release. Public repository build artifacts may be accessible to other GitHub users. Debug signing keys can differ between CI runs; updating may require a fresh install and removal of pilot local data. No production signing key is embedded. Before public release, establish owner-controlled signing, Play policy review (including the target API requirement at submission), privacy disclosures, accessibility and compatibility testing, and independent code review.

## Intended pilot device setup
1. Download the successful build artifact from your authenticated build service. Compare the checksum against `SHA256SUMS.txt` and verify the expected signing identity. Do not install an APK from a stranger or an unverified link.
2. Extract the artifact and open `ENGAGE-0.6.0-pilot.apk`. If Android requires it, temporarily allow installs from the trusted file manager/browser used, then disable that authorization again. Do not turn off Play Protect.
3. Start ENGAGE. No broad permissions should be required for normal handoffs. Grant microphone access only if you want voice input. Review permission scope in Android settings.
4. Select **Choose ENGAGE as digital assistant (experimental)**. The Android role UI must offer the app and the user must approve it. Verify the assistant gesture opens ENGAGE. This is an activity-based assistant entry point, not a VoiceInteractionService or always-listening assistant. If unavailable, use the launcher icon. Do not remove existing assistants or messaging applications.
5. Follow ACCEPTANCE.md; return to the previous assistant in Settings > Apps > Default apps > Digital assistant app if needed. Uninstall the pilot to remove its local data. Export a desired security baseline before uninstalling.

## Scope
- Exact foreground voice commands: saved phrase (default Mr. Spock, analysis), open ChatGPT, open workspace, open Google Chat, open dialer, prepare SMS, prepare WhatsApp, security analysis. Select a recipient and type/dictate the message before routing. Voice recognition depends on the installed on-device recognizer and supported language. This app does not emulate ChatGPT Voice, resolve names from spoken commands, or support a custom always-on wake word.
- Android system contact picker grants access to the selected phone number. No general contacts, call-log, SMS-history, accessibility, device-admin or storage permission. Explicit Philippine mobile number conversion; no silent country-code guesses. Review recipients, message text and event timezone in target apps.
- Google Chat is opened, not replaced as a communications service. ChatGPT connectors remain separate. Outlook remains the preferred calendar; OneDrive the primary storage. Users sign in independently to destination services.
- Device baseline is limited to visible launcher apps and selected settings; changes are not evidence of compromise. No spyware detection claim.
- The app does not store message drafts in saved instance state, has backup disabled, and requests no direct Internet permission. User-initiated target apps may use the network under their own policies.

## Build / release evidence
See VALIDATION.md. No APK has been generated in this environment. Neither GitHub nor Azure workflow was executed here. Do not mark a release as installed until the owner confirms the physical test results.

## Project vision
A Spock-inspired assistant with voice commands and user-authorized integration. Continuous wake words, direct APIs, and comprehensive assistant functionality remain roadmap items.
