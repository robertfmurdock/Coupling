# Chrome for Testing Sleep/Wake Corruption Investigation

## Problem Statement

E2E tests fail periodically after the laptop has been asleep/idle for a while. The failure manifests as:
- Error: `WebDriverError: tab crashed (Session info: chrome=148.0.7778.167)`
- Chrome crashes immediately when ChromeDriver tries to launch it
- Crash logs show `EXC_BREAKPOINT` / `SIGTRAP` at `ChromeMain`

## Root Cause Identified

The Chrome for Testing binary cache in `$TMPDIR/chrome/` (e.g., `/var/folders/.../T/chrome/mac_arm-148.0.7778.167/`) becomes corrupted after macOS sleep/wake cycles.

**Evidence:**
- Running `./gradlew :e2e:check` fails consistently with "tab crashed"
- Chrome binary runs fine from command line: `"$TMPDIR/chrome/.../Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing" --version` succeeds
- Deleting `$TMPDIR/chrome/` and re-running tests succeeds immediately
- Issue recurs after laptop sleeps and wakes

## Current Workaround

Manual cleanup script:
```bash
./agents.d/utilities/chrome-e2e-diagnostics.sh --clean
./gradlew :e2e:check
```

This works but requires manual intervention every time.

## Investigation Tasks

### 1. Understand Why Chrome Cache Corrupts After Sleep

**Questions to answer:**
- What specifically gets corrupted? File contents? File metadata? Extended attributes?
- Does macOS modify temp files during sleep/wake?
- Is this a known macOS issue with `/var/folders/` during sleep?
- Are there any file locks that don't get released properly?

**Investigation steps:**
- [ ] Before sleep: record checksums of Chrome binary and all related files
- [ ] After wake: compare checksums to identify what changed
- [ ] Check for extended attributes (xattr) changes after wake
- [ ] Check macOS system logs during sleep/wake for any errors related to temp files
- [ ] Test if issue happens with Chrome installed in a non-temp location
- [ ] Check if issue happens on other macOS versions or only 26.5 (Sequoia 15.6)

**Commands to run:**
```bash
# Before sleep
find "$TMPDIR/chrome" -type f -exec shasum {} \; > ~/chrome-checksums-before.txt
xattr -lr "$TMPDIR/chrome" > ~/chrome-xattr-before.txt

# After wake
find "$TMPDIR/chrome" -type f -exec shasum {} \; > ~/chrome-checksums-after.txt
xattr -lr "$TMPDIR/chrome" > ~/chrome-xattr-after.txt
diff ~/chrome-checksums-{before,after}.txt
diff ~/chrome-xattr-{before,after}.txt

# Check system logs
log show --predicate 'subsystem == "com.apple.kernel"' --last 1h | grep -i "chrome\|temp\|var/folders"
```

### 2. Identify the Library Responsible

**Suspects:**
1. **chromedriver npm package** (v148.0.3) - downloads and caches Chrome
   - Repository: https://github.com/giggio/node-chromedriver
   - Downloads Chrome to temp directory
   - Installation script: `build/js/node_modules/chromedriver/install.js`

2. **@wdio/local-runner** / **webdriverio** - manages ChromeDriver process
   - Repository: https://github.com/webdriverio/webdriverio
   - May hold file handles that don't release properly

3. **jsmints wdio plugin** - Gradle wrapper around WebDriverIO
   - Repository: https://github.com/robertfmurdock/jsmints (your own library)
   - Plugin used: `com.zegreatrob.jsmints.plugins.wdiotest`

**Investigation steps:**
- [ ] Check chromedriver package issues for sleep/wake or corruption reports
- [ ] Check WebDriverIO issues for macOS temp file issues
- [ ] Review how chromedriver package decides where to cache Chrome
- [ ] Check if chromedriver has an option to use a persistent cache location (not $TMPDIR)
- [ ] Review jsmints wdiotest plugin for any cleanup/lifecycle issues

### 3. Test Potential Solutions

#### Option A: Use Persistent Cache Location

Instead of `$TMPDIR`, cache Chrome in a location that macOS doesn't clean/modify:

```bash
# Set environment variable to use persistent location
export CHROMEDRIVER_CACHE_DIR=~/.local/cache/chrome-for-testing
```

**Test:**
- [ ] Modify chromedriver installation to use persistent location
- [ ] Verify cache survives sleep/wake cycles
- [ ] Measure disk space impact (~16MB per Chrome version)
- [ ] Ensure cache gets cleaned up eventually (don't accumulate old versions)

#### Option B: Use System Chrome

Install Chrome stable and use it instead of Chrome for Testing:

```kotlin
// In e2e/build.gradle.kts
wdioTest {
    chromeBinary.set("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
}
```

**Test:**
- [ ] Install Chrome stable
- [ ] Configure tests to use it
- [ ] Verify tests pass
- [ ] Verify system Chrome survives sleep/wake
- [ ] Check if this causes any version compatibility issues

#### Option C: Pre-Sleep Hook

Create a hook that cleans Chrome cache before sleep:

```bash
# macOS sleepwatcher or similar
# ~/.sleep
rm -rf "$TMPDIR/chrome"
```

**Test:**
- [ ] Install sleepwatcher: `brew install sleepwatcher`
- [ ] Create pre-sleep script
- [ ] Verify it runs before sleep
- [ ] Measure if this prevents the issue

#### Option D: Gradle Task to Validate Chrome Cache

Add a Gradle task that validates Chrome cache before running tests and auto-cleans if corrupted:

```kotlin
tasks.register("validateChromeCache") {
    doFirst {
        // Check if Chrome binary is valid
        // If not, clean cache
    }
}

tasks.named("e2eRun") {
    dependsOn("validateChromeCache")
}
```

**Test:**
- [ ] Implement validation task
- [ ] Test that it detects corrupted cache
- [ ] Test that it auto-recovers
- [ ] Measure performance impact

### 4. File Bug Reports

Once root cause is confirmed, file bugs with appropriate projects:

#### If issue is with chromedriver npm package:
**Repository:** https://github.com/giggio/node-chromedriver/issues

**Bug report template:**
```markdown
Title: Chrome for Testing cache corrupts after macOS sleep/wake cycles

## Environment
- macOS: 26.5 (Sequoia 15.6)
- chromedriver package: 148.0.3
- Chrome for Testing: 148.0.7778.167
- Architecture: Apple Silicon (M2/M3)

## Description
Chrome for Testing binaries cached in $TMPDIR/chrome/ become corrupted after macOS sleep/wake cycles, causing "tab crashed" errors when ChromeDriver attempts to launch Chrome.

## Reproduction
1. Install chromedriver package
2. Run tests successfully with Chrome for Testing
3. Put Mac to sleep
4. Wake Mac
5. Run tests again - Chrome crashes with EXC_BREAKPOINT

## Expected Behavior
Chrome cache should remain valid after sleep/wake cycles

## Proposed Solutions
- Use persistent cache location outside $TMPDIR
- Add cache validation on startup
- Document workaround of deleting cache after wake

## Workaround
```bash
rm -rf "$TMPDIR/chrome"
# Re-run tests to trigger fresh download
```
```

#### If issue is with macOS handling of /var/folders:
**Feedback:** Use macOS Feedback Assistant

**Report:**
- Temp files in `/var/folders/` get corrupted after sleep/wake
- Affects downloaded application bundles
- May be related to file system journaling or quarantine attributes

#### If issue is with WebDriverIO:
**Repository:** https://github.com/webdriverio/webdriverio/issues

Focus on whether WebDriverIO properly releases file handles and processes on test completion.

## Success Criteria

One or more of the following achieved:

1. **Permanent fix deployed** - Chrome cache no longer corrupts after sleep/wake
2. **Auto-recovery implemented** - Tests automatically recover from corrupted cache without manual intervention
3. **Bug reports filed** - Upstream projects are aware and tracking the issue
4. **Alternative solution documented** - System Chrome or persistent cache location used instead

## Files to Reference

- `/Users/robertfmurdock/git/Coupling/e2e/build.gradle.kts` - E2E test configuration
- `/Users/robertfmurdock/git/Coupling/build/js/node_modules/chromedriver/install.js` - Chrome download logic
- `/Users/robertfmurdock/git/Coupling/agents.d/utilities/chrome-e2e-diagnostics.sh` - Diagnostic script
- `/Users/robertfmurdock/git/Coupling/agents.d/utilities/CHROME_E2E_TROUBLESHOOTING.md` - Troubleshooting guide
- `~/Library/Logs/DiagnosticReports/Google Chrome for Testing*.ips` - Chrome crash logs

## Related Issues

Search these repositories for related issues:
- node-chromedriver: "sleep", "wake", "corrupt", "macOS", "tmp"
- webdriverio: "chrome crash", "macOS", "tmp"
- Chromium project: "Chrome for Testing" + "macOS"

## Timeline

This is a non-blocking enhancement. The workaround (manual cache cleanup) is documented and functional.

Suggested priority: **P2 - Medium**
- Not blocking development
- Affects developer productivity
- Workaround exists but is manual

## Checklist

- [ ] Complete "Understand Why" investigation
- [ ] Identify responsible library
- [ ] Test at least 2 potential solutions
- [ ] File bug reports if external issue confirmed
- [ ] Implement best solution
- [ ] Update documentation with permanent fix
- [ ] Move this file to agents.d/work_completed/
