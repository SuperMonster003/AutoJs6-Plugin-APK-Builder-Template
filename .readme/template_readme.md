<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="{{ repo_url }}/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="{{ icon_alt }}" border="0" width="128" />
    </picture>
  </p>

  <p>{{ text_plugin_synopsis }}</p>

  <p>
    <a href="{{ repo_url }}/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/{{ repo_slug }}?label=Release"/></a>
    <a href="{{ repo_url }}/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/{{ repo_slug }}?color=A24232&label=Issues"/></a>
    <a href="{{ license_url }}"><img alt="GitHub License" src="https://img.shields.io/github/license/{{ repo_slug }}?color=534BAE&label=License"/></a>
  </p>
</div>

******

### {{ h3_languages_with_ascii }}

******

{{ p_languages_all_supported_for_readme }}:

{{ placeholder_ul_languages_all_supported }}

******

### {{ h3_introduction }}

******

{{ p_introduction_plugin_managed }}

******

### {{ h3_how_it_works }}

******

{{ p_how_it_works_plugin_managed }}

******

### {{ h3_functions }}

******

{{ placeholder_features }}

******

### {{ h3_quick_start }}

******

- **{{ quick_start_install_title }}**: {{ quick_start_install }}
- **{{ quick_start_use_title }}**: {{ quick_start_use }}
- **{{ quick_start_verify_title }}**: {{ quick_start_verify }}
- **{{ quick_start_debug_title }}**: {{ quick_start_debug }}

******

### {{ h3_boundaries }}

******

{{ p_boundaries_intro }}:

{{ placeholder_boundaries }}

******

### {{ h3_faq }}

******

{{ p_matrix_faq }}

{{ p_faq_plugin_managed }}

******

### {{ h3_reference }}

******

{{ p_reference_intro }}

#### {{ h4_reference_runtime_kit }}

{{ p_runtime_kit }}:

```text
{{ runtime_kit_files }}
```

#### {{ h4_reference_discovery }}

{{ p_reference_discovery }}:

```text
{{ discovery_ids }}
```

#### {{ h4_reference_local_build }}

{{ p_local_build_runtime_kit }}:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

{{ p_local_build_with_runtime_kit }}:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

{{ p_local_build_with_unpacked_kit }}:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### {{ h4_reference_release_flow }}

{{ p_release_flow_intro }}:

```text
{{ release_flow }}
```

#### {{ h4_reference_signing }}

{{ p_signing_intro }}:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

{{ p_local_signing_intro }}:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### {{ h3_roadmap }}

******

{{ p_roadmap_plugin_managed }}

- [{{ text_link_roadmap }}]({{ repo_url }}/blob/master/ROADMAP.md)

******

### {{ h3_release_history }}

******

{{ placeholder_latest_release_history }}

##### {{ h5_for_more_release_history }}

* {{ placeholder_read_more_in_changelog }}

******

### {{ h3_license }}

******

{{ p_license }}

- [Mozilla Public License 2.0]({{ repo_url }}/blob/master/LICENSE)

******

### {{ h3_resource_layout }}

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

{{ p_resource_layout }}.

******

### {{ h3_links }}

******

- {{ text_link_autojs6_repo }}: {{ autojs6_repo_url }}
- {{ text_link_autojs6_docs }}: {{ autojs6_docs_url }}
