import urllib.request
import re

url = "https://app-companion-430619.appspot.com/assets/index-5TlmsyD4.js"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req) as resp:
    js = resp.read().decode("utf-8")

print("JS length:", len(js))
endpoints = set(re.findall(r'["\'`](/(?:api|v1|projects|screens)[^"\'`\s]*)["\'`]', js))
for ep in sorted(endpoints):
    if len(ep) < 80:
        print("EP:", ep)

# Search for fetches or axios or rpc
for m in re.finditer(r'fetch\([\'"`]([^\'"`]+)[\'"`]', js):
    print("Fetch:", m.group(1))

# Find routes in react router
routes = re.findall(r'path:\s*[\'"`]([^\'"`]+)[\'"`]', js)
print("Routes:", routes)
