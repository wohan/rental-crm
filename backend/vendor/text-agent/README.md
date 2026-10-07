# Text Agent — vendored source snapshot

Source: https://github.com/wohan/voice-text-agent-guide
Snapshot date: 2026-10-07; version: 0.1.0-SNAPSHOT.
Includes the Java core, Spring Boot starter and tests. This snapshot includes implementation not yet published to Maven or committed in the upstream repository; no upstream commit SHA is claimed.

Build: `mvn -f backend/vendor/text-agent/pom.xml clean install` from the Rental CRM repository root.
Docker builds this source before packaging the host application. No external snapshot repository or developer Maven cache is required.

The opt-in LiveProviderTest is disabled unless AGENT_LIVE_TEST=true. It uses synthetic data, a real configured model and a local test HTTP destination. No credentials are included.
