```
Run gitleaks/gitleaks-action@v2   

​            [yu086868-ui] is an individual user. No license key is required.   

​            gitleaks version: 8.24.3   

​            Version to install: 8.24.3 (target directory: /tmp/gitleaks-8.24.3)   

​            Cache hit for: gitleaks-cache-8.24.3-linux-x64   

​            Received 1519322 of 5713626 (26.6%), 1.4 MBs/sec   

​            Received 5713626 of 5713626 (100.0%), 4.4 MBs/sec   

​            Cache Size: ~5 MB (5713626 B)   

​            /usr/bin/tar -xf /home/runner/work/_temp/f2898540-a0cf-4f23-b49d-9fa9d4326901/cache.tzst -P -C /home/runner/work/iStudySpot/iStudySpot --use-compress-program unzstd   

​            Cache restored successfully   

​            Gitleaks restored from cache   

​            event type: pull_request   

​            gitleaks cmd: gitleaks detect --redact -v --exit-code=2 --report-format=sarif --report-path=results.sarif --log-level=debug --log-opts=--no-merges --first-parent fafeef2b76b90957597a9fa2903baceef87eddec^..7d552b953bb23a1d38d9a25261a7ef951c3c9d53   

​            /tmp/gitleaks-8.24.3/gitleaks detect --redact -v --exit-code=2 --report-format=sarif --report-path=results.sarif --log-level=debug --log-opts=--no-merges --first-parent fafeef2b76b90957597a9fa2903baceef87eddec^..7d552b953bb23a1d38d9a25261a7ef951c3c9d53   

​               

​                ○   

​                │╲   

​                │ ○   

​                ○ ░   

​                ░    gitleaks   

​               

​            2:34PM DBG using github.com/wasilibs/go-re2 regex engine   

​            2:34PM DBG using existing gitleaks config .gitleaks.toml from `(--source)/.gitleaks.toml`   

​            2:34PM DBG extending config with default config   

​            2:34PM DBG executing: /usr/bin/git -C . log -p -U0 --no-merges --first-parent fafeef2b76b90957597a9fa2903baceef87eddec^..7d552b953bb23a1d38d9a25261a7ef951c3c9d53   

​            2:34PM DBG SCM platform parsed from host host=github.com platform=github   

​            Finding:     REDACTED   

​            Secret:      REDACTED   

​            RuleID:      jwt   

​            Entropy:     5.369494   

​            File:        test_token.txt   

​            Line:        1   

​            Commit:      7d552b953bb23a1d38d9a25261a7ef951c3c9d53   

​            Author:      Scylier   

​            Email:       xiangyear@gmail.com   

​            Date:        2026-05-25T14:30:49Z   

​            Fingerprint: 7d552b953bb23a1d38d9a25261a7ef951c3c9d53:test_token.txt:jwt:1   

​            Link:        https://github.com/yu086868-ui/iStudySpot/blob/7d552b953bb23a1d38d9a25261a7ef951c3c9d53/test_token.txt#L1   

​               

​            2:34PM INF 1 commits scanned.   

​            2:34PM DBG Note: this number might be smaller than expected due to commits with no additions   

​            2:34PM INF scanned ~264492 bytes (264.49 KB) in 185ms   

​            2:34PM WRN leaks found: 1   

​            Artifact name is valid!   

​            Root directory input is valid!   

​            Beginning upload of artifact content to blob storage   

​            Uploaded bytes 7202   

​            Finished uploading artifact content to blob storage!   

​            SHA256 digest of uploaded artifact zip is 40df7e353ca3c2adea74f111a609efe148cdc37363c0e07077674218cb608dce   

​            Finalizing artifact upload   

​            Artifact gitleaks-results.sarif.zip successfully finalized. Artifact ID 7200137017   

​            Warning: Error encountered when attempting to write a comment on PR #96: HttpError: Resource not accessible by integration   

​            Likely an issue with too large of a diff for the comment to be written.   

​            All secrets that have been leaked will be reported in the summary and job artifact.   

​            Warning: 🛑 Leaks detected, see job summary for details   
```





