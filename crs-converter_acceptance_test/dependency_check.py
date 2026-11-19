# coding: utf-8
"""Helpers to verify dependency versions reported by /info."""
import json
from pathlib import Path
import re

MODULE_DIR = Path(__file__).resolve().parent
DEFAULT_EXPECTATIONS_PATH = MODULE_DIR / "expected_dependencies.json"
VERSION_RE = re.compile(r"\d+(\.\d+)+")


def _is_valid_version(value) -> bool:
    return isinstance(value, str) and VERSION_RE.fullmatch(value) is not None


def load_expectations(path=None) -> dict[str, str]:
    """Read expected_dependencies.json and return a mapping of dependency name to expected version.

    A relative path is resolved against this module's directory.
    """
    target = DEFAULT_EXPECTATIONS_PATH if path is None else MODULE_DIR / path

    try:
        raw = target.read_text(encoding="utf-8")
    except OSError as exc:
        raise ValueError(f"Missing or unreadable expectations file '{target}': {exc}") from exc

    try:
        data = json.loads(raw)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON in expectations file '{target}': {exc}") from exc

    if not isinstance(data, dict):
        raise ValueError(f"Expectations file '{target}' must contain a JSON object")

    expectations: dict[str, str] = {}
    for name, entry in data.items():
        if not isinstance(entry, dict) or not isinstance(entry.get("version"), str) or not entry["version"].strip():
            raise ValueError(f"Entry '{name}' in '{target}' has no 'version'")
        expectations[name] = entry["version"]

    return expectations


def check_dependency(entries, name, expected) -> None:
    """Assert that entries contains a single valid version for name matching expected."""
    entry_list = list(entries) if entries is not None else []

    if not _is_valid_version(expected):
        raise AssertionError(
            f"Missing configuration or malformed expectation for '{name}': "
            f"expected={expected!r}, observed={entry_list!r}"
        )

    matches = [version for entry_name, version in entry_list if entry_name == name]

    if not matches:
        observed_names = [entry_name for entry_name, _ in entry_list]
        raise AssertionError(
            f"Missing dependency entry for '{name}': "
            f"expected={expected!r}, observed={observed_names!r}"
        )

    if len(matches) > 1:
        raise AssertionError(
            f"Ambiguous dependency entry for '{name}' ({len(matches)} entries found): "
            f"expected={expected!r}, observed={matches!r}"
        )

    reported = matches[0]
    if not _is_valid_version(reported):
        raise AssertionError(
            f"Empty or malformed reported version for '{name}': "
            f"expected={expected!r}, observed={reported!r}"
        )

    if reported != expected:
        raise AssertionError(
            f"Version mismatch for '{name}': expected={expected!r}, observed={reported!r}"
        )
