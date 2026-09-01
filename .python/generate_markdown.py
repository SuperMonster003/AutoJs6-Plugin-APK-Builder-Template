# -*- coding: utf-8 -*-
import json
import re
from pathlib import Path


LANGUAGE_CODES = [
    "zh-Hans",
    "zh-Hant-HK",
    "zh-Hant-TW",
    "en",
    "fr",
    "es",
    "ja",
    "ko",
    "ru",
    "ar",
]
LANGUAGE_CODE_DEFAULT = "zh-Hans"
README_COMPAT_ROOT_LANGUAGES = {"en"}
ANDROID_CHANGELOG_ALIASES = {
    "zh-Hans": ["zh", "zh-Hans"],
    "zh-Hant-HK": ["zh-rHK", "zh-Hant-HK"],
    "zh-Hant-TW": ["zh-rTW", "zh-Hant-TW"],
}
TEMPLATE_PATTERN = re.compile(r"\{\{\s*([A-Za-z0-9_$.-]+)\s*\}\}")


def project_root() -> Path:
    return Path(__file__).resolve().parents[1]


ROOT = project_root()
README_DIR = ROOT / ".readme"
CHANGELOG_DIR = ROOT / ".changelog"
ANDROID_CHANGELOG_DIR = ROOT / "app" / "src" / "main" / "assets" / "doc"


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def render_template(text: str, values: dict) -> str:
    def repl(match):
        key = match.group(1).strip()
        if key not in values:
            raise KeyError(f"Missing template value: {key}")
        return str(values[key])

    return TEMPLATE_PATTERN.sub(repl, text)


def render_dynamic(value, values: dict):
    if isinstance(value, dict):
        return {k: render_dynamic(v, values) for k, v in value.items()}
    if isinstance(value, list):
        return [render_dynamic(v, values) for v in value]
    if isinstance(value, str):
        return render_template(value, values)
    return value


def bullet_list(items):
    return "\n".join(f"- {item}" for item in items)


def markdown_link(label, url):
    return f"[{label}]({url})"


def validate_language_keys(raw_languages: dict):
    expected = list(raw_languages[LANGUAGE_CODE_DEFAULT].keys())
    for code in LANGUAGE_CODES:
        actual = list(raw_languages[code].keys())
        if actual != expected:
            raise ValueError(
                f"README locale key/order mismatch for {code}: "
                f"expected {expected}, actual {actual}"
            )


def validate_changelog_keys(raw_changelogs: dict):
    expected = list(raw_changelogs[LANGUAGE_CODE_DEFAULT].keys())
    expected_versions = list(raw_changelogs[LANGUAGE_CODE_DEFAULT]["$data"].keys())
    for code in LANGUAGE_CODES:
        actual = list(raw_changelogs[code].keys())
        if actual != expected:
            raise ValueError(
                f"Changelog locale key/order mismatch for {code}: "
                f"expected {expected}, actual {actual}"
            )
        actual_versions = list(raw_changelogs[code]["$data"].keys())
        if actual_versions != expected_versions:
            raise ValueError(
                f"Changelog version/order mismatch for {code}: "
                f"expected {expected_versions}, actual {actual_versions}"
            )


def load_languages():
    common = load_json(README_DIR / "common.json")
    raw_languages = {code: load_json(README_DIR / f"lang_{code}.json") for code in LANGUAGE_CODES}
    raw_changelogs = {code: load_json(CHANGELOG_DIR / f"lang_{code}.json") for code in LANGUAGE_CODES}
    validate_language_keys(raw_languages)
    validate_changelog_keys(raw_changelogs)

    languages = {}
    changelogs = {}
    for code in LANGUAGE_CODES:
        merged_lang = {**common, **raw_languages[code]}
        languages[code] = render_dynamic(merged_lang, merged_lang)

        raw_changelog = raw_changelogs[code]
        changelog_values = {k: v for k, v in raw_changelog.items() if k != "$data"}
        changelog_values = render_dynamic(changelog_values, {**common, **changelog_values})
        changelog_data = render_dynamic(raw_changelog["$data"], {**common, **changelog_values})
        changelogs[code] = {
            "values": changelog_values,
            "data": changelog_data,
        }
    return languages, changelogs


def format_changelog_items(changelog, limit=None):
    values = changelog["values"]
    data = changelog["data"]
    chunks = []
    for index, (version_name, item) in enumerate(data.items()):
        if limit is not None and index >= limit:
            break
        lines = [
            f"# {version_name}",
            "",
            f"###### {item['released_date']}",
            "",
        ]
        for category in ["hint", "feature", "fix", "improvement", "dependency"]:
            for text in item.get(category, []):
                label = values[f"changelog_label_{category}"]
                lines.append(f"* `{label}` {text}")
        chunks.append("\n".join(lines).rstrip())
    return "\n\n".join(chunks).rstrip() + "\n"


def build_language_list(target_code, languages):
    repo_url = languages[LANGUAGE_CODE_DEFAULT]["repo_url"]
    lines = []
    for code in LANGUAGE_CODES:
        content = languages[code]
        label = f"{content['$name']} [{code}]"
        if code == target_code:
            lines.append(f"- {label} # {content['text_current_lowercase']}")
        else:
            url = f"{repo_url}/blob/master/.readme/README-{code}.md"
            lines.append(f"- {markdown_link(label, url)}")
    return "\n".join(lines)


def build_readme_values(code, languages, changelogs):
    content = dict(languages[code])
    content["placeholder_ul_languages_all_supported"] = build_language_list(code, languages)
    content["placeholder_features"] = bullet_list(content["features"])
    content["placeholder_boundaries"] = bullet_list(content["boundaries"])
    content["placeholder_latest_release_history"] = format_changelog_items(changelogs[code], limit=3).rstrip()
    content["placeholder_read_more_in_changelog"] = markdown_link(
        content["text_changelog"],
        f"{content['repo_url']}/blob/master/app/src/main/assets/doc/CHANGELOG-{code}.md",
    )
    return content


def write_text(path: Path, text: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")
    print(f"Generated {path.relative_to(ROOT)}")


def ensure_fully_rendered(kind: str, code: str, output: str):
    residue = TEMPLATE_PATTERN.search(output)
    if residue:
        raise ValueError(f"Unresolved {kind} placeholder for {code}: {residue.group(0)}")


def generate_readmes(languages, changelogs):
    template = (README_DIR / "template_readme.md").read_text(encoding="utf-8")
    for code in LANGUAGE_CODES:
        output = render_template(template, build_readme_values(code, languages, changelogs))
        ensure_fully_rendered("README", code, output)
        path = README_DIR / f"README-{code}.md"
        write_text(path, output)
        if code == LANGUAGE_CODE_DEFAULT:
            write_text(ROOT / "README.md", output)
        if code in README_COMPAT_ROOT_LANGUAGES:
            write_text(ROOT / f"README-{code}.md", output)


def generate_changelogs(languages, changelogs):
    template = (CHANGELOG_DIR / "template_changelog.md").read_text(encoding="utf-8")
    for code in LANGUAGE_CODES:
        values = dict(languages[code])
        values["placeholder_release_history"] = format_changelog_items(changelogs[code]).rstrip()
        output = render_template(template, values)
        ensure_fully_rendered("CHANGELOG", code, output)
        names = ANDROID_CHANGELOG_ALIASES.get(code, [code])
        for name in names:
            write_text(ANDROID_CHANGELOG_DIR / f"CHANGELOG-{name}.md", output)
        if code == LANGUAGE_CODE_DEFAULT:
            write_text(ANDROID_CHANGELOG_DIR / "CHANGELOG.md", output)


def main():
    if LANGUAGE_CODE_DEFAULT not in LANGUAGE_CODES:
        raise ValueError(f"Default language code {LANGUAGE_CODE_DEFAULT!r} is not in LANGUAGE_CODES")
    languages, changelogs = load_languages()
    generate_changelogs(languages, changelogs)
    generate_readmes(languages, changelogs)


if __name__ == "__main__":
    main()
