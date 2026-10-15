# Aura Transcribe v0.3.3 — AI Fix Keyboard & Overlay Toggle Reliability

`versionCode 48` (`v0.3.3`) — Maintenance and reliability release resolving a state-persistence issue in AI post-processing controls across the on-screen keyboard (`RustInputMethodService`) and floating overlay (`FloatingOverlayService`).

### Key Highlights
- **Persistent AI Fix Toggle State:** Fixed an issue where disabling the AI Fix switch from the keyboard or floating overlay removed the disk marker (`pp_enabled`) and subsequently blocked users from reactivating it without reopening the main settings activity.
- **Decoupled Configuration & Enablement Checks:** Introduced `resolvePostProcessConfigured` and `readPostProcessConfigured` in `SettingsManager`. The toggle listener now asynchronously validates provider credential and local model readiness on the credential lane independently of whether the global switch is currently toggled on.
- **Race Condition Prevention:** Added defensive checks ensuring late asynchronous credential callbacks never overwrite user intent if the toggle state was modified during an in-flight check.
- **Unit Test Coverage:** Added automated test cases in `PostProcessConfigurationGuardTest` verifying that provider configuration evaluation functions consistently regardless of switch state.
