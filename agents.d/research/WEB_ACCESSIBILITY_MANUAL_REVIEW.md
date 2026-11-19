# Accessibility review: final manual pass

Status: pending user execution. This is a checklist, not evidence of a pass.
Related card: [Extended Web Accessibility Audit](../work/WEB_ACCESSIBILITY_AUDIT_FOLLOWUP.md).

## Environment and fixture

Use the freshly built local application at https://localhost, not the deployed
version. Record macOS, Safari and VoiceOver versions, app commit, approximate window
size, zoom and keyboard-navigation settings. If Safari presents a certificate
warning, the user must decide how to proceed; the agent must not bypass it.

Use a disposable local party with two named players (Alex and Sam), one pin and one
saved spin. Existing local fixtures are fine. Do not enter real private email
addresses. An empty party cannot validate saved forms, history or populated charts.
A contribution fixture is needed for the contributor popup and contribution views;
if unavailable, mark those rows untested. Do not connect real integrations or create
secrets for this audit. Preserve the fixture until any failures are reproduced.

## Calibrate first

1. Start VoiceOver with Command-F5 (or the Mac's configured shortcut).
2. Use VoiceOver's standard Control-Option (VO) navigation. Verify it speaks a
   known heading and a named link on About; verify VO-Space activates Back.
3. Check Safari's keyboard-navigation setting before judging Tab reachability.
   Record whether Quick Nav is enabled. Do not count ordinary Tab with silent
   VoiceOver as a screen-reader test.
4. If speech does not match the focused control, stop and recalibrate. A browser
   accessibility-tree query is not a substitute for what VoiceOver announces.

## Route checklist

Use Tab/Shift-Tab as well as VO navigation; arrow keys operate native selects,
Space operates checkboxes, Enter/VO-Space activates links and buttons.

| View/action | Expected evidence | Result |
| --- | --- | --- |
| Welcome → Get started → Demo | Heading outline is intelligible; real controls reachable; illustrated Demo inputs stay out of focus; final Back works | Pending |
| About | One main title, centered peer Back control; article readable; navigation leaves no stranded focus | Pending |
| Party List and Settings | Core actions reachable in sensible order; Settings opens, Escape closes and focus returns; no unnecessary skip-link stop | Pending |
| Recent information | Named dialog and Close announced, background inaccessible while modal, post scrolls to final line, Escape/Close return focus | Pending |
| New/edit Party | Every field has its visible name; Unique Id includes permanent URL warning; badge controls expose grouping/default/alternate hints; values remain editable | Pending |
| New/edit Player | Visible name labels agree; Email explains party access; badge options use configured names; selection is spoken | Pending |
| New/edit Pin | Name, Icon and Target named; Icon hint explains primary identifier; icon-picker link remains separately usable | Pending |
| Prepare/current/history | Prepare/Spin and Copy reachable in intended order; generated pair names intelligible; history entry content accessible; record whether manual rearrangement is possible by keyboard | Pending |
| Statistics/contributions | Filters named and operable; focused controls visible; record available text alternative to charts; try contributor popup and Escape/return focus | Pending |
| User/retired/pins/secrets | Outline and existing controls understandable; disabled identity fields spoken as disabled; inspect secrets UI without generating/copying a secret | Pending |
| GraphiQL/integration pages | GraphiQL frame identified as query editor; enter/exit with keyboard; inspect integration status without connecting external accounts | Pending |

## Reflow, contrast and report format

Repeat About, one form and Recent information at 200% zoom and narrow width. Confirm
article/dialog final lines remain reachable, controls do not overlap, and keyboard
focus stays visible. Record actual dimensions/zoom; do not infer a 320px reflow pass
from a single 200% zoom check. Restore zoom when finished.

Contrast is still a measurement task: visual inspection alone cannot establish
contrast ratios. Record actual foreground/background pairs and their ratios.
Forced-colors needs a supporting environment or explicit browser emulation; macOS
Increase Contrast and prefers-contrast are different checks. Mark unavailable
conditions untested.

For each failure: route, fixture/state, exact action, spoken/visible result,
expected result, browser/version, size/zoom and whether it repeats. For each pass:
record the row and tested conditions. Partial results are useful. No broad audit
completion or screen-reader certification is claimed while required rows remain
untested; larger interaction/visual redesigns need their own bounded work cards.
