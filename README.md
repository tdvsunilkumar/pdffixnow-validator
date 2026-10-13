# PDFFixNow Desktop (Windows)

<p align="center">
  <strong>Fast, Secure, Offline PDF/A Validation for Windows</strong><br>
  Powered by the industry-standard veraPDF engine. Your documents never leave your computer.
</p>

<p align="center">
  <a href="https://www.pdffixnow.com"><strong>Official Website: https://www.pdffixnow.com</strong></a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Windows%2010%20%7C%2011%20(64--bit)-0078D6?logo=windows" alt="Platform: Windows" />
  <img src="https://img.shields.io/badge/Engine-veraPDF%20(MPL--2.0)-4CAF50" alt="Engine: veraPDF" />
  <img src="https://img.shields.io/badge/Privacy-100%25%20Offline-green" alt="Privacy: 100% Offline" />
  <img src="https://img.shields.io/badge/Java%20Runtime-Bundled%20(No%20Java%20Setup%20Required)-orange" alt="Java: Bundled" />
  <img src="https://img.shields.io/badge/Version-1.0.0-blue" alt="Version 1.0.0" />
</p>

---

## 🌐 Visit Our Website

Looking for online PDF conversion, repair, and validation tools?  
Visit our official website: **[https://www.pdffixnow.com](https://www.pdffixnow.com)**

* **Web Tools & Services:** [https://www.pdffixnow.com](https://www.pdffixnow.com)
* **Latest Desktop Releases:** [https://www.pdffixnow.com/downloads/](https://www.pdffixnow.com)
* **Desktop App Update Feed:** `https://www.pdffixnow.com/downloads/latest.json`

---

## 📖 Overview

**PDFFixNow Desktop** is a lightweight, high-performance Windows desktop application designed to validate PDF files against official **ISO PDF/A archiving standards** (PDF/A-1, PDF/A-2, PDF/A-3, and PDF/A-4).

### 🔒 100% Offline & Confidential
Unlike web-based validators, **PDFFixNow Desktop runs completely offline**. All PDF parsing and conformance checking are executed locally on your workstation using the embedded veraPDF library. Your confidential files, contracts, invoices, and legal documents are **never uploaded to any server or cloud**.

---

## ✨ Features

- **Drag & Drop Workflow**: Drop individual PDF files or entire nested directories into the app.
- **Auto-Detection & Profile Selection**: Automatically detects declared PDF/A profiles from metadata, or forces validation against specific ISO standards.
- **Multi-Threaded Batch Processing**: Configurable multi-core parallel processing (1–8 threads) to validate thousands of documents in seconds.
- **In-Depth Failure Diagnostics**: Pinpoints the exact ISO clause violation, test number, human-readable error explanation, and the specific object location inside the PDF.
- **Comprehensive Reports**: Export results to formatted **HTML reports** (with expandable failure lists) or **CSV spreadsheets** for Excel and auditing pipelines.
- **Standalone Runtime**: Bundles its own self-contained Java runtime. End users do not need Java installed.

---

## 🚀 How to Use PDFFixNow Desktop

Follow these simple steps to validate your PDF files:

### Step 1: Launch the Application
- Open **PDFFixNow** from your Start Menu, Desktop shortcut, or run the portable executable (`PDFFixNow.exe`).

### Step 2: Add PDF Files or Folders
You have three convenient ways to load files:
1. **Drag & Drop**: Drag files or folders directly from Windows Explorer and drop them anywhere onto the application window.
2. **Add Files**: Click the **Add files...** button on the toolbar to select one or more `.pdf` files.
3. **Add Folder**: Click the **Add folder...** button to select an entire folder. PDFFixNow will automatically scan the folder and all its subfolders recursively for PDF files.

> **Tip:** You can right-click any item in the list to **"Show in folder"** or **"Remove from list"**. Use **Clear list** to start fresh.

### Step 3: Choose Validation Profile
In the **"Validate against:"** drop-down menu on the top toolbar:
- **Auto-detect (Recommended)**: Reads the embedded XMP metadata of each PDF and validates it against whichever PDF/A profile it claims to conform to.
- **Specific Profile**: Select a specific standard if you need to enforce compliance (e.g., `PDF/A-1b`, `PDF/A-2b`, `PDF/A-3b`, `PDF/A-4`).

### Step 4: Configure Performance (Threads)
- Adjust the **Threads** spinner (1 to 8) to set how many files are validated simultaneously.
- PDFFixNow defaults to an optimal thread count based on your processor cores.

### Step 5: Start Validation
- Click the blue **Validate** button.
- Watch real-time progress in the status bar at the bottom.
- You can click **Cancel** at any time to halt remaining files in the queue.

### Step 6: Review Results & Rule Failures
Once validation completes, review the status column:
- 🟢 **Valid**: The document fully complies with the target PDF/A standard.
- 🔴 **Invalid**: The document fails one or more PDF/A specification rules.
- 🟠 **Error**: The file could not be parsed or read (e.g. corrupt or password-protected file).

**Inspect Rule Failures:**
- Click on any row in the upper results table.
- The lower **Rule Failures Panel** will immediately display:
  - **Clause**: The exact ISO specification clause (e.g., `6.2.2`, `6.4`).
  - **Test**: The veraPDF rule test identifier.
  - **Description**: Clear explanation of what failed (e.g., *DeviceRGB used with no OutputIntent*, *Font not embedded*).
  - **Location in PDF**: Exact object reference or dictionary path where the error occurred.

### Step 7: Export Audit Reports
Need to share findings with clients, team members, or compliance auditors?
1. Click **Export report...** on the toolbar.
2. Choose your preferred export format:
   - **HTML Report (`.html`)**: Beautiful, self-contained interactive report that opens in any web browser, complete with summary statistics and expandable failure details.
   - **CSV Spreadsheet (`.csv`)**: Formatted with UTF-8 BOM for instant, seamless opening in Microsoft Excel or importing into automated QA pipelines.

---

## 📋 Supported PDF/A Profiles

PDFFixNow supports the complete suite of ISO PDF/A standards:

| Standard | Profile Code | Description | Typical Use Case |
| :--- | :--- | :--- | :--- |
| **PDF/A-1** (ISO 19005-1) | `1a` / `1b` | Level B (Visual preservation) & Level A (Accessible structure) | Long-term archiving of basic scanned/electronic documents |
| **PDF/A-2** (ISO 19005-2) | `2a` / `2b` / `2u` | Support for JPEG2000, transparency, layers, Unicode (`2u`) | Modern documents with graphics, layers, or multi-page formats |
| **PDF/A-3** (ISO 19005-3) | `3a` / `3b` / `3u` | Allows embedding arbitrary file attachments (XML, CSV, CAD) | Electronic invoicing (ZUGFeRD, Factur-X) and hybrid records |
| **PDF/A-4** (ISO 19005-4) | `4` / `4e` / `4f` | PDF 2.0 based, Engineering (`4e`), Embedded files (`4f`) | Modern archiving based on ISO 32000-2 (PDF 2.0) |

---

## 💻 System Requirements

- **Operating System**: Windows 10 or Windows 11 (64-bit)
- **Java**: **Not required!** The installer and portable versions bundle their own lightweight, secure OpenJDK runtime.
- **Disk Space**: ~150 MB

---

## 🛠️ For Developers: Building from Source

If you want to build or modify PDFFixNow Desktop on your development machine:

### Prerequisites
1. **JDK 21+** (e.g. [Eclipse Temurin 21](https://adoptium.net/)) — ensure `java -version` and `jpackage --version` work from PowerShell or Command Prompt.
2. **Maven 3.9+** — `mvn -v`.
3. **WiX Toolset 3.11+** — Required only if generating the `.exe` installer via `build.bat`. (Not needed for `build-portable.bat`).

### Build Scripts
- **Test in development**:
  ```bat
  run-dev.bat
  ```
- **Build portable release** (`target\dist\PDFFixNow\PDFFixNow.exe`):
  ```bat
  build-portable.bat
  ```
- **Build full Windows installer** (`target\dist\PDFFixNow-1.0.0.exe`):
  ```bat
  build.bat
  ```
- **Sign installer** (optional, using your code-signing certificate):
  ```bat
  signtool sign /fd SHA256 /tr http://timestamp.digicert.com /td SHA256 /a target\dist\PDFFixNow-1.0.0.exe
  ```

### Release Checklist
- Check for updated stable veraPDF releases at [Maven Central: veraPDF validation-model](https://search.maven.org/artifact/org.verapdf/validation-model) and update `pom.xml` if desired.
- Test against the public [veraPDF Test Corpus](https://github.com/veraPDF/veraPDF-corpus).
- Publish `latest.json` on [https://www.pdffixnow.com/downloads/latest.json](https://www.pdffixnow.com) to provide automatic in-app update notifications:
  ```json
  { "version": "1.0.1", "url": "https://www.pdffixnow.com/downloads/PDFFixNow-1.0.1.exe" }
  ```
- Maintain `--win-upgrade-uuid 6c1d2a5e-3b7f-4e0a-9d52-8f41c7a0b1e3` in `build.bat` unchanged so subsequent releases seamlessly upgrade older installations.

### Project Layout
- `src/main/java/com/pdffixnow/desktop/App.java`: Main JavaFX application window, toolbar, results table, failure details pane, and status bar.
- `src/main/java/com/pdffixnow/desktop/service/ValidationService.java`: veraPDF validator integration with auto-detect and profile loading.
- `src/main/java/com/pdffixnow/desktop/service/ReportService.java`: HTML and CSV report generation.
- `src/main/java/com/pdffixnow/desktop/service/UpdateService.java`: Non-intrusive background update check against pdffixnow.com.
- `src/main/java/com/pdffixnow/desktop/model/`: Result records, table row models, and profile definitions.
- `installer/`: WiX configuration, installer banner, dialog bitmaps, and license text.

---

## 📜 Third-Party Licenses & Attribution

- **veraPDF**: Core PDF/A validation is powered by [veraPDF](https://verapdf.org), Copyright the veraPDF Consortium / Open Preservation Foundation, licensed under the [Mozilla Public License 2.0 (MPL-2.0)](https://www.mozilla.org/MPL/2.0/). Source code is available at [https://github.com/veraPDF](https://github.com/veraPDF).
- **OpenJFX / OpenJDK**: Distributed under GPLv2 + Classpath Exception.

---

## 📬 Contact & Support

- **Website**: [https://www.pdffixnow.com](https://www.pdffixnow.com)
- **Feedback & Issues**: Submit issues or feature requests on GitHub or contact us via our website.
