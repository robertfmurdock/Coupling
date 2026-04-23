#!/usr/bin/env node

import fs from 'node:fs';

const args = process.argv.slice(2);
const pathArg = args.find((arg) => !arg.startsWith('--'));
const path = pathArg || 'build/test-output/test.jsonl';
const maxOffendersArg = args.find((arg) => arg.startsWith('--max-offenders='));
const maxOffenders = Number(maxOffendersArg?.split('=')[1] || 20);
const strictMode = args.includes('--strict');
const failOnNonJson = args.includes('--fail-on-non-json');
const failOnMissingCore = args.includes('--fail-on-missing-core');

const requiredByAny = ['type', 'timestamp', 'run_id', 'platform'];
const endEventTypes = new Set(['TestEnd', 'StepEnd']);
const requiredByEnd = ['status', 'duration_ms', 'task', 'suite', 'test'];

if (!fs.existsSync(path)) {
  console.error(`ERROR: file not found: ${path}`);
  process.exit(2);
}

const lines = fs.readFileSync(path, 'utf8').split(/\r?\n/);

const results = {
  file: path,
  total_lines: lines.length,
  non_empty_lines: 0,
  parsed_json_lines: 0,
  non_json_lines: 0,
  missing_core_fields: 0,
  missing_end_fields: 0,
  bad_duration_ms: 0,
  type_counts: {},
  platform_counts: {},
};

const offenders = [];

const addCount = (bucket, key) => {
  const label = key === undefined || key === null || key === '' ? 'undefined' : String(key);
  bucket[label] = (bucket[label] || 0) + 1;
};

const missingKeys = (obj, keys) => keys.filter((k) => obj[k] === undefined || obj[k] === null || obj[k] === '');

const addOffender = (lineNumber, reason, sample) => {
  if (offenders.length < maxOffenders) {
    offenders.push({ line: lineNumber, reason, sample: sample.slice(0, 160) });
  }
};

for (let i = 0; i < lines.length; i += 1) {
  const line = lines[i];
  if (!line.trim()) {
    continue;
  }

  results.non_empty_lines += 1;

  let parsed;
  try {
    parsed = JSON.parse(line);
    results.parsed_json_lines += 1;
  } catch {
    results.non_json_lines += 1;
    addOffender(i + 1, 'non-json', line);
    continue;
  }

  addCount(results.type_counts, parsed.type);
  addCount(results.platform_counts, parsed.platform);

  const missingCore = missingKeys(parsed, requiredByAny);
  if (missingCore.length > 0) {
    results.missing_core_fields += 1;
    addOffender(i + 1, `missing core: ${missingCore.join(',')}`, line);
  }

  if (endEventTypes.has(parsed.type)) {
    const missingEnd = missingKeys(parsed, requiredByEnd);
    if (missingEnd.length > 0) {
      results.missing_end_fields += 1;
      addOffender(i + 1, `missing end: ${missingEnd.join(',')}`, line);
    }
    if (parsed.duration_ms !== undefined && typeof parsed.duration_ms !== 'number') {
      results.bad_duration_ms += 1;
      addOffender(i + 1, 'duration_ms not numeric', line);
    }
  }
}

const totalViolations =
  results.non_json_lines +
  results.missing_core_fields +
  results.missing_end_fields +
  results.bad_duration_ms;

const failingViolations = strictMode
  ? totalViolations
  : ((failOnNonJson ? results.non_json_lines : 0) +
    (failOnMissingCore ? results.missing_core_fields : 0));
const mode = strictMode
  ? 'strict'
  : (failOnNonJson && failOnMissingCore
    ? 'compat-fail-non-json-core'
    : (failOnNonJson
      ? 'compat-fail-non-json'
      : (failOnMissingCore ? 'compat-fail-core' : 'compat')));

console.log(JSON.stringify({
  ...results,
  mode,
  total_violations: totalViolations,
  failing_violations: failingViolations,
  offenders,
}, null, 2));

if (failingViolations > 0) {
  process.exit(1);
}
