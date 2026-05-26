import subprocess
import re
import time
import sys
import os
import xml.etree.ElementTree as ET
import warnings

warnings.filterwarnings("ignore", category=DeprecationWarning)
sys.stdout.reconfigure(line_buffering=True)

LOG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "test_result.txt")
_log_fh = None

def get_log_fh():
    global _log_fh
    if _log_fh is None:
        _log_fh = open(LOG_FILE, "w", encoding="utf-8")
    return _log_fh

PACKAGE = "com.example.scyiler.istudyspot"
DUMP_REMOTE = "/sdcard/uidump_auto.xml"
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DUMP_LOCAL = os.path.join(SCRIPT_DIR, "uidump_auto.xml")

if sys.platform == "win32":
    CREATE_NO_WINDOW = 0x08000000
else:
    CREATE_NO_WINDOW = 0

results = {}
_ui_cache = None
_ui_cache_time = 0

def adb(args, timeout=15):
    try:
        proc = subprocess.Popen(
            ["adb"] + args,
            stdin=subprocess.DEVNULL,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            creationflags=CREATE_NO_WINDOW
        )
        stdout, stderr = proc.communicate(timeout=timeout)
        return stdout.decode("utf-8", errors="replace").strip()
    except subprocess.TimeoutExpired:
        proc.kill()
        try:
            proc.communicate(timeout=2)
        except:
            pass
        return ""
    except Exception:
        return ""

def adb_shell(cmd, timeout=15):
    return adb(["shell", cmd], timeout=timeout)

def tap(x, y):
    global _ui_cache_time
    adb_shell(f"input tap {int(x)} {int(y)}")
    _ui_cache_time = 0

def swipe(x1, y1, x2, y2, duration=300):
    global _ui_cache_time
    adb_shell(f"input swipe {int(x1)} {int(y1)} {int(x2)} {int(y2)} {duration}")
    _ui_cache_time = 0

def type_text(text):
    safe = text.replace(" ", "%s")
    for ch, esc in [("&", "\\&"), ("<", "\\<"), (">", "\\>"), ("(", "\\("),
                     (")", "\\)"), (";", "\\;"), ("'", "\\'"), ('"', '\\"'),
                     ("|", "\\|"), ("^", "\\^")]:
        safe = safe.replace(ch, esc)
    adb_shell(f"input text {safe}")

def press_back():
    global _ui_cache_time
    adb_shell("input keyevent 4")
    _ui_cache_time = 0
    time.sleep(0.5)

def press_enter():
    global _ui_cache_time
    adb_shell("input keyevent 66")
    _ui_cache_time = 0
    time.sleep(0.5)

def scroll_down():
    swipe(540, 1800, 540, 600, 300)
    time.sleep(1)

def parse_bounds(bounds_str):
    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds_str)
    if m:
        return int(m.group(1)), int(m.group(2)), int(m.group(3)), int(m.group(4))
    return None

def get_center(node):
    bounds = node.get("bounds", "")
    b = parse_bounds(bounds)
    if b is not None:
        return (b[0] + b[2]) // 2, (b[1] + b[3]) // 2
    return None

def dump_ui(max_retries=2, use_cache=True):
    global _ui_cache, _ui_cache_time
    if use_cache and _ui_cache is not None and (time.time() - _ui_cache_time) < 2.0:
        return _ui_cache
    for attempt in range(max_retries + 1):
        try:
            time.sleep(0.3)
            r = adb_shell(f"uiautomator dump {DUMP_REMOTE}", timeout=10)
            if "dumped" not in r.lower() and "hierchary" not in r.lower():
                if attempt < max_retries:
                    time.sleep(1)
                    continue
            adb(["pull", DUMP_REMOTE, DUMP_LOCAL], timeout=10)
            if not os.path.exists(DUMP_LOCAL):
                if attempt < max_retries:
                    time.sleep(1)
                    continue
            with open(DUMP_LOCAL, "r", encoding="utf-8") as f:
                content = f.read()
            if len(content) < 100:
                if attempt < max_retries:
                    time.sleep(1)
                    continue
            content = re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f]", "", content)
            root = ET.fromstring(content)
            text_count = sum(1 for n in root.iter() if n.get("text", "") or n.get("content-desc", ""))
            if text_count < 3:
                if attempt < max_retries:
                    time.sleep(1)
                    continue
            _ui_cache = root
            _ui_cache_time = time.time()
            return root
        except Exception:
            if attempt < max_retries:
                time.sleep(1)
                continue
    return None

def find_element(root, text=None, desc=None, clickable=None, partial=False):
    if root is None:
        return None
    for node in root.iter():
        match = True
        if text is not None:
            t = node.get("text", "")
            match = match and (text in t if partial else text == t)
        if desc is not None:
            d = node.get("content-desc", "")
            match = match and (desc in d if partial else desc == d)
        if clickable is not None:
            c = node.get("clickable", "false") == "true"
            match = match and clickable == c
        if match:
            return node
    return None

def find_all(root, text=None, desc=None, clickable=None, partial=False):
    if root is None:
        return []
    results_list = []
    for node in root.iter():
        match = True
        if text is not None:
            t = node.get("text", "")
            match = match and (text in t if partial else text == t)
        if desc is not None:
            d = node.get("content-desc", "")
            match = match and (desc in d if partial else desc == d)
        if clickable is not None:
            c = node.get("clickable", "false") == "true"
            match = match and clickable == c
        if match:
            results_list.append(node)
    return results_list

def tap_element(text=None, desc=None, clickable=None, partial=False, retries=2):
    for attempt in range(retries + 1):
        root = dump_ui()
        if root is None:
            if attempt < retries:
                time.sleep(1)
                continue
            return False
        node = find_element(root, text=text, desc=desc, clickable=clickable, partial=partial)
        if node is None:
            if attempt < retries:
                time.sleep(1)
                continue
            return False
        c = get_center(node)
        if c is not None:
            tap(c[0], c[1])
            return True
    return False

def wait_for_element(text=None, desc=None, timeout=10, interval=2, partial=False):
    start = time.time()
    while time.time() - start < timeout:
        root = dump_ui()
        if root is not None:
            node = find_element(root, text=text, desc=desc, partial=partial)
            if node is not None:
                return node
        time.sleep(interval)
    return None

def element_exists(text=None, desc=None, partial=False):
    root = dump_ui()
    if root is None:
        return False
    return find_element(root, text=text, desc=desc, partial=partial) is not None

def clear_log():
    adb(["logcat", "-c"], timeout=5)

def get_logs(keywords, count=300):
    output = adb(["logcat", "-d", "-t", str(count)], timeout=10)
    lines = output.split("\n")
    return [l for l in lines if any(k in l for k in keywords)]

def section(title):
    msg = f"\n{'='*60}\n  {title}\n{'='*60}"
    print(msg, flush=True)
    get_log_fh().write(msg + "\n")
    get_log_fh().flush()

def result(name, ok, detail=""):
    s = "PASS" if ok else "FAIL"
    msg = f"  [{s}] {name}" + (f" - {detail}" if detail else "")
    print(msg, flush=True)
    get_log_fh().write(msg + "\n")
    get_log_fh().flush()

def info(msg):
    line = f"  [INFO] {msg}"
    print(line, flush=True)
    get_log_fh().write(line + "\n")
    get_log_fh().flush()

def warn(msg):
    line = f"  [WARN] {msg}"
    print(line, flush=True)
    get_log_fh().write(line + "\n")
    get_log_fh().flush()

def launch_app():
    adb_shell(f"monkey -p {PACKAGE} -c android.intent.category.LAUNCHER 1")
    time.sleep(3)

def go_home_tab():
    tap_element(text="首页")
    time.sleep(1.5)

def go_profile_tab():
    tap_element(text="我的")
    time.sleep(1.5)

def go_rules_tab():
    tap_element(text="规则")
    time.sleep(1.5)

def go_more_tab():
    tap_element(text="更多")
    time.sleep(1.5)

def go_back_until(text=None, desc=None, max_attempts=5, partial=False):
    for _ in range(max_attempts):
        if text and element_exists(text=text, partial=partial):
            return True
        if desc and element_exists(desc=desc, partial=partial):
            return True
        if not text and not desc:
            return True
        press_back()
        time.sleep(1)
    return False

def tap_first_clickable_in_area(y_min=400, y_max=2000):
    root = dump_ui()
    if root is None:
        return False
    for node in root.iter():
        if node.get("clickable") == "true":
            b = parse_bounds(node.get("bounds", ""))
            if b is not None and y_min < b[1] and b[3] < y_max:
                c = get_center(node)
                if c is not None:
                    tap(c[0], c[1])
                    return True
    return False

def check_prerequisites():
    output = adb(["devices"], timeout=5)
    lines = [l for l in output.split("\n") if l.strip() and "List" not in l]
    if not lines:
        print("ERROR: No emulator/device connected!")
        return False
    output = adb_shell(f"pm list packages | grep {PACKAGE}", timeout=5)
    if PACKAGE not in output:
        print(f"ERROR: App {PACKAGE} not installed!")
        return False
    return True

def ensure_adb_alive():
    output = adb(["devices"], timeout=5)
    if output and "device" in output:
        return
    info("adb not responding, restarting...")
    try:
        subprocess.run(
            ["taskkill", "/F", "/IM", "adb.exe"],
            stdin=subprocess.DEVNULL, stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL, timeout=5
        )
    except:
        pass
    time.sleep(2)
    adb(["start-server"], timeout=10)
    time.sleep(2)

TEST_TS = int(time.time()) % 100000
TEST_USER = f"autotest{TEST_TS}"
TEST_PASS = "Test123456"
TEST_NICK = f"Auto{TEST_TS}"

def test_01_app_launch():
    section("TEST 1: 应用启动与首页")
    launch_app()
    time.sleep(3)

    node = wait_for_element(text="预约座位", timeout=10, partial=True)
    ok = node is not None
    result("应用启动-首页加载", ok, "预约座位可见" if ok else "首页未加载")
    results["app_launch"] = ok

    if ok:
        root = dump_ui()
        for elem in ["预约座位", "签到", "场馆导览", "我的预约", "学习记录", "AI咨询", "通知提醒"]:
            found = find_element(root, text=elem, partial=True) is not None
            result(f"首页元素-{elem}", found)

    root = dump_ui()
    if root is not None:
        for nav in ["首页", "规则", "更多", "我的"]:
            found = find_element(root, text=nav) is not None
            result(f"底部导航-{nav}", found)

def test_02_studyroom_list():
    section("TEST 2: 自习室列表")
    clear_log()

    if not tap_element(text="预约座位", partial=True):
        warn("未找到预约座位按钮")
        return
    info("点击'预约座位'")
    time.sleep(3)

    node = wait_for_element(partial=True, text="自习室", timeout=10)
    ok = node is not None
    result("自习室列表加载", ok, "列表可见" if ok else "列表未加载")
    results["studyroom_list"] = ok

    if ok:
        root = dump_ui()
        rooms = find_all(root, partial=True, text="自习室")
        info(f"找到 {len(rooms)} 个自习室相关元素")

        logs = get_logs(["studyrooms", "OkHttp"])
        api_ok = any("200" in l and "studyrooms" in l for l in logs)
        result("自习室API调用", api_ok, "GET /api/studyrooms -> 200" if api_ok else "未检测到")
        results["studyroom_api"] = api_ok

def test_03_seat_map():
    section("TEST 3: 座位地图")

    root = dump_ui()
    room_tapped = False
    clickable_items = find_all(root, clickable=True)
    for item in clickable_items:
        b = parse_bounds(item.get("bounds", ""))
        if b is not None and 400 < b[1] and b[3] < 2000:
            text_val = item.get("text", "")
            if "自习室" in text_val or "收藏" in item.get("content-desc", ""):
                continue
            c = get_center(item)
            if c is not None:
                tap(c[0], c[1])
                info(f"点击自习室卡片 ({c[0]},{c[1]})")
                room_tapped = True
                break

    if not room_tapped and clickable_items:
        for item in clickable_items:
            b = parse_bounds(item.get("bounds", ""))
            if b is not None and 400 < b[1] and b[3] < 2000:
                c = get_center(item)
                if c is not None:
                    tap(c[0], c[1])
                    info(f"点击列表项 ({c[0]},{c[1]})")
                    room_tapped = True
                    break

    if not room_tapped:
        warn("未找到可点击的自习室卡片")
        tap(540, 600)

    time.sleep(3)

    node = wait_for_element(partial=True, text="座位", timeout=10)
    ok = node is not None
    result("座位地图加载", ok, "座位信息可见" if ok else "座位地图未加载")
    results["seat_map"] = ok

    if ok:
        root = dump_ui()
        for keyword in ["空闲", "已占", "总座位"]:
            found = find_element(root, partial=True, text=keyword) is not None
            if found:
                info(f"座位图例可见: {keyword}")
                break

        logs = get_logs(["seats", "studyrooms"])
        seat_api = any("200" in l and "seats" in l for l in logs)
        result("座位API调用", seat_api, "GET /api/studyrooms/{id}/seats -> 200" if seat_api else "未检测到")
        results["seat_api"] = seat_api

def test_04_login():
    section("TEST 4: 登录/注册")
    go_back_until(text="预约座位", partial=True, max_attempts=3)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)
    go_profile_tab()
    time.sleep(2)

    if tap_element(text="点击登录", partial=True):
        info("点击'点击登录'")
    elif tap_element(text="登录", partial=True):
        info("点击'登录'入口")
    else:
        warn("未找到登录入口，尝试直接点击区域")
        tap(540, 900)
    time.sleep(2)

    username_node = wait_for_element(text="用户名", timeout=5, partial=True)
    if username_node is not None:
        c = get_center(username_node)
        if c is not None:
            tap(c[0], c[1])
            time.sleep(0.5)
            type_text(TEST_USER)
            info(f"输入用户名: {TEST_USER}")
    time.sleep(0.5)

    root = dump_ui()
    password_node = find_element(root, text="密码", partial=True)
    if password_node is not None:
        c = get_center(password_node)
        if c is not None:
            tap(c[0], c[1])
            time.sleep(0.5)
            type_text(TEST_PASS)
            info(f"输入密码: {TEST_PASS}")
    time.sleep(0.5)

    clear_log()
    if tap_element(text="登录"):
        info("点击'登录'按钮")
    time.sleep(3)

    root = dump_ui()
    has_error = find_element(root, partial=True, text="错误") is not None or \
                find_element(root, partial=True, text="失败") is not None or \
                find_element(root, partial=True, text="不存在") is not None

    if has_error:
        warn("登录失败，尝试注册新账号")
        press_back()
        time.sleep(1)

        if tap_element(text="注册新账号", partial=True):
            info("点击'注册新账号'")
        time.sleep(2)

        for label, value in [("用户名", TEST_USER), ("昵称", TEST_NICK),
                              ("密码", TEST_PASS), ("确认密码", TEST_PASS)]:
            node = find_element(dump_ui(), text=label, partial=True)
            if node is not None:
                c = get_center(node)
                if c is not None:
                    tap(c[0], c[1])
                    time.sleep(0.3)
                    type_text(value)
                    info(f"输入{label}: {value}")
                    time.sleep(0.3)

        clear_log()
        if tap_element(text="注册"):
            info("点击'注册'按钮")
        time.sleep(3)

        if element_exists(text="登录"):
            info("注册成功，返回登录页")
            for label, value in [("用户名", TEST_USER), ("密码", TEST_PASS)]:
                node = find_element(dump_ui(), text=label, partial=True)
                if node is not None:
                    c = get_center(node)
                    if c is not None:
                        tap(c[0], c[1])
                        time.sleep(0.3)
                        type_text(value)
                        time.sleep(0.3)
            tap_element(text="登录")
            time.sleep(3)

    go_profile_tab()
    time.sleep(2)
    root = dump_ui()
    click_login_gone = find_element(root, text="点击登录") is None
    user_visible = find_element(root, text=TEST_USER) is not None or \
                   find_element(root, text=TEST_NICK) is not None
    logged_in = click_login_gone or user_visible

    result("登录成功", logged_in, f"用户 {TEST_USER} 已登录" if logged_in else "登录可能失败")
    results["login"] = logged_in

    logs = get_logs(["auth/login", "auth/register"])
    login_api = any("200" in l and "login" in l for l in logs)
    register_api = any("200" in l and "register" in l for l in logs)
    result("登录/注册API", login_api or register_api,
           "200 OK" if (login_api or register_api) else "未检测到")
    results["login_api"] = login_api or register_api

def test_05_booking():
    section("TEST 5: 预约座位")
    go_back_until(text="预约座位", partial=True, max_attempts=3)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)

    clear_log()
    if not tap_element(text="预约座位", partial=True):
        tap(135, 875)
    info("点击'预约座位'")
    time.sleep(3)

    root = dump_ui()
    room_tapped = False
    clickable_items = find_all(root, clickable=True)
    for item in clickable_items:
        b = parse_bounds(item.get("bounds", ""))
        if b is not None and 400 < b[1] and b[3] < 2000:
            c = get_center(item)
            if c is not None:
                tap(c[0], c[1])
                info(f"选择自习室 ({c[0]},{c[1]})")
                room_tapped = True
                break
    if not room_tapped:
        tap(540, 600)
    time.sleep(3)

    root = dump_ui()
    available = find_all(root, clickable=True)
    seat_tapped = False
    for seat in available:
        b = parse_bounds(seat.get("bounds", ""))
        if b is not None and 500 < b[1] and b[3] < 1800:
            text_val = seat.get("text", "")
            if re.match(r"\d+-\d+", text_val):
                c = get_center(seat)
                if c is not None:
                    tap(c[0], c[1])
                    info(f"选择座位 {text_val} ({c[0]},{c[1]})")
                    seat_tapped = True
                    break
    if not seat_tapped:
        info("尝试点击座位区域")
        tap(300, 800)
    time.sleep(2)

    if tap_element(text="预约此座位"):
        info("点击'预约此座位'")
    elif tap_element(text="预约", partial=True):
        info("点击'预约'按钮")
    else:
        warn("未找到预约按钮")
    time.sleep(2)

    booking_screen = element_exists(text="开始时间", partial=True) or \
                     element_exists(text="确认预约", partial=True)
    result("预约页面加载", booking_screen, "预约表单可见" if booking_screen else "未进入预约页面")
    results["booking_screen"] = booking_screen

    if booking_screen:
        if tap_element(text="开始时间", partial=True):
            info("点击'开始时间'")
            time.sleep(1)
            for _ in range(3):
                if tap_element(text="确认"):
                    info("确认选择")
                    time.sleep(1)
                    break
                time.sleep(0.5)

        if tap_element(text="结束时间", partial=True):
            info("点击'结束时间'")
            time.sleep(1)
            for _ in range(3):
                if tap_element(text="确认"):
                    info("确认选择")
                    time.sleep(1)
                    break
                time.sleep(0.5)

        tap_element(text="按小时", partial=True)
        time.sleep(0.5)

        clear_log()
        if tap_element(text="确认预约"):
            info("点击'确认预约'")
        time.sleep(3)

        logs = get_logs(["reservations"])
        book_ok = any("200" in l and "reservations" in l for l in logs)
        result("预约API", book_ok, "POST /api/reservations -> 200" if book_ok else "未检测到")
        results["booking_api"] = book_ok

        booking_success = element_exists(text="已支付") or \
                         element_exists(text="待支付") or \
                         element_exists(text="pending") or \
                         element_exists(partial=True, text="预约成功") or \
                         element_exists(partial=True, text="订单")
        result("预约结果", booking_success, "订单已创建" if booking_success else "预约可能失败")
        results["booking_result"] = booking_success
    else:
        results["booking_api"] = False
        results["booking_result"] = False

def test_06_order_list():
    section("TEST 6: 订单列表")
    go_back_until(text="预约座位", partial=True, max_attempts=5)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)

    if tap_element(text="我的预约", partial=True):
        info("点击'我的预约'")
    else:
        tap(945, 875)
    time.sleep(3)

    root = dump_ui()
    has_orders = find_element(root, partial=True, text="待支付") is not None or \
                 find_element(root, partial=True, text="已支付") is not None or \
                 find_element(root, partial=True, text="使用中") is not None or \
                 find_element(root, partial=True, text="已完成") is not None
    no_orders = find_element(root, text="暂无") is not None or \
                find_element(root, text="去预约座位") is not None

    ok = has_orders or no_orders
    result("订单列表加载", ok, "有订单" if has_orders else ("无订单" if no_orders else "未知"))
    results["order_list"] = ok

def test_07_checkin():
    section("TEST 7: 签到")
    go_back_until(text="预约座位", partial=True, max_attempts=3)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)

    if tap_element(text="我的预约", partial=True):
        info("点击'我的预约'")
    time.sleep(3)

    root = dump_ui()
    order_tapped = False
    for status_text in ["已支付", "待支付", "使用中"]:
        nodes = find_all(root, text=status_text)
        if nodes:
            c = get_center(nodes[0])
            if c is not None:
                tap(c[0], c[1])
                info(f"点击'{status_text}'订单")
                order_tapped = True
                break

    if not order_tapped:
        clickable_items = find_all(root, clickable=True)
        for item in clickable_items:
            b = parse_bounds(item.get("bounds", ""))
            if b is not None and 400 < b[1] and b[3] < 2000:
                c = get_center(item)
                if c is not None:
                    tap(c[0], c[1])
                    order_tapped = True
                    break

    time.sleep(2)

    clear_log()
    if tap_element(text="签到"):
        info("点击'签到'按钮")
        time.sleep(3)

        logs = get_logs(["checkin"])
        checkin_ok = any("200" in l and "checkin" in l for l in logs)
        result("签到API", checkin_ok, "POST /api/checkin -> 200" if checkin_ok else "未检测到")
        results["checkin_api"] = checkin_ok

        root = dump_ui()
        checked_in = find_element(root, text="使用中") is not None or \
                     find_element(root, text="in_use") is not None or \
                     find_element(root, text="签退") is not None
        result("签到结果", checked_in, "状态变为使用中" if checked_in else "签到可能失败")
        results["checkin_result"] = checked_in
    else:
        warn("未找到签到按钮（可能没有已支付的订单）")
        result("签到", False, "无已支付订单可签到")
        results["checkin_api"] = False
        results["checkin_result"] = False

def test_08_checkout():
    section("TEST 8: 签退")
    clear_log()
    if tap_element(text="签退"):
        info("点击'签退'按钮")
        time.sleep(3)

        logs = get_logs(["checkout"])
        checkout_ok = any("200" in l and "checkout" in l for l in logs)
        result("签退API", checkout_ok, "POST /api/checkout -> 200" if checkout_ok else "未检测到")
        results["checkout_api"] = checkout_ok

        root = dump_ui()
        completed = find_element(root, text="已完成") is not None or \
                    find_element(root, text="completed") is not None
        result("签退结果", completed, "订单已完成" if completed else "签退可能失败")
        results["checkout_result"] = completed
    else:
        warn("未找到签退按钮")
        result("签退", False, "无使用中订单可签退")
        results["checkout_api"] = False
        results["checkout_result"] = False

def test_09_ai_chat():
    section("TEST 9: AI聊天")
    go_back_until(text="预约座位", partial=True, max_attempts=5)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)

    clear_log()
    if tap_element(text="AI咨询", partial=True):
        info("点击'AI咨询'")
    else:
        tap(405, 1078)
    time.sleep(3)

    char_screen = element_exists(text="选择AI助手", partial=True) or \
                  element_exists(text="学霸猫", partial=True)
    result("AI角色选择页", char_screen, "角色列表可见" if char_screen else "未加载")
    results["ai_char_screen"] = char_screen

    if char_screen:
        if tap_element(text="学霸猫", partial=True):
            info("选择'学霸猫'")
        else:
            tap_first_clickable_in_area(400, 1500)
        time.sleep(3)

        chat_screen = element_exists(desc="发送") or \
                      element_exists(text="输入您的问题", partial=True)
        result("AI聊天页", chat_screen, "聊天界面可见" if chat_screen else "未加载")
        results["ai_chat_screen"] = chat_screen

        if chat_screen:
            input_node = find_element(dump_ui(), text="输入您的问题", partial=True)
            if input_node is not None:
                c = get_center(input_node)
                if c is not None:
                    tap(c[0], c[1])
                    time.sleep(0.5)
                    type_text("hello")
                    info("输入消息: hello")
                    time.sleep(0.5)

            if tap_element(desc="发送"):
                info("点击发送")
            press_enter()
            time.sleep(3)

            logs = get_logs(["chat", "characters", "deepseek"])
            char_api = any("characters" in l and "200" in l for l in logs)
            chat_api = any("chat" in l for l in logs)
            result("AI角色API", char_api)
            result("AI聊天API", chat_api)
            results["ai_api"] = char_api or chat_api

def test_10_rules():
    section("TEST 10: 规则页面")
    go_back_until(text="预约座位", partial=True, max_attempts=5)
    time.sleep(1)
    go_home_tab()
    time.sleep(1)

    clear_log()
    go_rules_tab()
    time.sleep(3)

    root = dump_ui()
    has_rules = find_element(root, partial=True, text="规则") is not None or \
                find_element(root, partial=True, text="须知") is not None or \
                find_element(root, partial=True, text="FAQ") is not None or \
                find_element(root, partial=True, text="常见") is not None or \
                find_element(root, partial=True, text="使用") is not None

    result("规则页面加载", has_rules, "规则内容可见" if has_rules else "规则内容未加载")
    results["rules"] = has_rules

def test_11_more_page():
    section("TEST 11: 更多页面")
    go_home_tab()
    time.sleep(1)
    go_more_tab()
    time.sleep(2)

    root = dump_ui()
    has_more = find_element(root, text="学习统计") is not None or \
               find_element(root, text="成就徽章") is not None or \
               find_element(root, text="预约记录") is not None or \
               find_element(root, text="积分兑换") is not None

    result("更多页面加载", has_more, "菜单项可见" if has_more else "菜单未加载")
    results["more_page"] = has_more

def test_12_profile():
    section("TEST 12: 个人中心")
    go_home_tab()
    time.sleep(1)
    go_profile_tab()
    time.sleep(2)

    root = dump_ui()
    has_profile = find_element(root, text="我的订单") is not None or \
                  find_element(root, text="我的钱包") is not None or \
                  find_element(root, text="偏好设置") is not None or \
                  find_element(root, text="退出登录") is not None

    result("个人中心加载", has_profile, "菜单项可见" if has_profile else "未加载")
    results["profile"] = has_profile

def main():
    header = "\n" + "="*60 + "\n  iStudySpot UI自动化联调测试\n  基于UI元素控制 (uiautomator dump + XML解析)\n" + "="*60
    print(header, flush=True)
    get_log_fh().write(header + "\n")
    get_log_fh().flush()

    section("PREP: 环境检查")
    ensure_adb_alive()

    if not check_prerequisites():
        print("ERROR: Prerequisites not met! Exiting.")
        return 1

    info("环境检查通过")

    test_01_app_launch()
    test_02_studyroom_list()
    test_03_seat_map()
    test_04_login()
    test_05_booking()
    test_06_order_list()
    test_07_checkin()
    test_08_checkout()
    test_09_ai_chat()
    test_10_rules()
    test_11_more_page()
    test_12_profile()

    section("测试结果汇总")
    for name, ok in results.items():
        result(name, ok)
    total = len(results)
    passed = sum(1 for v in results.values() if v)
    pct = passed / total * 100 if total > 0 else 0
    print(f"\n  总计: {passed}/{total} 通过 ({pct:.0f}%)", flush=True)
    get_log_fh().write(f"\n  总计: {passed}/{total} 通过 ({pct:.0f}%)\n")
    get_log_fh().close()

    return 0 if passed == total else 1

if __name__ == "__main__":
    sys.exit(main())
