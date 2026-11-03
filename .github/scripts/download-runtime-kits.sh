#!/usr/bin/env bash
# Downloads and unpacks one Runtime Kit set into $RUNNER_TEMP/autojs6-runtime-kits.
# Formal builds read the same-tag Release of RUNTIME_KIT_REPO (the AutoJs6 Releases carry no kits);
# candidate builds read SOURCE_ARTIFACT from Actions run SOURCE_RUN_ID of SOURCE_REPO.
set -euo pipefail
shopt -s nullglob

archive_dir="$RUNNER_TEMP/autojs6-runtime-kit-download"
runtime_kits_dir="$RUNNER_TEMP/autojs6-runtime-kits"
rm -rf "$archive_dir" "$runtime_kits_dir"
mkdir -p "$archive_dir" "$runtime_kits_dir"

if [ -n "${SOURCE_RUN_ID:-}" ]; then
  gh run download "$SOURCE_RUN_ID" \
    --repo "${SOURCE_REPO:?SOURCE_REPO is required for an Actions artifact}" \
    --name "${SOURCE_ARTIFACT:?SOURCE_ARTIFACT is required for an Actions artifact}" \
    --dir "$archive_dir"
else
  gh release download "${SOURCE_TAG:?SOURCE_TAG is required}" \
    --repo "${RUNTIME_KIT_REPO:?RUNTIME_KIT_REPO is required for a Release download}" \
    --pattern "autojs6-runtime-kit-*.zip" \
    --dir "$archive_dir"
fi

archives=("$archive_dir"/autojs6-runtime-kit-*.zip)
if [ "${#archives[@]}" -eq 0 ]; then
  echo "No Runtime Kit archives were downloaded." >&2
  exit 1
fi

for archive in "${archives[@]}"; do
  archive_name="$(basename "${archive%.zip}")"
  target_dir="$runtime_kits_dir/$archive_name"
  mkdir -p "$target_dir"
  unzip -q "$archive" -d "$target_dir"
done
rm -rf "$archive_dir"
echo "RUNTIME_KITS_DIR=$runtime_kits_dir" >> "$GITHUB_ENV"
