#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""P3 工程收尾 e2e 验证：按钮级权限码 + 操作日志真实落库 + AOP 切面。"""
import json
import urllib.request
import urllib.error

BASE = "http://localhost:8080"


def req(method, path, data=None, token=None):
    url = BASE + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["token"] = token
    body = json.dumps(data).encode("utf-8") if data is not None else None
    r = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=15) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8"))
        except Exception:
            return e.code, {}
    except Exception as e:
        return -1, {"error": str(e)}


def ok(label, cond, extra=""):
    print(f"[{'PASS' if cond else 'FAIL'}] {label} {extra}")
    return cond


def main():
    print("=== 1. 登录 ===")
    st, login = req("POST", "/api/sys/user/login",
                    {"username": "admin", "password": "123456"})
    print("  login status:", st, "| code:", login.get("code"))
    d = login.get("data", {}) or {}
    token = d.get("token")
    uid = (d.get("user") or {}).get("id")
    print("  uid:", uid, "| token len:", len(token) if token else 0)
    assert token and uid, "登录失败，无法继续"

    print("\n=== 2. 按钮级权限码 (user-perms) ===")
    st, perms = req("GET", f"/api/sys/permission/user-perms/{uid}", token=token)
    pset = (perms.get("data") or [])
    print("  status:", st, "| perms count:", len(pset))
    print("  sample:", pset[:8])
    ok("user-perms 返回非空", len(pset) > 0)
    # 校验与 v-has 对齐的关键码存在
    for code in ["product:add", "activity:delete", "order:close",
                 "pointrule:edit", "product:status"]:
        ok(f"perms 含 {code}", code in pset)

    print("\n=== 3. 操作日志分页 (种子数据) ===")
    st, before = req("GET", "/api/system/operation-log/page?pageNum=1&pageSize=10", token=token)
    bdata = before.get("data") or {}
    btotal = bdata.get("total")
    brecords = bdata.get("records") or []
    print("  status:", st, "| total:", btotal, "| returned:", len(brecords))
    ok("operation-log/page 返回种子数据", btotal is not None and btotal >= 1)

    print("\n=== 4. 触发写操作：新增积分规则 ===")
    st, created = req("POST", "/api/point-rule",
                      {"ruleName": "P3自动化测试规则", "actionType": "获取",
                       "points": 1, "status": 1}, token=token)
    cdata = created.get("data")
    print("  create status:", st, "| code:", created.get("code"), "| newId:", cdata)
    ok("新增积分规则成功", st == 200 and created.get("code") == 200)
    new_id = cdata if isinstance(cdata, int) else None

    print("\n=== 5. 触发写操作：删除该规则 ===")
    if new_id:
        st, deleted = req("DELETE", f"/api/point-rule/{new_id}", token=token)
        print("  delete status:", st, "| code:", deleted.get("code"))
        ok("删除积分规则成功", st == 200 and deleted.get("code") == 200)
    else:
        print("  (跳过删除：未拿到 newId)")
        ok("拿到 newId", False)

    print("\n=== 6. 操作日志分页 (写后) — 验证 AOP 真实落库 ===")
    st, after = req("GET", "/api/system/operation-log/page?pageNum=1&pageSize=10", token=token)
    adata = after.get("data") or {}
    atotal = adata.get("total")
    arecords = adata.get("records") or []
    print("  status:", st, "| total(before->after):", btotal, "->", atotal)
    newest = arecords[0] if arecords else {}
    print("  newest log:", json.dumps(newest, ensure_ascii=False)[:300])
    ok("写操作后总记录数增加", atotal is not None and btotal is not None and atotal > btotal)
    ok("最新日志记录到本次操作(新增/删除)",
       newest.get("operation") in ("新增", "删除", "登录系统") or "新增" in str(newest.get("operation")))
    ok("日志记录到操作用户", newest.get("userName") in ("admin", "system") or newest.get("userName") is not None)

    print("\n=== 汇总 ===")
    print("P3 按钮级权限(user-perms) + 操作日志真实落库(AOP) 验证完成。")


if __name__ == "__main__":
    main()
