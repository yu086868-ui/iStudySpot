# Android 安全扫描配置指南

## 概述

本项目集成了多层安全扫描机制，包括密钥泄露检测和Android安全风险分析。

## GitHub Secrets 配置

在使用CI/CD安全扫描之前，需要在GitHub仓库中配置以下Secrets：

### 必需的Secrets

| Secret名称 | 描述 | 获取方式 |
|-----------|------|---------|
| `GITHUB_TOKEN` | GitHub令牌 | 自动提供，无需配置 |
| `CODECOV_TOKEN` | CodeCov上传令牌 | 从[CodeCov](https://codecov.io/)获取 |

### 可选的Secrets

| Secret名称 | 描述 | 获取方式 |
|-----------|------|---------|
| `GITLEAKS_LICENSE` | Gitleaks企业版许可证 | 从[Gitleaks](https://gitleaks.com/)购买 |
| `MOBSF_API_KEY` | MobSF API密钥 | 从MobSF服务器获取 |
| `MOBSF_SERVER` | MobSF服务器地址 | 部署MobSF服务器后获取 |

## 安全扫描工具

### 1. Gitleaks - 密钥泄露检测

**配置文件**: `.gitleaks.toml`

**本地运行**:
```bash
# 安装Gitleaks
brew install gitleaks  # macOS
# 或
choco install gitleaks  # Windows

# 运行扫描
gitleaks detect --source . --config ../.gitleaks.toml
```

**检测内容**:
- API密钥
- Firebase密钥
- AWS凭证
- 私钥文件
- Gradle属性中的敏感信息

### 2. Trivy - 漏洞扫描

**配置文件**: `.trivyignore`

**本地运行**:
```bash
# 安装Trivy
brew install trivy  # macOS
# 或
choco install trivy  # Windows

# 运行扫描
trivy fs .
```

**检测内容**:
- 文件系统漏洞
- 密钥泄露
- 配置问题

### 3. Semgrep - 静态分析

**配置文件**: `frontend/Android/.semgrep.yml`

**本地运行**:
```bash
# 安装Semgrep
pip install semgrep

# 运行扫描
semgrep --config .semgrep.yml
```

**检测内容**:
- 硬编码密钥
- 不安全存储
- WebView安全配置
- SQL注入
- 弱加密算法

### 4. OWASP Dependency Check

**配置**: 已集成到`build.gradle.kts`

**本地运行**:
```bash
./gradlew dependencyCheckAnalyze
```

**报告位置**: `app/build/reports/dependency-check-report.html`

**检测内容**:
- 依赖漏洞
- 已知CVE
- 过时依赖

### 5. Android Lint

**本地运行**:
```bash
./gradlew lintDebug
```

**报告位置**: `app/build/reports/lint-results-debug.html`

**检测内容**:
- 安全问题
- 性能问题
- 国际化问题
- 代码质量

### 6. MobSF - APK安全分析

**部署MobSF服务器**:
```bash
# 使用Docker部署
docker pull opensecurity/mobile-security-framework-mobsf
docker run -it -p 8000:8000 opensecurity/mobile-security-framework-mobsf:latest
```

**获取API密钥**:
1. 访问 http://localhost:8000
2. 登录后访问 http://localhost:8000/api_docs
3. 获取API密钥

### 7. QARK - Android安全分析

**本地运行**:
```bash
# 安装QARK
pip install qark

# 运行扫描
qark --apk app/build/outputs/apk/debug/app-debug.apk
```

### 8. Mariana Trench - 数据流分析

**本地运行**:
```bash
# 安装Mariana Trench
pip install mariana-trench

# 运行扫描
mariana-trench --system-apk app/build/outputs/apk/debug/app-debug.apk
```

### 9. CodeQL - 代码安全分析

**本地运行**:
```bash
# 下载CodeQL CLI
# https://github.com/github/codeql-cli-binaries/releases

# 创建数据库
codeql database create --language=java-kotlin --source-root=. codeql-db

# 运行分析
codeql database analyze codeql-db --format=csv --output=results.csv
```

## CI/CD工作流

### 安全扫描流程

```
┌─────────────────────────────────────────────────────────────┐
│                     Security Scan                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  Gitleaks   │  │   Trivy     │  │   Semgrep   │         │
│  │ 密钥泄露检测 │  │ 漏洞扫描    │  │ 静态分析    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Android Security Analysis                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   MobSF     │  │    QARK     │  │  Mariana    │         │
│  │ APK安全分析  │  │ 组件风险检测 │  │  Trench     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│  ┌─────────────┐  ┌─────────────┐                          │
│  │ Android Lint│  │ Dependency  │                          │
│  │ 安全检查    │  │   Check     │                          │
│  └─────────────┘  └─────────────┘                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    CodeQL Analysis                           │
│              Java/Kotlin 代码安全分析                         │
└─────────────────────────────────────────────────────────────┘
```

### 触发条件

- Push到`main`或`develop`分支
- Pull Request到`main`或`develop`分支
- 仅当`frontend/Android/`目录下的文件发生变化时触发

## 安全报告

所有安全扫描报告会上传到GitHub Actions Artifacts：

| Artifact名称 | 内容 |
|-------------|------|
| `security-reports` | MobSF、QARK、Mariana Trench报告 |
| `dependency-check-report` | OWASP依赖检查报告 |
| `test-results` | 测试结果 |

## 处理安全问题

### 1. 密钥泄露

如果发现密钥泄露：

1. **立即撤销泄露的密钥**
2. **生成新密钥**
3. **更新代码中的密钥引用**
4. **将密钥移至安全存储**（如环境变量、密钥管理服务）

### 2. 依赖漏洞

如果发现依赖漏洞：

1. **检查是否有更新版本**
2. **更新到安全版本**
3. **如果无法更新，评估风险并考虑替代方案**
4. **必要时添加到`.trivyignore`并说明原因**

### 3. 代码安全问题

如果发现代码安全问题：

1. **评估风险等级**
2. **修复问题**
3. **添加测试验证修复**
4. **更新安全规则**

## 最佳实践

### 密钥管理

```kotlin
// ❌ 错误：硬编码密钥
val apiKey = "sk-1234567890abcdef"

// ✅ 正确：使用BuildConfig
val apiKey = BuildConfig.API_KEY

// ✅ 正确：使用环境变量
val apiKey = System.getenv("API_KEY")
```

### 网络安全

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">your-domain.com</domain>
    </domain-config>
</network-security-config>
```

### 数据存储

```kotlin
// ✅ 使用EncryptedSharedPreferences存储敏感数据
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val sharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

## 参考资料

- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)
- [Gitleaks Documentation](https://github.com/gitleaks/gitleaks)
- [Trivy Documentation](https://aquasecurity.github.io/trivy/)
- [Semgrep Documentation](https://semgrep.dev/docs/)
- [MobSF Documentation](https://github.com/MobSF/Mobile-Security-Framework-MobSF)
