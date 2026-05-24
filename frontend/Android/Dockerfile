# Android 前端 Dockerfile - 多阶段构建
# 用于 CI/CD 环境中构建 APK

# ============================================
# 阶段1: 构建阶段
# ============================================
FROM eclipse-temurin:17-jdk-jammy AS builder

LABEL maintainer="iStudySpot Team"
LABEL description="Android frontend build environment"

WORKDIR /app

ENV DEBIAN_FRONTEND=noninteractive
ENV GRADLE_OPTS="-Xmx4g -XX:+HeapDumpOnOutOfMemoryError"
ENV ANDROID_SDK_ROOT=/opt/android-sdk

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

RUN wget -q https://services.gradle.org/distributions/gradle-8.5-bin.zip -O /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && ln -s /opt/gradle-8.5/bin/gradle /usr/local/bin/gradle \
    && rm /tmp/gradle.zip

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdline-tools.zip \
    && unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

ENV PATH="${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools"

RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true \
    && sdkmanager --update \
    && sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

COPY gradle ./gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY app/build.gradle.kts ./app/
COPY gradle/libs.versions.toml ./gradle/

RUN gradle --no-daemon dependencies --stacktrace

COPY app ./app

RUN gradle --no-daemon assembleDebug --stacktrace

# ============================================
# 阶段2: 测试阶段
# ============================================
FROM builder AS tester

RUN gradle --no-daemon testDebugUnitTest jacocoTestReport --stacktrace

# ============================================
# 阶段3: 输出阶段
# ============================================
FROM eclipse-temurin:17-jre-jammy AS output

WORKDIR /output

COPY --from=builder /app/app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk

COPY --from=tester /app/app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml ./coverage/

COPY --from=tester /app/app/build/reports/tests/testDebugUnitTest/ ./test-reports/

LABEL org.opencontainers.image.source="https://github.com/yu086868-ui/iStudySpot"
LABEL org.opencontainers.image.description="iStudySpot Android Frontend Build Output"

CMD ["echo", "Build completed. APK available at /output/app-debug.apk"]
