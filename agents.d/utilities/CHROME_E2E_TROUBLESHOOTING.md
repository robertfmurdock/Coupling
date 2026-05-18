# Chrome for Testing E2E Troubleshooting Guide

## Problem Summary

E2E tests occasionally fail due to Chrome for Testing issues. The failures typically manifest as:
- "Failed to connect to Chrome" errors
- "Chrome failed to start" messages  
- Tests hanging during browser launch
- Timeouts when initializing the browser

## Root Causes

The issue stems from how ChromeDriver manages Chrome binaries and temp files:

### 1. **Stuck ChromeDriver Processes**
ChromeDriver processes can get orphaned if tests crash or are interrupted. These processes hold ports and file locks, preventing new test runs from starting Chrome properly.

**Location:** Running processes can be found with:
```bash
pgrep -fl chromedriver
```

**Example:**
```
82989 /var/folders/.../chromedriver --port=63074
87077 /var/folders/.../chromedriver --port=64243
```

### 2. **Temp File Corruption**
ChromeDriver downloads and caches Chrome binaries in temporary directories. These can become corrupted if:
- Download is interrupted
- Disk runs out of space
- Process crashes during extraction
- File locks are not released properly

**Primary temp locations:**
- `$TMPDIR/<version>/chromedriver/` - Main temp directory (usually `/var/folders/...`)
- `$TMPDIR/chromedriver/` - Fallback location
- `~/.cache/puppeteer/chrome/` - Puppeteer's Chrome cache (771MB+)

### 3. **Version Mismatches**
The chromedriver package (currently 148.0.3) tries to download a matching Chrome for Testing binary. If the temp cache has an older version or corrupted files, tests can fail.

## Quick Diagnostic

Run the diagnostic script:
```bash
./agents.d/utilities/chrome-e2e-diagnostics.sh --check
```

This will show:
- ✓ Running Chrome/ChromeDriver processes
- ✓ Cached Chrome versions
- ✓ Temp directory sizes
- ✓ ChromeDriver binary status

## Current Known Issue (May 2026)

**Chrome 148.0.7778.167 crashes on macOS 26.5 (Sequoia 15.6)**

Symptoms:
- Error: `WebDriverError: tab crashed (Session info: chrome=148.0.7778.167)`
- Crash logs show `EXC_BREAKPOINT` / `SIGTRAP` at `ChromeMain`
- Happens immediately when ChromeDriver tries to start Chrome

This is a Chrome for Testing bug with version 148 on recent macOS versions, not a temp file issue.

## Solutions (in order of preference)

### 0. Fix Chrome 148 Crash (Current Issue)

If you see "tab crashed" with Chrome 148.0.7778.167 on macOS 26.5, downgrade chromedriver:

```bash
# In build.gradle.kts or package.json, pin to older chromedriver version
# Or use CHROMEDRIVER_VERSION environment variable
export DETECT_CHROMEDRIVER_VERSION=false
export CHROMEDRIVER_VERSION="147.0.0"

# Clean and rebuild
./agents.d/utilities/chrome-e2e-diagnostics.sh --clean
./gradlew :e2e:check
```

Alternatively, install Chrome stable and use it:
```bash
# Download Chrome stable from google.com/chrome
# Then set environment variable
export WDIO_CHROME_BINARY="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
./gradlew :e2e:check
```

### 1. Kill Stuck Processes (Fastest - for sleep/wake issues)
If the diagnostic shows stuck chromedriver processes:

```bash
# Kill all chromedriver processes
pkill -9 chromedriver

# Or kill specific PIDs shown by diagnostic
kill -9 82989 87077 88369
```

Then retry your tests:
```bash
./gradlew :e2e:jsE2eTest
```

### 2. Clean Build (Safe)
```bash
./gradlew clean :e2e:jsE2eTest
```

This clears Gradle's build cache but preserves Chrome downloads.

### 3. Nuclear Clean (Thorough)
If processes + clean build don't work, clean all Chrome caches:

```bash
./agents.d/utilities/chrome-e2e-diagnostics.sh --clean
```

⚠️ **WARNING:** This removes all cached Chrome binaries (~16MB) and will force re-download on next test run.

The script will remove:
- `~/.cache/puppeteer/` (~771MB)
- `$TMPDIR/<version>/chromedriver/`
- `$TMPDIR/chromedriver/`
- `build/js/node_modules/chromedriver/lib/chromedriver`

After cleaning, run:
```bash
./gradlew :e2e:jsE2eTest
```

ChromeDriver will automatically download fresh binaries.

### 4. Restart Machine (Ultimate Fix)
Restarting clears:
- All temp files in `$TMPDIR` 
- All orphaned processes
- File locks and system resources

This always works but is the slowest option.

## Prevention

### Why This Happens Periodically

The issue is inherent to the ChromeDriver installation model:
1. Each test run spawns chromedriver processes
2. If tests are interrupted (Ctrl-C, crash, timeout), processes can be orphaned
3. Temp directories accumulate over time
4. macOS `/var/folders/` temp space is cleaned on reboot, not continuously

### Best Practices

1. **Let tests complete gracefully** - Avoid Ctrl-C during browser startup
2. **Check for stuck processes** before running e2e tests if previous run had issues
3. **Restart occasionally** to clear system temp files
4. **Run diagnostic** when you encounter issues instead of guessing

## Technical Details

### ChromeDriver Installation Flow

1. Yarn installs `chromedriver` package (148.0.3) into `build/js/node_modules/`
2. Post-install script (`chromedriver/install.js`) runs
3. Script checks for existing binary in `findSuitableTempDirectory()`:
   - Tries `$npm_config_tmp`
   - Tries `$XDG_CACHE_HOME`
   - Tries `$TMPDIR` (usually `/var/folders/...` on macOS)
   - Falls back to `$(pwd)/tmp`
4. Downloads Chrome for Testing from `googlechromelabs.github.io`
5. Extracts to temp directory like `/var/folders/.../148.0.7778.167/chromedriver/`
6. Creates symlink in `node_modules/chromedriver/lib/chromedriver`

### Why Temp Files Matter

The e2e test configuration (`e2e/build.gradle.kts`) uses:
```kotlin
wdioTest {
    chromeBinary.set(System.getenv("WDIO_CHROME_BINARY"))
}
```

Since `WDIO_CHROME_BINARY` is not set, WebDriverIO uses the default Chrome for Testing binary that chromedriver downloaded. If those temp files are corrupted or locked, tests fail.

## File Locations Reference

| What | Location | Size | Cleaning |
|------|----------|------|----------|
| ChromeDriver temp cache | `$TMPDIR/<version>/chromedriver/` | ~16MB | Cleaned on reboot |
| Puppeteer Chrome cache | `~/.cache/puppeteer/chrome/` | ~771MB | Manual only |
| ChromeDriver package | `build/js/node_modules/chromedriver/` | ~2MB | `./gradlew clean` |
| Running processes | Memory | - | `pkill chromedriver` |

## Environment Variables

You can override ChromeDriver behavior:

```bash
# Skip download entirely (must provide your own Chrome)
export CHROMEDRIVER_SKIP_DOWNLOAD=true

# Force re-download even if cached
export CHROMEDRIVER_FORCE_DOWNLOAD=true

# Use specific Chrome binary for tests
export WDIO_CHROME_BINARY=/path/to/chrome

# Detect system Chrome version and match
export DETECT_CHROMEDRIVER_VERSION=true
```

## Related Files

- `e2e/build.gradle.kts` - E2E test configuration
- `build/js/packages/Coupling-e2e-e2eTest/wdio.conf.mjs` - WebDriverIO config (generated)
- `build/js/packages/Coupling-e2e-e2eTest/wdio.conf.d/chrome.mjs` - Chrome capabilities (generated)
- `build/js/node_modules/chromedriver/install.js` - ChromeDriver installation logic

## Diagnostic Script Usage

```bash
# Check status only (default)
./agents.d/utilities/chrome-e2e-diagnostics.sh
./agents.d/utilities/chrome-e2e-diagnostics.sh --check

# Clean all caches (with confirmation prompt)
./agents.d/utilities/chrome-e2e-diagnostics.sh --clean
```

The script is safe to run anytime and will show you exactly what's happening with Chrome for Testing.
