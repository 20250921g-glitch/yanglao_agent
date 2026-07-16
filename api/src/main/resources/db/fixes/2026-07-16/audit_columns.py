import os, re

SRC = "/root/20250921g/yanglao_agent/api/src/main/java"
DB_COLS_FILE = "/tmp/db_cols.txt"

db = {}
with open(DB_COLS_FILE) as f:
    for line in f:
        line = line.rstrip("\n")
        if not line:
            continue
        t, c = line.split("\t")
        db.setdefault(t, set()).add(c)

def snake(name):
    s = re.sub(r'(.)([A-Z][a-z]+)', r'\1_\2', name)
    s = re.sub(r'([a-z0-9])([A-Z])', r'\1_\2', s)
    return s.lower()

missing = {}
for root, _, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        path = os.path.join(root, fn)
        try:
            txt = open(path, encoding="utf-8", errors="ignore").read()
        except Exception:
            continue
        m = re.search(r'@TableName\(\s*"([^"]+)"\s*\)', txt)
        if not m:
            continue
        table = m.group(1)
        cur_ann = []
        for line in txt.splitlines():
            ls = line.strip()
            if ls.startswith("@TableField"):
                cur_ann = [ls]
                continue
            if ls.startswith("@"):
                cur_ann = [ls]
                continue
            fm = re.match(r'(?:private|public|protected)?\s+(?:final\s+|static\s+)*([\w<>\[\],\s]+?)\s+(\w+)\s*[;=]', ls)
            if fm:
                fname = fm.group(2)
                if fname == "serialVersionUID":
                    cur_ann = []
                    continue
                if any("exist" in a and "false" in a for a in cur_ann):
                    cur_ann = []
                    continue
                col = None
                for a in cur_ann:
                    vm = re.search(r'value\s*=\s*"([^"]+)"', a) or re.search(r'@TableField\(\s*"([^"]+)"', a)
                    if vm:
                        col = vm.group(1)
                        break
                if not col:
                    col = snake(fname)
                if table in db and col not in db[table]:
                    missing.setdefault(table, []).append(col)
                cur_ann = []

total = 0
for t in sorted(missing):
    cols = missing[t]
    total += len(cols)
    print("TABLE %s: %d missing -> %s" % (t, len(cols), ", ".join(cols)))
if not missing:
    print("NO_MISSING_COLUMNS")
else:
    print("=== TOTAL MISSING COLUMNS: %d across %d tables ===" % (total, len(missing)))
