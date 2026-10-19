# Practical Java

Small, dependency-free Java examples for studying concurrency and backend components.
Each topic keeps its implementation, runnable checks, and explanation together.
Start with the [study index](src/study/README.md) or the
[rate limiter design](src/ratelimiter/README.md).

## Setup

Install JDK 21 and Git, then clone the repository:

```powershell
git clone https://github.com/savi0909/practical-java.git
Set-Location practical-java
java -version
javac -version
```

This is a plain Java project with no Maven, Gradle, or external dependencies.
Use JDK 21 for the complete source tree; the ID comparison harness uses a Java 21 API.

In IntelliJ IDEA, open the repository folder, select JDK 21 as the project SDK,
and mark `src` as **Sources Root**. Run check classes through their `main` methods.
IDE settings and compiled output remain local and are ignored by Git.

## Compile and run checks

Run from the repository root in PowerShell. Compilation treats warnings as errors:

```powershell
$sourceFiles = @(Get-ChildItem -LiteralPath src -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName })
javac --release 21 -Xlint:all -Werror -d out/setup-check @sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }

$checkClasses = @(
    'study.idgenerator.IdTester'
    'ratelimiter.RateLimiterTest'
    'ratelimiter.TokenBucketRateLimiterTest'
    'study.expiringstore.ExpiringStoreTest'
    'study.idempotencyregistry.IdempotencyRegistryTest'
    'study.inventoryreservation.InventoryReservationTest'
    'study.boundedqueue.BoundedBlockingQueueTest'
    'study.delayedscheduler.DelayedTaskSchedulerTest'
)
foreach ($checkClass in $checkClasses) {
    java -ea -cp out/setup-check $checkClass
    if ($LASTEXITCODE -ne 0) { throw "Check failed: $checkClass" }
}
```

Run the rate limiter client example after compiling:

```powershell
java -cp out/setup-check ratelimiter.RateLimiterExample
```

Verified on 2026-10-19 with Zulu OpenJDK 21.0.9: all 23 Java source files compiled
without warnings, all eight check runners passed, and the client example accepted
the login and search requests. The performance comparison harness was not run.
These focused checks do not prove concurrency correctness or establish performance guarantees.

## Performance labs

Use separate modules, such as **Rate Limiter Lab**, for performance-testing runs
and benchmarks. Keep each lab's workload, configuration, run instructions, and
results together, while keeping the study examples focused on implementation
and correctness. Record the JDK, environment, workload, and measurement limits
alongside results so timings can be interpreted and reproduced.

This is the preferred organization for future performance work. The existing
ID comparison harness remains in `src/study/idgenerator/`; this setup does not
add or migrate a lab module.

## Study context

[MEMORY.md](MEMORY.md) records learning preferences; [CONTEXT.md](CONTEXT.md)
records progress and validation history. Earlier entries refer to an independent
`rate-limiter-lab/` repository, which is ignored and is not included in this clone.
The available rate limiter examples are under `src/ratelimiter/`.
