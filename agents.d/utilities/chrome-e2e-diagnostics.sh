#!/usr/bin/env bash
# Chrome for Testing E2E Diagnostics and Cleanup
#
# This script helps diagnose and fix Chrome for Testing issues that cause e2e test failures.
# The issue typically manifests as chromedriver being unable to start Chrome due to corrupted
# or locked temp files.
#
# Usage:
#   ./chrome-e2e-diagnostics.sh [--clean]
#
# Options:
#   --clean    Remove all Chrome for Testing caches and temp files (CAUTION: will trigger re-download)
#   --check    Check status only (default)

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

function print_header() {
    echo -e "${GREEN}=== $1 ===${NC}"
}

function print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

function print_error() {
    echo -e "${RED}✗ $1${NC}"
}

function print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

function check_puppeteer_cache() {
    print_header "Checking Puppeteer Chrome Cache"

    if [ -d ~/.cache/puppeteer ]; then
        echo "Puppeteer cache location: ~/.cache/puppeteer"
        echo "Chrome versions cached:"
        find ~/.cache/puppeteer/chrome -maxdepth 1 -type d | tail -n +2 | while read -r dir; do
            echo "  - $(basename "$dir")"
        done

        # Check size
        local size=$(du -sh ~/.cache/puppeteer 2>/dev/null | cut -f1)
        echo "Total cache size: $size"
    else
        print_warning "Puppeteer cache not found at ~/.cache/puppeteer"
    fi
    echo
}

function check_chromedriver_temp() {
    print_header "Checking ChromeDriver Temp Directories"

    # Check common temp locations
    local temp_locations=(
        "${TMPDIR:-}"
        "/tmp"
        "${XDG_CACHE_HOME:-}"
        "$(pwd)/tmp"
    )

    local found=0
    for temp_dir in "${temp_locations[@]}"; do
        if [ -z "$temp_dir" ]; then
            continue
        fi

        # Look for chromedriver version directories
        if [ -d "$temp_dir" ]; then
            while IFS= read -r -d '' chrome_dir; do
                echo "Found: $chrome_dir"
                local size=$(du -sh "$chrome_dir" 2>/dev/null | cut -f1)
                echo "  Size: $size"
                found=1
            done < <(find "$temp_dir" -maxdepth 2 -type d -name "chromedriver" -print0 2>/dev/null)
        fi
    done

    if [ $found -eq 0 ]; then
        echo "No ChromeDriver temp directories found"
    fi
    echo
}

function check_yarn_chromedriver() {
    print_header "Checking Yarn ChromeDriver Package"

    if [ -d build/js/node_modules/chromedriver ]; then
        local version=$(grep '"version"' build/js/node_modules/chromedriver/package.json | head -1 | cut -d'"' -f4)
        echo "ChromeDriver package version: $version"

        if [ -f build/js/node_modules/chromedriver/lib/chromedriver/chromedriver ]; then
            print_success "ChromeDriver binary exists"
        else
            print_error "ChromeDriver binary NOT found"
        fi
    else
        print_warning "ChromeDriver package not installed (run gradle build first)"
    fi
    echo
}

function check_chrome_processes() {
    print_header "Checking for Running Chrome/ChromeDriver Processes"

    local chrome_procs=$(pgrep -fl "chrome|chromedriver" || true)
    if [ -n "$chrome_procs" ]; then
        print_warning "Found running Chrome/ChromeDriver processes:"
        echo "$chrome_procs"
        echo
        echo "These may need to be killed if tests are stuck"
    else
        print_success "No Chrome/ChromeDriver processes running"
    fi
    echo
}

function clean_all() {
    print_header "CLEANING Chrome for Testing Caches"

    print_warning "This will remove all cached Chrome binaries and force re-download"
    echo "Press Ctrl-C to abort, or Enter to continue..."
    read -r

    # Clean puppeteer cache
    if [ -d ~/.cache/puppeteer ]; then
        echo "Removing ~/.cache/puppeteer..."
        rm -rf ~/.cache/puppeteer
        print_success "Removed Puppeteer cache"
    fi

    # Clean temp directories
    local temp_locations=(
        "${TMPDIR:-}"
        "/tmp"
        "${XDG_CACHE_HOME:-}"
    )

    for temp_dir in "${temp_locations[@]}"; do
        if [ -z "$temp_dir" ] || [ ! -d "$temp_dir" ]; then
            continue
        fi

        # Clean chromedriver directories
        while IFS= read -r -d '' chrome_dir; do
            echo "Removing $chrome_dir..."
            rm -rf "$chrome_dir"
            print_success "Removed $(basename "$(dirname "$chrome_dir")")/chromedriver"
        done < <(find "$temp_dir" -maxdepth 2 -type d -name "chromedriver" -print0 2>/dev/null)

        # Clean chrome directories (where Chrome for Testing binaries are cached)
        if [ -d "$temp_dir/chrome" ]; then
            echo "Removing $temp_dir/chrome..."
            rm -rf "$temp_dir/chrome"
            print_success "Removed Chrome for Testing cache"
        fi
    done

    # Clean yarn chromedriver
    if [ -d build/js/node_modules/chromedriver/lib/chromedriver ]; then
        echo "Removing build/js/node_modules/chromedriver/lib/chromedriver..."
        rm -rf build/js/node_modules/chromedriver/lib/chromedriver
        print_success "Removed ChromeDriver binary"
    fi

    echo
    print_success "Cleanup complete"
    echo "Next steps:"
    echo "  1. Run: ./gradlew :e2e:jsE2eTest"
    echo "  2. ChromeDriver will be re-downloaded automatically"
}

function main() {
    local mode="check"

    if [ $# -gt 0 ]; then
        case "$1" in
            --clean)
                mode="clean"
                ;;
            --check)
                mode="check"
                ;;
            *)
                echo "Usage: $0 [--check|--clean]"
                exit 1
                ;;
        esac
    fi

    if [ "$mode" = "clean" ]; then
        clean_all
    else
        echo "Chrome for Testing E2E Diagnostics"
        echo "=================================="
        echo
        check_chrome_processes
        check_puppeteer_cache
        check_chromedriver_temp
        check_yarn_chromedriver

        echo
        print_header "Quick Fixes"
        echo "1. If tests fail, first try: ./gradlew clean :e2e:jsE2eTest"
        echo "2. If that doesn't work, run: $0 --clean"
        echo "3. Restarting your machine will also clear temp files"
        echo
        echo "Common failure symptoms:"
        echo "  - 'Failed to connect to Chrome' errors"
        echo "  - 'Chrome failed to start' messages"
        echo "  - Tests hanging during browser launch"
    fi
}

main "$@"
