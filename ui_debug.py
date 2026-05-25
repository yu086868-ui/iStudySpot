import subprocess, time, os, re, xml.etree.ElementTree as ET

print('Step 1: start app', flush=True)
subprocess.run('adb shell am start -n com.example.scyiler.istudyspot/com.example.scylier.istudyspot.MainActivity', shell=True, capture_output=True, text=True, timeout=10)
time.sleep(5)

print('Step 2: dump ui', flush=True)
r = subprocess.run('adb shell uiautomator dump /sdcard/uit.xml', shell=True, capture_output=True, text=True, timeout=15)
print(f'  dump: {r.stdout.strip()[:100]}', flush=True)

print('Step 3: pull file', flush=True)
r = subprocess.run('adb pull /sdcard/uit.xml uit.xml', shell=True, capture_output=True, text=True, timeout=10)
print(f'  pull: {r.stdout.strip()[:100]}', flush=True)

print('Step 4: parse xml', flush=True)
with open('uit.xml', 'r', encoding='utf-8') as f:
    content = f.read()
content = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f]', '', content)
root = ET.fromstring(content)
print(f'  parsed OK, root tag: {root.tag}', flush=True)

print('Step 5: find elements', flush=True)
for node in root.iter():
    t = node.get('text', '')
    d = node.get('content-desc', '')
    if t:
        print(f'  TEXT: {t[:60]}', flush=True)
    if d and d.strip():
        print(f'  DESC: {d[:60]}', flush=True)

print('DONE', flush=True)
