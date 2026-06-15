import re, xml.etree.ElementTree as ET
with open(r'f:\Scylier-Project\active\school\iStudySpot\uit.xml', 'r', encoding='utf-8') as f:
    content = f.read()
content = re.sub(r'[\x00-\x08\x0b\x0c\x0e-\x1f]', '', content)
root = ET.fromstring(content)
for node in root.iter():
    t = node.get('text', '')
    d = node.get('content-desc', '')
    b = node.get('bounds', '')
    if '预约座位' in t or '预约座位' in d:
        print(f'MATCH_BOOK text=[{t}] desc=[{d}] bounds={b}')
    if '我的' in t or '我的' in d:
        print(f'MATCH_MINE text=[{t}] desc=[{d}] bounds={b}')
    if '首页' in t or '首页' in d:
        print(f'MATCH_HOME text=[{t}] desc=[{d}] bounds={b}')
print('--- Search done ---')
