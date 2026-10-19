# Practical Java

Java 21 coding practice: algorithms, concurrency, rate limiters, and backend
component design. The active IDE workspace is `C:\Users\abhis\hello`.

## Setup and checks

Install JDK 21, Maven, and Git. For a new checkout:

```powershell
git clone https://github.com/savi0909/practical-java.git
Set-Location practical-java
java -version
mvn -version
mvn test
```

In IntelliJ IDEA, open `pom.xml` as a Maven project and select JDK 21. Maven
uses `src/main/java` for implementations and `src/test/java` for tests.
IDE settings and the `target/` and `out/` build directories are ignored by Git.

After `mvn test`, run the standalone substring exercise:

```powershell
java -cp target/classes dsa.slidingwindow.LongestSubstringWithoutRepeating
```

The rate limiter also has a standalone check runner:

```powershell
java -cp 'target/classes;target/test-classes' ratelimiter.fixedwindow.FixedWindowRateLimiterCheck
```

The semicolon classpath separator above is for Windows. On other platforms, use
a colon. Verified on 2026-10-19 with Zulu OpenJDK 21.0.9: 41 Maven tests, all nine
substring scenarios, and the standalone fixed-window checks passed. Focused
checks do not prove concurrency correctness or establish performance guarantees.
The fresh build compiled 151 main and seven test source files. Existing compiler
warnings remain in `bank/Bank.java` (synchronization on value-based objects) and
`dsa/datastructures/Bag.java` (unchecked operations).

## Study material

- [Learning plan](docs/learning-plan.md): study order, priorities, and progress.
- [Coding practice handover](docs/coding-guidance-handover.md): practice modes,
  assistance tracking, and attempt history.
- [Rate limiter context](rate-limiter.md): shared contracts and design decisions.
- [Fixed window](src/main/java/ratelimiter/fixedwindow/README.md),
  [sliding window](src/main/java/ratelimiter/slidingwindow/README.md),
  [sliding-window counters](src/main/java/ratelimiter/slidingwindowcounter/README.md),
  and [token bucket](src/main/java/ratelimiter/tokenbucket/README.md): implementation notes.

## Performance labs

Prefer separate modules, such as **Rate Limiter Lab**, for performance-testing
runs and benchmarking. Keep each lab's workloads, configuration, run instructions,
and results together. Record the JDK, environment, and measurement limits alongside
results so runs can be interpreted and reproduced. Lab modules are planned future
organization; the current project remains one Maven module.

## Repository history

The IDE project was previously connected to `savi0909/coding-java`. Its current
files are now published to `savi0909/practical-java`, preserving both repositories'
existing Git history. In the migrated local workspace, `origin` points to
`practical-java` and the `coding-java` remote retains the previous destination.
