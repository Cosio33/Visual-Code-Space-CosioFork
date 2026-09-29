<!--suppress HtmlDeprecatedAttribute, CheckImageSize -->

<div align="center">
  <img src="./images/ic_launcher.png" alt="Visual Code Space" width="120" height="120"/>
</div>

<h1 align="center"><b>Visual Code Space</b></h1>
<p align="center"><b>A Modern Code Editor for Android</b></p>

<div align="center">
  <a href="https://github.com/Visual-Code-Space/Visual-Code-Space/actions/workflows/androidci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/Visual-Code-Space/Visual-Code-Space/androidci.yml?branch=main" alt="Android CI">
  </a>
  <a href="https://opensource.org/licenses/GPL-3.0">
    <img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License">
  </a>
</div>

<br>

<div align="center">
  <img src="images/vcspace_1.jpg" width="32%"  alt="Screenshot 1"/>
  <img src="images/vcspace_2.jpg" width="32%"  alt="Screenshot 2"/>
  <img src="images/vcspace_3.jpg" width="32%"  alt="Screenshot 3"/>
</div>
<div align="center">
  <img src="images/vcspace_4.jpg" width="32%"  alt="Screenshot 4"/>
  <img src="images/vcspace_5.jpg" width="32%"  alt="Screenshot 5"/>
  <img src="images/vcspace_6.jpg" width="32%"  alt="Screenshot 6"/>
</div>
<div align="center">
  <img src="images/vcspace_7.jpg" width="32%"  alt="Screenshot 7"/>
  <img src="images/vcspace_8.jpg" width="32%"  alt="Screenshot 8"/>
  <img src="images/vcspace_9.jpg" width="32%"  alt="Screenshot 9"/>
</div>
<br>
<em>A glimpse of Visual Code Space in action.</em>

<br>

## 🚀 Elevate Your Mobile Coding Experience

**Visual Code Space** is a cutting-edge code editor meticulously crafted for Android devices. It
empowers you to code on the go with a seamless and efficient environment. Forget about cumbersome
setups; dive straight into coding with our intuitive interface and powerful features.

## ✨ Key Features

- **⚡ Blazing Fast File Explorer:** Navigate your project directories with speed and ease.
- **🎨 Multi-Language Syntax Highlighting:** Enjoy syntax highlighting for a wide array of
  programming languages, making your code more readable and less error-prone.
- **📑 Tabbed Editor:** Manage multiple files simultaneously with our convenient tabbed interface.
- **💻 Integrated Terminal Emulator:** Execute commands directly within the app using our built-in
  terminal emulator.
- **🚫 Ad-Free Experience:** Focus on your code without any distractions.
- **🔌 Plugin Support:** Extend the functionality of Visual Code Space with custom plugins written in
  BeanShell.

## 🆕 What's New in this Fork — `vscodev2-debug.apk` (v2.0.1 / 201 debug)

> **Build:** `app/build/outputs/apk/debug/vscodev2-debug.apk` (42.4 MB) — 16/05/2026 11:57 | `applicationId: com.teixeira.vcspace.debug` | Base commit `d31c59d` (`feat: add Markdown preview`) + local changes below. Debug variant, not a release tag.

This fork extends the upstream editor with a **secure, customizable AI layer** and a new **Edit with AI** workflow. The APK you see here already contains these changes.

### ✨ Highlights

| Feature | Details |
|---------|---------|
| **🔐 Secure AI Settings** | New screen `Settings → AI` ( `SettingScreens.Ai` ). API Key stored in `EncryptedSharedPreferences` (`ai_secure_prefs`, `MasterKey.AES256_GCM`, `AES256_SIV/GCM`). Toggle `Enable Custom Configuration` to use your own key/model/prompts or fallback to `Secrets.getGenerativeAiApiKey()`. |
| **🧠 AI Abstraction** | New `core/ai/` module: `AiManager` (singleton, double-checked locking), `AiProvider` interface + `GeminiProvider` implementation, `AiResponse(text, totalTokenCount, promptTokenCount, candidatesTokenCount)`, `AiSettings` |
| **✏️ Edit with AI** | Select code in Sora/Monaco editor → text-action `Edit with AI` (icon `AutoFixHigh`) → `EditCodeDialog` (instructions + read-only selection preview) → `provider.editCode(code, instructions, language)` → `EditCodeResultDialog` → **Apply Changes** replaces the exact selection (`Content.delete/insert` with saved `cursorLeft/RightLine/Column`) |
| **🔄 Refactor** | `GenerateContentDialog`, `SoraEditor` (`explainCode`/`importComponents`), `AiResponseSheet`/`CodeExplanationSheet`/`ImportComponentsSheet` now consume `AiResponse` via `AiManager.getProvider(context)` instead of static `Gemini` object |
| **⚙️ Custom Prompts** | 5 editable system prompts: `Explain Code`, `Generate Code`, `Import Components`, `Complete Code`, `Edit Code` (placeholders `%code%`, `%prompt%`, `%language%`, `%before%`, `%after%`, `%instructions%`) + `Model Name` (`gemini-2.0-flash` default) + `Temperature` slider (0.7 default, `topK=64, topP=0.95, maxOutputTokens=65536`) |
| **📦 Dependency** | `+ androidx.security:security-crypto` (EncryptedSharedPreferences). `bsh 3.0.0-SNAPSHOT → 2.0b5` |

### 📂 Changed / New Files (real diff `git diff -w`: 12 files + 10 untracked)

**New (untracked):**
```
app/src/main/java/com/teixeira/vcspace/core/ai/AiManager.kt
app/src/main/java/com/teixeira/vcspace/core/ai/AiProvider.kt
app/src/main/java/com/teixeira/vcspace/core/ai/AiResponse.kt
app/src/main/java/com/teixeira/vcspace/core/ai/AiSettings.kt
app/src/main/java/com/teixeira/vcspace/core/ai/GeminiProvider.kt
app/src/main/java/com/teixeira/vcspace/ui/components/ai/EditCodeDialog.kt
app/src/main/java/com/teixeira/vcspace/ui/components/ai/EditCodeResultDialog.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/settings/AiSettingsScreen.kt
feature/editor/src/main/java/com/teixeira/vcspace/editor/listener/OnEditCodeListener.kt
```

**Modified (functional):**
```
app/build.gradle.kts, gradle/libs.versions.toml,
app/src/main/java/com/teixeira/vcspace/ui/components/ai/GenerateContentDialog.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/SettingScreens.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/editor/EditorScreen.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/editor/ai/AiResponseSheet.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/editor/ai/CodeExplanationSheet.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/editor/ai/ImportComponentsSheet.kt
app/src/main/java/com/teixeira/vcspace/ui/screens/settings/SettingsScreen.kt
core/resources/src/main/res/values/strings.xml (+20 strings)
feature/editor/src/main/java/com/teixeira/vcspace/editor/VCSpaceEditor.kt
feature/editor/src/main/java/com/teixeira/vcspace/editor/textaction/EditorTextActionItem.kt
```

> **Note:** `git diff` reports 442 files due to line-ending `LF → CRLF` noise. `git diff -w --stat` = **12 files, 139 ins / 26 del** = real logic change. The listed files above are the only ones with functional impact.

### 🔧 Build this APK

```bash
./gradlew assembleDebug  # -> app/build/outputs/apk/debug/app-debug.apk (rename to vscodev2-debug.apk)
# or
./gradlew assembleRelease
```

Requires `local.properties` (sdk.dir), `token.properties` / `Secrets`. Debug APK is `com.teixeira.vcspace.debug` (suffix `.debug`).

### 📦 Installation

Ready to start coding? Download the latest version of Visual Code Space from
our [releases page](https://github.com/Visual-Code-Space/Visual-Code-Space/releases)
or [telegram group](https://t.me/visualcodespace).

> **Fork APK:** This repo's debug build is `vscodev2-debug.apk` (see `app/build/outputs/apk/debug/`). For a signed release use `app-release.apk` / GitHub Releases.

## 📖 Plugin Development

### Unleash the Power of Customization

Visual Code Space supports custom plugins written in Java, allowing you to tailor the editor to
your specific needs. For detailed instructions and examples, please refer to our [Basic Plugin](docs/plugins/basic-docs.md)
documentation.

## 🤝 Contributing

We are always looking for ways to improve Visual Code Space and welcome contributions from the
community. Please see
our [CONTRIBUTING.md](https://github.com/Visual-Code-Space/Visual-Code-Space/blob/main/CONTRIBUTING.md)
for guidelines on how to get involved.

## 💖 Special Thanks

We extend our gratitude to the following projects and individuals for their invaluable
contributions:

- [Rosemoe](https://github.com/Rosemoe) for
  the [sora-editor](https://github.com/Rosemoe/sora-editor)
- [VSCode](https://github.com/microsoft/vscode) for
  the [TextMate files](https://github.com/microsoft/vscode/tree/main/extensions)
- [Termux](https://github.com/termux) for
  the [Terminal Emulator](https://github.com/termux/termux-app)
- [Akash Yadav](https://github.com/itsaky) for the
  awesome [AndroidIDE](https://github.com/AndroidIDEOfficial/AndroidIDE)

## 🧑‍💻 Contributors

<a href="https://github.com/Visual-Code-Space/Visual-Code-Space/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Visual-Code-Space/Visual-Code-Space" alt="Contributors"/>
</a>

## 📜 License

```
Visual Code Space is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Visual Code Space is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Visual Code Space.  If not, see <https://www.gnu.org/licenses/>.
```

Any violations to the license can be reported either by opening an issue or writing a mail to us
directly.