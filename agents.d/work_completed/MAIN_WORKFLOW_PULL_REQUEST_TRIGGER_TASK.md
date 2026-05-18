# Task: Add pull_request trigger to main.yml

## Goal
Replace the SSH key workaround (used in bot-created PRs to bypass the `GITHUB_TOKEN` push restriction) with a proper `pull_request` trigger in `main.yml` so all PRs trigger a build natively.

## Background
GitHub Actions does not fire other workflows when a branch is pushed using `GITHUB_TOKEN`. As a workaround, `weekly-cleanup-pr.yml` and `dependency-update.yml` use `ssh-key: ${{ secrets.SSH_PRIVATE_KEY }}` in their checkout steps so that `peter-evans/create-pull-request` pushes via SSH instead, which does trigger downstream workflows.

The cleaner long-term fix is to add a `pull_request` trigger to `main.yml` so the build fires on any PR regardless of how the branch was pushed.

## Hard constraints
- Do not break the existing `push` trigger behaviour (master deploys must continue to work).
- AWS credential selection (Deploy vs. LocalDevelopment role) must remain correct for both `push` and `pull_request` events.
- Concurrency group logic must remain correct so in-flight runs are cancelled appropriately on non-master refs.

## What to change
- `.github/workflows/main.yml`: add `pull_request` event trigger.
- Audit all `if: ${{ github.ref == 'refs/heads/master' }}` conditions to confirm they still guard deploy-only steps correctly under a `pull_request` event (they should, since a PR ref is not `refs/heads/master`).
- After the trigger is confirmed working, the `ssh-key` lines in `weekly-cleanup-pr.yml` and `dependency-update.yml` can be removed as they are no longer needed.

## Checklist

### Code changes
- [x] `main.yml`: replaced `push: branches-ignore` with `push: branches: [master]` + `pull_request` trigger
- [x] `main.yml`: audited all `if: github.ref == 'refs/heads/master'` guards — correct for both `push` and `pull_request` events (PR refs are `refs/pull/N/merge`)
- [x] `weekly-cleanup-pr.yml`: removed `ssh-key` from checkout step
- [x] `dependency-update.yml`: removed `ssh-key` from checkout step

### Validation
- [x] Master push build completes successfully (deploy role, release steps)
- [x] Test PR triggers `main.yml` with exactly one run (no duplicate from `push`)
- [x] Manually triggered bot workflow (`dependency-update` or `weekly-cleanup-pr`) produces a PR that shows a `main.yml` check run

## Definition of done
- `main.yml` has a `pull_request` trigger.
- A PR from any source (human or bot) triggers a build without requiring SSH key workarounds.
- `ssh-key` workaround lines removed from `weekly-cleanup-pr.yml` and `dependency-update.yml`.
