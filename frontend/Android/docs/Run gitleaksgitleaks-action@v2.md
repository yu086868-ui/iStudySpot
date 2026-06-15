Run gitleaks/gitleaks-action@v2   

​            [yu086868-ui] is an individual user. No license key is required.   

​            gitleaks version: 8.24.3   

​            Version to install: 8.24.3 (target directory: /tmp/gitleaks-8.24.3)   

​            Downloading gitleaks from https://github.com/zricethezav/gitleaks/releases/download/v8.24.3/gitleaks_8.24.3_linux_x64.tar.gz   

​            /usr/bin/tar xz --warning=no-unknown-keyword --overwrite -C /tmp/gitleaks-8.24.3 -f /tmp/gitleaks.tmp   

​            /usr/bin/tar --posix -cf cache.tzst --exclude cache.tzst -P -C /home/runner/work/iStudySpot/iStudySpot --files-from manifest.txt --use-compress-program zstdmt   

​            Sent 5713628 of 5713628 (100.0%), 5.4 MBs/sec   

​            event type: pull_request   

​            gitleaks cmd: gitleaks detect --redact -v --exit-code=2 --report-format=sarif --report-path=results.sarif --log-level=debug --log-opts=--no-merges --first-parent 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc^..a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            /tmp/gitleaks-8.24.3/gitleaks detect --redact -v --exit-code=2 --report-format=sarif --report-path=results.sarif --log-level=debug --log-opts=--no-merges --first-parent 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc^..a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​               

​                ○   

​                │╲   

​                │ ○   

​                ○ ░   

​                ░    gitleaks   

​               

​            6:00AM DBG using github.com/wasilibs/go-re2 regex engine   

​            6:00AM DBG using existing gitleaks config .gitleaks.toml from `(--source)/.gitleaks.toml`   

​            6:00AM DBG extending config with default config   

​            6:00AM DBG executing: /usr/bin/git -C . log -p -U0 --no-merges --first-parent 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc^..a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            6:00AM DBG SCM platform parsed from host host=github.com platform=github   

​            Finding:     ...r(username: String, REDACTED: String, nickname: String, p...   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.750000   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        11   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:11   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L11   

​               

​            Finding:     ...reshREDACTED(refreshREDACTED: String) = apiManager.refres...   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        13   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:13   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L13   

​               

​            Finding:     suspend fun logout(REDACTED: String) = ApiManager(REDACT...   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        14   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:14   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L14   

​               

​            Finding:     ...tring) = ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        14   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:14   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L14   

​               

​            Finding:     ...reshREDACTED(refreshREDACTED: String) = executeRequest {   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt   

​            Line:        105   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt:gradle-properties-secret:105   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt#L105   

​               

​            Finding:     ...ookingType: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        27   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:27   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L27   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        28   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:28   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L28   

​               

​            Finding:     ...pageSize: Int = 20, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        30   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:30   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L30   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        31   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:31   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L31   

​               

​            Finding:     ...rDetail(id: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        33   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:33   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L33   

​               

​            Finding:     | `REDACTED.kt` | 20+个 | View...   

​            Secret:      REDACTED   

​            RuleID:      android-api-key   

​            Entropy:     4.054229   

​            Tags:        [api-key android]   

​            File:        frontend/Android/docs/work-summary.md   

​            Line:        84   

​            Commit:      1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-12T15:54:41Z   

​            Fingerprint: 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc:frontend/Android/docs/work-summary.md:android-api-key:84   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc/frontend/Android/docs/work-summary.md?plain=1#L84   

​               

​            Finding:     ...elOrder(id: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        36   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:36   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L36   

​               

​            Finding:     ...ayOrder(id: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        39   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:39   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L39   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        40   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:40   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L40   

​               

​            Finding:     ...ng, seatId: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        45   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:45   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L45   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        46   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:46   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L46   

​               

​            Finding:     ...InRecordId: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        48   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:48   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L48   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        49   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:49   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L49   

​               

​            Finding:     ...pageSize: Int = 20, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        51   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:51   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L51   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        52   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:52   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L52   

​               

​            Finding:     ...n getCurrentCheckin(REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        54   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:54   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L54   

​               

​            Finding:     ApiManager(REDACTED = REDACTED, context = context)....   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        55   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:55   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L55   

​               

​            Finding:     ...end fun getUserInfo(REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        58   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:58   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L58   

​               

​            Finding:     REDACTED = "mock_new_REDACTED"   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt   

​            Line:        112   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt:gradle-properties-secret:112   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/infra/network/ApiManager.kt#L112   

​               

​            Finding:     ...il: String? = null, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        61   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:61   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L61   

​               

​            Finding:     ...n changeREDACTED(oldREDACTED: String, newREDACTED: String...   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.750000   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        64   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:64   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L64   

​               

​            Finding:     ...n changeREDACTED(oldREDACTED: String, newREDACTED: String...   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.750000   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        64   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:64   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L64   

​               

​            Finding:     ...ewPassword: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        64   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:64   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L64   

​               

​            Finding:     ...mentMethod: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        68   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:68   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L68   

​               

​            Finding:     ...tStatus(id: String, REDACTED: String) =   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.321928   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt   

​            Line:        71   

​            Commit:      a5028f27ebe052f9b32eb397e34c4c1757ab44b0   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-13T03:07:14Z   

​            Fingerprint: a5028f27ebe052f9b32eb397e34c4c1757ab44b0:frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt:gradle-properties-secret:71   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/a5028f27ebe052f9b32eb397e34c4c1757ab44b0/frontend/Android/app/src/main/java/com/example/scylier/istudyspot/repository/MainRepository.kt#L71   

​               

​            Finding:     val REDACTED = "sk-1234567890abcdef"   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.584963   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/docs/security.md   

​            Line:        255   

​            Commit:      1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-12T15:54:41Z   

​            Fingerprint: 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc:frontend/Android/docs/security.md:gradle-properties-secret:255   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc/frontend/Android/docs/security.md?plain=1#L255   

​               

​            Finding:     val REDACTED = BuildConfig.API_KEY   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.584963   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/docs/security.md   

​            Line:        258   

​            Commit:      1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-12T15:54:41Z   

​            Fingerprint: 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc:frontend/Android/docs/security.md:gradle-properties-secret:258   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc/frontend/Android/docs/security.md?plain=1#L258   

​               

​            Finding:     val REDACTED = System.getenv("API_KEY")   

​            Secret:      REDACTED   

​            RuleID:      gradle-properties-secret   

​            Entropy:     2.584963   

​            Tags:        [gradle secret]   

​            File:        frontend/Android/docs/security.md   

​            Line:        261   

​            Commit:      1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-12T15:54:41Z   

​            Fingerprint: 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc:frontend/Android/docs/security.md:gradle-properties-secret:261   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc/frontend/Android/docs/security.md?plain=1#L261   

​               

​            Finding:     val apiKey = "REDACTED"   

​            Secret:      REDACTED   

​            RuleID:      generic-api-key   

​            Entropy:     4.247928   

​            File:        frontend/Android/docs/security.md   

​            Line:        255   

​            Commit:      1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-12T15:54:41Z   

​            Fingerprint: 1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc:frontend/Android/docs/security.md:generic-api-key:255   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/1eddfe0d55fc1747d312a3be60f0f8ce6cfad9bc/frontend/Android/docs/security.md?plain=1#L255   

​               

​            6:00AM INF 2 commits scanned.   

​            6:00AM DBG Note: this number might be smaller than expected due to commits with no additions   

​            6:00AM INF scanned ~60681 bytes (60.68 KB) in 134ms   

​            6:00AM WRN leaks found: 34   

​            Artifact name is valid!   

​            Root directory input is valid!   

​            Beginning upload of artifact content to blob storage   

​            Uploaded bytes 8245   

​            Finished uploading artifact content to blob storage!   

​            SHA256 digest of uploaded artifact zip is 7411edae4b120e9c5ddd57ad86e3fa11c4096410dccbc24232b5b202cae5bf65   

​            Finalizing artifact upload   

​            Artifact gitleaks-results.sarif.zip successfully finalized. Artifact ID 7011059687   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: Error encountered when attempting to write a comment on PR #86: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: 🛑 Leaks detected, see job summary for details   