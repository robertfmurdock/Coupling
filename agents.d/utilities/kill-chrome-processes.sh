#!/usr/bin/env bash
# Kill stuck ChromeDriver processes
#
# This is a quick fix for when e2e tests fail due to orphaned chromedriver processes.
# These processes can hold ports and prevent new test runs from starting.
#
# Usage:
#   ./kill-chrome-processes.sh

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "Checking for ChromeDriver processes..."

# Find chromedriver processes (exclude this script and grep)
CHROMEDRIVER_PIDS=$(pgrep -f "chromedriver.*--port" || true)

if [ -z "$CHROMEDRIVER_PIDS" ]; then
    echo -e "${GREEN}✓ No stuck ChromeDriver processes found${NC}"
    exit 0
fi

echo -e "${YELLOW}Found ChromeDriver processes:${NC}"
pgrep -fl "chromedriver.*--port"
echo

echo -e "${RED}Killing processes...${NC}"
echo "$CHROMEDRIVER_PIDS" | xargs kill -9

echo -e "${GREEN}✓ ChromeDriver processes killed${NC}"
echo
echo "You can now run: ./gradlew :e2e:jsE2eTest"
