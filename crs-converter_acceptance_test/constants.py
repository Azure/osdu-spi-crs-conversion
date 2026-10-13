import os
import uuid
from urllib.parse import urlparse


def _resolve_test_id():
    """Build a per-run token: MY_TEST_ID (traceability) + CI_JOB_ID (unique per job/retry).

    CIMPL CI always exports a static MY_TEST_ID (e.g. 12345). Using that alone
    collides across concurrent jobs and deletes fixtures mid-run. Always append
    CI_JOB_ID when present; fall back to a short UUID locally.
    """
    parts = []
    explicit = os.getenv("MY_TEST_ID")
    if explicit and explicit != "NOT_FOUND":
        parts.append(explicit)
    ci_job = os.getenv("CI_JOB_ID")
    if ci_job:
        parts.append(ci_job)
    if parts:
        return "_".join(parts)
    return uuid.uuid4().hex[:8]


def instance_origin(root_url):
    """Return scheme://host[:port] from a bare hostname or full instance URL."""
    root = (root_url or "").strip().rstrip("/")
    if not root:
        return None
    if "://" not in root:
        host = root.split("/")[0]
        scheme = "http" if "localhost" in host else "https"
        return f"{scheme}://{host}"
    parsed = urlparse(root)
    if not parsed.netloc:
        return None
    return f"{parsed.scheme}://{parsed.netloc}"


def service_base_url(root_url, base_url="/api/crs/converter"):
    """
    Build the CRS Converter base URL.

    Accepts a bare hostname (legacy) or a full instance endpoint (ADR-046).
    Does not double-append BASE_URL when the path is already present.
    """
    origin = instance_origin(root_url)
    if not origin:
        return None
    base = base_url or ""
    if base and not base.startswith("/"):
        base = "/" + base
    base_stripped = base.rstrip("/")

    root = (root_url or "").strip().rstrip("/")
    if "://" in root:
        parsed = urlparse(root)
        path = (parsed.path or "").rstrip("/")
        if path and (path == base_stripped or path.endswith(base_stripped)):
            return f"{parsed.scheme}://{parsed.netloc}{path}"
    return origin + base_stripped


BASE_URL = os.getenv("BASE_URL", "/api/crs/converter")
ROOT_URL = os.getenv("VIRTUAL_SERVICE_HOST_NAME")
MY_TENANT = os.getenv("MY_TENANT")
MY_REPLACE_DOMAIN = os.getenv("MY_REPLACE_DOMAIN", "NOT_FOUND")
MY_LEGAL_TAG = os.getenv("MY_LEGAL_TAG", "NOT_FOUND")
MY_TEST_ID = _resolve_test_id()

_storage_override = os.getenv("STORAGE_URL")
if _storage_override and _storage_override != "NOT_FOUND":
    STORAGE_URL = _storage_override
else:
    _origin = instance_origin(ROOT_URL)
    STORAGE_URL = f"{_origin}/api/storage/v2/records" if _origin else "NOT_FOUND"


def _normalize_legal_tags_url(url):
    """CI LEGAL_URL is often the Legal base (.../api/legal/v1/); POST needs /legaltags."""
    if not url or url == "NOT_FOUND":
        return "NOT_FOUND"
    normalized = url.strip().rstrip("/")
    if normalized.endswith("/legaltags"):
        return normalized
    return normalized + "/legaltags"


_legal_override = os.getenv("LEGAL_URL")
if _legal_override and _legal_override != "NOT_FOUND":
    LEGAL_URL = _normalize_legal_tags_url(_legal_override)
else:
    _origin = instance_origin(ROOT_URL)
    LEGAL_URL = f"{_origin}/api/legal/v1/legaltags" if _origin else "NOT_FOUND"


def legal_tag_short_name(full_or_short, data_partition_id):
    """Return Legal API create name (without partition prefix)."""
    tag = (full_or_short or "").strip()
    partition = (data_partition_id or "").strip()
    prefix = f"{partition}-"
    if partition and tag.startswith(prefix):
        return tag[len(prefix):]
    return tag
