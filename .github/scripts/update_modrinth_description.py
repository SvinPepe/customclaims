#!/usr/bin/env python3
"""
Updates the Modrinth project description (body) using the Modrinth API.
Requires MODRINTH_TOKEN with 'Write to project data' permission.
"""

import os
import sys
import json
import urllib.request
import urllib.error

PROJECT_ID = "kOE5HGwC"
API_URL = f"https://api.modrinth.com/v2/project/{PROJECT_ID}"
DESCRIPTION_FILE = "DESCRIPTION.md"


def main():
    token = os.environ.get("MODRINTH_TOKEN") or os.environ.get("MODRINTH_API_KEY")
    if not token:
        print("[WARN] MODRINTH_TOKEN is not set. Skipping Modrinth description update.")
        return 0

    if not os.path.isfile(DESCRIPTION_FILE):
        print(f"[ERROR] Description file '{DESCRIPTION_FILE}' not found.")
        return 1

    with open(DESCRIPTION_FILE, "r", encoding="utf-8") as f:
        body_content = f.read()

    payload = json.dumps({"body": body_content}).encode("utf-8")

    req = urllib.request.Request(
        API_URL,
        data=payload,
        headers={
            "Authorization": token,
            "Content-Type": "application/json",
            "User-Agent": "SvinPepe/opac-warfare/publish-script",
        },
        method="PATCH",
    )

    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            print(f"[SUCCESS] Updated Modrinth project ({PROJECT_ID}) description! HTTP status: {status}")
            return 0
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8", errors="replace")
        print(f"[ERROR] HTTP {e.code} while updating Modrinth description: {error_body}")
        return 1
    except Exception as e:
        print(f"[ERROR] Failed to update Modrinth description: {e}")
        return 1


if __name__ == "__main__":
    sys.exit(main())
