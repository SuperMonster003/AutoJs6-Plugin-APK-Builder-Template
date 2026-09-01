#!/usr/bin/env python3
"""Validate a complete ABI Runtime Kit set and emit a deterministic build manifest."""

import argparse
import json
from pathlib import Path

from verify_runtime_kit import (
    load_rules,
    parse_host_compatibility,
    parse_template_variant,
    resolve_runtime_kit_root,
)


def canonical(value) -> str:
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(",", ":"))


def discover_runtime_kit_roots(candidate: Path) -> list[Path]:
    candidate = candidate.resolve()
    if (candidate / "runtime-kit.json").is_file():
        return [candidate]

    roots: list[Path] = []
    for child in sorted(path for path in candidate.iterdir() if path.is_dir()):
        root = resolve_runtime_kit_root(child)
        if (root / "runtime-kit.json").is_file() and root not in roots:
            roots.append(root)
    if not roots:
        raise SystemExit(f"Cannot find Runtime Kits under {candidate}")
    return roots


def shared_identity(metadata: dict) -> dict:
    contract = dict(metadata.get("contract") or {})
    contract.pop("nativeLibManifestHash", None)
    template = metadata.get("template") or {}
    default_key_store = metadata.get("defaultKeyStore") or {}
    return {
        "schemaVersion": metadata.get("schemaVersion"),
        "host": metadata.get("host") or {},
        "templatePackageName": template.get("packageName"),
        "defaultKeyStoreSha256": default_key_store.get("sha256"),
        "contractExceptNativeLibs": contract,
        "compatibility": metadata.get("compatibility") or {},
    }


def build_manifest(candidate: Path) -> dict:
    rules = load_rules()
    variant_rules = rules["templateVariant"]
    universal_variant = variant_rules["universalVariant"]
    prefix = variant_rules["variantPrefix"]
    expected_variants = [
        universal_variant,
        *(f"{prefix}{abi}" for abi in variant_rules["allowedAbis"]),
    ]

    records = []
    identities: dict[str, str] = {}
    runtime_kit_ids: set[str] = set()
    for root in discover_runtime_kit_roots(candidate):
        metadata = json.loads((root / "runtime-kit.json").read_text("utf-8"))
        variant, supported_abis = parse_template_variant(metadata, rules, require_explicit=True)
        parse_host_compatibility(metadata, rules, require_explicit=True)
        if variant in identities:
            raise SystemExit(f"Duplicate Runtime Kit variant: {variant}")

        runtime_kit_id = str(metadata.get("runtimeKitId") or "").strip()
        if not runtime_kit_id:
            raise SystemExit(f"Runtime Kit {variant} does not declare runtimeKitId")
        if runtime_kit_id in runtime_kit_ids:
            raise SystemExit(f"Duplicate Runtime Kit runtimeKitId: {runtime_kit_id}")
        runtime_kit_ids.add(runtime_kit_id)

        identities[variant] = canonical(shared_identity(metadata))
        slug = "universal" if variant == universal_variant else variant.removeprefix(prefix)
        records.append(
            {
                "variant": variant,
                "slug": slug,
                "supportedAbis": supported_abis,
                "runtimeKitDir": str(root),
                "runtimeKitId": runtime_kit_id,
            }
        )

    actual_variants = set(identities)
    expected_variant_set = set(expected_variants)
    missing = [variant for variant in expected_variants if variant not in actual_variants]
    unexpected = sorted(actual_variants - expected_variant_set)
    if missing or unexpected:
        raise SystemExit(
            "Incomplete Runtime Kit variant set: "
            f"missing={missing or 'none'}, unexpected={unexpected or 'none'}"
        )

    identity_values = set(identities.values())
    if len(identity_values) != 1:
        baseline = identities[universal_variant]
        mismatched = [variant for variant, value in identities.items() if value != baseline]
        raise SystemExit(
            "Runtime Kit variants do not share the same host/contract identity: "
            + ", ".join(mismatched)
        )

    order = {variant: index for index, variant in enumerate(expected_variants)}
    records.sort(key=lambda record: order[record["variant"]])
    universal_metadata = json.loads(
        (Path(records[0]["runtimeKitDir"]) / "runtime-kit.json").read_text("utf-8")
    )
    return {
        "schemaVersion": 1,
        "host": universal_metadata.get("host") or {},
        "variants": records,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("runtime_kits", type=Path, help="Directory containing one extracted directory per Runtime Kit")
    parser.add_argument("--output", type=Path, help="Write the build manifest to this path (stdout when omitted)")
    args = parser.parse_args()

    manifest = build_manifest(args.runtime_kits)
    content = json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(content, "utf-8")
        print(f"Runtime Kit set verified: {len(manifest['variants'])} variants -> {args.output}")
    else:
        print(content, end="")


if __name__ == "__main__":
    main()
