import xml.etree.ElementTree as ET
tree = ET.parse(r'C:\Users\Lenovo\Desktop\iStudySpot\backend\istudyspot-backend\target\site\jacoco\jacoco.xml')
root = tree.getroot()

# Get per-class results
classes = []
for pkg in root.findall('.//package'):
    pkg_name = pkg.get('name').replace('com/ycyu/istudyspotbackend/', '')
    for cls in pkg.findall('class'):
        cls_name = cls.get('name')
        for counter in cls.findall('counter'):
            if counter.get('type') == 'INSTRUCTION':
                missed = int(counter.get('missed'))
                covered = int(counter.get('covered'))
                total = missed + covered
                if total > 0:
                    rate = 100 * covered / total
                    classes.append((pkg_name, cls_name, missed, covered, total, rate))

# Sort by coverage ascending
classes.sort(key=lambda x: x[5])

# Show 0% and low coverage
print("=== 0% COVERAGE (TOP PRIORITY) ===")
for pkg, cls, m, c, t, r in classes:
    if r == 0:
        print(f"  {pkg}/{cls:<30} {t:>4} instructions")

print(f"\n=== LOW COVERAGE (< 50%) ===")
for pkg, cls, m, c, t, r in classes:
    if 0 < r < 50:
        print(f"  {pkg}/{cls:<30} {covered}/{total} = {r:.1f}%")

# Per-package summary
print(f"\n=== PACKAGE SUMMARY ===")
pkgs = {}
for pkg, cls, m, c, t, r in classes:
    if pkg not in pkgs:
        pkgs[pkg] = [0, 0]
    pkgs[pkg][0] += c
    pkgs[pkg][1] += t

for name in sorted(pkgs.keys()):
    c, t = pkgs[name]
    print(f"  {name:<40} {c}/{t} = {100*c/t:.1f}%")

# Overall
c_all = sum(v[0] for v in pkgs.values())
t_all = sum(v[1] for v in pkgs.values())
print(f"\n  {'OVERALL':<40} {c_all}/{t_all} = {100*c_all/t_all:.1f}%")