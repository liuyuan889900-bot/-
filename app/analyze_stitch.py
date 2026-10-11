import urllib.request
import re
import json

url = "https://app-companion-430619.appspot.com/assets/index-5TlmsyD4.js"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req) as resp:
    js = resp.read().decode("utf-8")

print("JS length:", len(js))
endpoints = set(re.findall(r'["\'`](/(?:api|v1|projects|screens)[^"\'`\s]*)["\'`]', js))
for ep in sorted(endpoints):
    if len(ep) < 60:
        print("EP:", ep)

# Also search for "getProject" or "project" api calls
matches = set(re.findall(r'(\w*project\w*)', js, re.IGNORECASE))
print("Project identifiers:", [m for m in matches if len(m) < 25][:20])
