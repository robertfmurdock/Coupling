#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  scripts/test-suite-performance-harness.sh [options]

Options:
  --iterations N              Number of repeated runs (default: 5)
  --tasks "TASKS"             Gradle tasks to execute (default: ":sdk:jsNodeTest :sdk:jvmTest")
  --actions "A,B,C"           Comma-separated actions to track (default: DeletePartyCommand,ApplyBoostCommand,GqlQuery)
  --output-dir PATH           Output directory (default: build/test-output/perf-harness-<timestamp>)
  --gradle-args "ARGS"        Extra args passed to Gradle test task invocation
  --split-task-invocations    Run each task as a separate Gradle invocation per iteration
  --skip-test-runs            Skip task execution and only aggregate existing run artifacts
  -h, --help                  Show this help

Example:
  scripts/test-suite-performance-harness.sh \
    --iterations 5 \
    --tasks ":sdk:jsNodeTest :sdk:jvmTest" \
    --actions "DeletePartyCommand,ApplyBoostCommand,GqlQuery"
EOF
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "ERROR: required command not found: $1" >&2
    exit 1
  fi
}

iterations=5
tasks=":sdk:jsNodeTest :sdk:jvmTest"
actions_csv="DeletePartyCommand,ApplyBoostCommand,GqlQuery"
timestamp="$(date +%Y%m%d-%H%M%S)"
output_dir="build/test-output/perf-harness-${timestamp}"
gradle_extra_args=""
split_task_invocations=0
skip_test_runs=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --iterations)
      iterations="${2:-}"
      shift 2
      ;;
    --tasks)
      tasks="${2:-}"
      shift 2
      ;;
    --actions)
      actions_csv="${2:-}"
      shift 2
      ;;
    --output-dir)
      output_dir="${2:-}"
      shift 2
      ;;
    --gradle-args)
      gradle_extra_args="${2:-}"
      shift 2
      ;;
    --split-task-invocations)
      split_task_invocations=1
      shift
      ;;
    --skip-test-runs)
      skip_test_runs=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "ERROR: unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if ! [[ "$iterations" =~ ^[0-9]+$ ]] || [[ "$iterations" -lt 1 ]]; then
  echo "ERROR: --iterations must be an integer >= 1" >&2
  exit 1
fi

require_cmd jq
require_cmd awk

mkdir -p "$output_dir"
actions_json="$(printf '%s\n' "$actions_csv" | jq -Rc 'split(",") | map(gsub("^\\s+|\\s+$";"")) | map(select(length > 0))')"

echo "Output directory: $output_dir"
echo "Iterations: $iterations"
echo "Tasks: $tasks"
echo "Actions: $actions_csv"
echo "Split task invocations: $split_task_invocations"

if [[ "$skip_test_runs" -eq 0 ]]; then
  echo "Resetting test.jsonl"
  ./gradlew resetTestJsonl --no-configuration-cache >/dev/null

  for run in $(seq 1 "$iterations"); do
    run_dir="$output_dir/run-${run}"
    mkdir -p "$run_dir"
    echo "[$run/$iterations] Running tasks"
    if [[ "$split_task_invocations" -eq 1 ]]; then
      IFS=' ' read -r -a task_array <<<"$tasks"
      for idx in "${!task_array[@]}"; do
        task_name="${task_array[$idx]}"
        if [[ -z "$task_name" ]]; then
          continue
        fi
        echo "[$run/$iterations] Running task ${idx}/${#task_array[@]}: $task_name"
        if [[ "$idx" -eq 0 ]]; then
          # shellcheck disable=SC2086
          ./gradlew -Pcoupling.testLog.reset=true "$task_name" --no-configuration-cache --rerun-tasks $gradle_extra_args
        else
          # shellcheck disable=SC2086
          ./gradlew "$task_name" --no-configuration-cache --rerun-tasks $gradle_extra_args
        fi
      done
    else
      # shellcheck disable=SC2086
      ./gradlew -Pcoupling.testLog.reset=true $tasks --no-configuration-cache --rerun-tasks $gradle_extra_args
    fi

    echo "[$run/$iterations] Capturing analyze report"
    ./gradlew analyzeTestJsonl --no-configuration-cache >"$run_dir/analyze.json"
    cp build/test-output/test.jsonl "$run_dir/test.jsonl"

    echo "[$run/$iterations] Computing per-run metrics"
    jq -s --argjson run "$run" --argjson keyActions "$actions_json" '
      def percentile($arr; $p):
        if ($arr | length) == 0 then null
        else
          ($arr | sort) as $sorted
          | $sorted[(((($sorted | length) - 1) * $p) | floor)]
        end;
      def stats($arr):
        if ($arr | length) == 0 then
          {count: 0, avg_ms: null, p95_ms: null, min_ms: null, max_ms: null}
        else
          {
            count: ($arr | length),
            avg_ms: (($arr | add) / ($arr | length)),
            p95_ms: percentile($arr; 0.95),
            min_ms: ($arr | min),
            max_ms: ($arr | max)
          }
        end;
      [
        .[]
        | select(
            .type == "Log"
            and .logger == "command"
            and .properties.command_phase == "end"
            and ((.properties.command_duration_ms | type) == "number")
          )
        | {
            task: (.task // "unknown-task"),
            action: (.properties.command_action // "unknown-action"),
            duration_ms: .properties.command_duration_ms
          }
      ] as $events
      | {
          run: $run,
          tasks: (
            $events
            | group_by(.task)
            | map(
                {task: .[0].task}
                + stats(map(.duration_ms))
                + {sample_size: (map(.duration_ms) | length)}
              )
            | sort_by(.task)
          ),
          key_actions: (
            $keyActions
            | map(
                . as $action
                | ($events | map(select(.action == $action) | .duration_ms)) as $durations
                | ({action: $action} + stats($durations))
              )
          )
        }
    ' "$run_dir/test.jsonl" >"$run_dir/metrics.json"

    if [[ "$(jq '[.tasks[] | .count] | add // 0' "$run_dir/metrics.json")" -eq 0 ]]; then
      echo "ERROR: run $run captured zero command end events; check task selection/logging." >&2
      exit 1
    fi
  done
else
  echo "Skipping test runs; using existing artifacts under $output_dir/run-*/metrics.json"
fi

echo "Aggregating results"
jq -s --argjson keyActions "$actions_json" '
  def median($arr):
    if ($arr | length) == 0 then null
    else
      ($arr | sort) as $sorted
      | $sorted[(((($sorted | length) - 1) / 2) | floor)]
    end;
  def percentile($arr; $p):
    if ($arr | length) == 0 then null
    else
      ($arr | sort) as $sorted
      | $sorted[(((($sorted | length) - 1) * $p) | floor)]
    end;
  def mean($arr):
    if ($arr | length) == 0 then null else (($arr | add) / ($arr | length)) end;
  def stddev($arr):
    if ($arr | length) < 2 then 0
    else
      (mean($arr)) as $m
      | (([$arr[] | ((. - $m) * (. - $m))] | add) / ($arr | length) | sqrt)
    end;
  def cv($arr):
    if ($arr | length) == 0 then null
    else
      (mean($arr)) as $m
      | if $m == 0 then 0 else (stddev($arr) / $m) end
    end;
  def summarize_values($arr):
    {
      runs: ($arr | length),
      min: (if ($arr | length) == 0 then null else ($arr | min) end),
      max: (if ($arr | length) == 0 then null else ($arr | max) end),
      median: median($arr),
      p95: percentile($arr; 0.95),
      cv: cv($arr)
    };
  def numbers_or_empty: map(select(. != null));

  . as $runs
  | {
      generated_at_utc: now | todate,
      run_count: ($runs | length),
      tasks: (
        [ $runs[] | .tasks[]? | .task ] | unique | sort
        | map(
            . as $task
            | ($runs | map(.tasks[]? | select(.task == $task) | .avg_ms) | numbers_or_empty) as $avgVals
            | ($runs | map(.tasks[]? | select(.task == $task) | .p95_ms) | numbers_or_empty) as $p95Vals
            | {
                task: $task,
                avg_command_ms: summarize_values($avgVals),
                p95_command_ms: summarize_values($p95Vals)
              }
          )
      ),
      key_actions: (
        $keyActions
        | map(
            . as $action
            | ($runs | map(.key_actions[]? | select(.action == $action) | .p95_ms) | numbers_or_empty) as $p95Vals
            | ($runs | map(.key_actions[]? | select(.action == $action) | .avg_ms) | numbers_or_empty) as $avgVals
            | {
                action: $action,
                avg_ms: summarize_values($avgVals),
                p95_ms: summarize_values($p95Vals)
              }
          )
      )
    }
' "$output_dir"/run-*/metrics.json >"$output_dir/summary.json"

jq -r '
  def f($n):
    if $n == null then "n/a" else (($n * 1000 | round) / 1000 | tostring) end;
  "Task-level command duration summary",
  "task | avg_median_ms | avg_p95_ms | avg_cv | p95_median_ms | p95_p95_ms | p95_cv",
  (.tasks[] | [
      .task,
      f(.avg_command_ms.median),
      f(.avg_command_ms.p95),
      f(.avg_command_ms.cv),
      f(.p95_command_ms.median),
      f(.p95_command_ms.p95),
      f(.p95_command_ms.cv)
    ] | join(" | ")),
  "",
  "Key action command duration summary",
  "action | avg_median_ms | avg_p95_ms | avg_cv | p95_median_ms | p95_p95_ms | p95_cv",
  (.key_actions[] | [
      .action,
      f(.avg_ms.median),
      f(.avg_ms.p95),
      f(.avg_ms.cv),
      f(.p95_ms.median),
      f(.p95_ms.p95),
      f(.p95_ms.cv)
    ] | join(" | "))
' "$output_dir/summary.json" | tee "$output_dir/summary.txt"

cat <<EOF

Done.
- Raw per-run data: $output_dir/run-*/{analyze.json,test.jsonl,metrics.json}
- Aggregated JSON: $output_dir/summary.json
- Human summary:   $output_dir/summary.txt
EOF
