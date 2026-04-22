#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';

const args = process.argv.slice(2);
const pathArg = args.find((arg) => !arg.startsWith('--'));
const logPath = pathArg || 'build/test-output/test.jsonl';
const strictMode = args.includes('--strict');
const maxOffendersArg = args.find((arg) => arg.startsWith('--max-offenders='));
const maxOffenders = Number(maxOffendersArg?.split('=')[1] || 30);

const knownPhases = [
  'setup-start',
  'setup-finish',
  'exercise-start',
  'exercise-finish',
  'verify-start',
  'verify-finish',
  'test-start',
  'test-finish',
];

if (!fs.existsSync(logPath)) {
  console.error(`ERROR: file not found: ${logPath}`);
  process.exit(2);
}

const lines = fs.readFileSync(logPath, 'utf8').split(/\r?\n/);

const suiteNamesUsingTestmints = collectTestmintsSuiteNames(process.cwd());
const tests = new Map();
const offenders = [];
const phaseCounts = Object.fromEntries(knownPhases.map((phase) => [phase, 0]));

let parsedJsonLines = 0;
let nonJsonLines = 0;
let testStartCount = 0;
let testEndCount = 0;

for (let i = 0; i < lines.length; i += 1) {
  const line = lines[i];
  if (!line.trim()) continue;

  let event;
  try {
    event = JSON.parse(line);
    parsedJsonLines += 1;
  } catch {
    nonJsonLines += 1;
    continue;
  }

  const key = testKey(event);
  if (event.type === 'TestStart' && key) {
    testStartCount += 1;
    const record = tests.get(key) || makeRecord(event);
    record.starts += 1;
    tests.set(key, record);
  }

  if (event.type === 'TestEnd' && key) {
    testEndCount += 1;
    const record = tests.get(key) || makeRecord(event);
    record.ends += 1;
    record.status = event.status || record.status;
    record.durationMs = event.duration_ms;
    tests.set(key, record);
  }

  if (event.type === 'Log' && key && isTestmintsLog(event)) {
    const record = tests.get(key) || makeRecord(event);
    record.hasTestmints = true;
    const phase = parsePhase(event);
    if (phase) {
      record.phases.add(phase);
      phaseCounts[phase] = (phaseCounts[phase] || 0) + 1;
    }
    tests.set(key, record);
  }
}

let missingStart = 0;
let missingEnd = 0;
let duplicateStart = 0;
let duplicateEnd = 0;
let completedTests = 0;
let testsWithTestmints = 0;
let expectedTestmintsTests = 0;
let missingExpectedTestmints = 0;
let missingPhaseTests = 0;

for (const record of tests.values()) {
  if (record.starts === 0) {
    missingStart += 1;
    addOffender(`missing-start ${record.task} ${record.suite}.${record.test}`);
  }
  if (record.ends === 0) {
    missingEnd += 1;
    addOffender(`missing-end ${record.task} ${record.suite}.${record.test}`);
  }
  if (record.starts > 1) {
    duplicateStart += 1;
    addOffender(`duplicate-start(${record.starts}) ${record.task} ${record.suite}.${record.test}`);
  }
  if (record.ends > 1) {
    duplicateEnd += 1;
    addOffender(`duplicate-end(${record.ends}) ${record.task} ${record.suite}.${record.test}`);
  }

  const isSkipped = record.status === 'SKIPPED';
  if (!isSkipped && record.ends > 0) {
    completedTests += 1;
  }

  if (record.hasTestmints) {
    testsWithTestmints += 1;
  }

  const suiteSimpleName = record.suite.split('.').pop();
  const expectsTestmints = suiteNamesUsingTestmints.has(suiteSimpleName);

  if (!isSkipped && record.ends > 0 && expectsTestmints) {
    expectedTestmintsTests += 1;
    if (!record.hasTestmints) {
      missingExpectedTestmints += 1;
      addOffender(`missing-testmints ${record.task} ${record.suite}.${record.test}`);
      continue;
    }

    const missingPhases = requiredPhases().filter((phase) => !record.phases.has(phase));
    if (missingPhases.length > 0) {
      missingPhaseTests += 1;
      addOffender(`missing-phases(${missingPhases.join(',')}) ${record.task} ${record.suite}.${record.test}`);
    }
  }
}

const totalViolations =
  missingStart +
  missingEnd +
  duplicateStart +
  duplicateEnd +
  missingExpectedTestmints +
  missingPhaseTests;

const result = {
  file: logPath,
  mode: strictMode ? 'strict' : 'report',
  parsed_json_lines: parsedJsonLines,
  non_json_lines: nonJsonLines,
  test_start_events: testStartCount,
  test_end_events: testEndCount,
  unique_tests: tests.size,
  source_suites_using_testmints: suiteNamesUsingTestmints.size,
  tests_missing_start: missingStart,
  tests_missing_end: missingEnd,
  tests_with_duplicate_start: duplicateStart,
  tests_with_duplicate_end: duplicateEnd,
  completed_tests: completedTests,
  tests_with_testmints: testsWithTestmints,
  expected_testmints_tests: expectedTestmintsTests,
  tests_missing_expected_testmints: missingExpectedTestmints,
  tests_missing_required_testmints_phases: missingPhaseTests,
  phase_counts: phaseCounts,
  total_violations: totalViolations,
  failing_violations: strictMode ? totalViolations : 0,
  offenders,
};

console.log(JSON.stringify(result, null, 2));

if (strictMode && totalViolations > 0) {
  process.exit(1);
}

function makeRecord(event) {
  return {
    task: event.task || 'unknown-task',
    suite: event.suite || 'unknown-suite',
    test: event.test || 'unknown-test',
    starts: 0,
    ends: 0,
    status: null,
    durationMs: null,
    hasTestmints: false,
    phases: new Set(),
  };
}

function testKey(event) {
  if (!event.task || !event.suite || !event.test || !event.run_id) {
    return null;
  }
  return `${event.run_id}||${event.task}||${event.suite}||${event.test}`;
}

function isTestmintsLog(event) {
  if (event.logger === 'testmints') {
    return true;
  }

  const props = event.properties;
  if (props && typeof props === 'object') {
    if (props.testmints === true || typeof props.testmints_phase === 'string') {
      return true;
    }
  }

  const message = typeof event.message === 'string' ? event.message : '';
  return message.includes('[testmints]') || /\btestmints\b\s*-/.test(message);
}

function parsePhase(event) {
  const props = event.properties;
  if (props && typeof props === 'object' && typeof props.testmints_phase === 'string') {
    return props.testmints_phase;
  }

  const message = typeof event.message === 'string' ? event.message : '';
  return knownPhases.find((phase) => message.includes(phase)) || null;
}

function requiredPhases() {
  return ['setup-start', 'setup-finish', 'exercise-start', 'exercise-finish', 'verify-start', 'verify-finish'];
}

function collectTestmintsSuiteNames(rootDir) {
  const result = new Set();
  walk(rootDir);
  return result;

  function walk(dir) {
    if (isSkippedDir(dir)) return;

    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const fullPath = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        walk(fullPath);
        continue;
      }

      if (!entry.isFile() || !entry.name.endsWith('Test.kt')) {
        continue;
      }

      const content = fs.readFileSync(fullPath, 'utf8');
      if (!content.includes('com.zegreatrob.testmints')) {
        continue;
      }

      result.add(path.basename(entry.name, '.kt'));
    }
  }
}

function isSkippedDir(dirPath) {
  const name = path.basename(dirPath);
  return name === '.git' || name === '.gradle' || name === 'build' || name === 'node_modules';
}

function addOffender(message) {
  if (offenders.length < maxOffenders) {
    offenders.push(message);
  }
}
