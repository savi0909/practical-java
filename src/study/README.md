# Basic Java backend study examples

Each topic has its own code, runnable checks, and short README tutorial.
Start with the implementation comments, then run its test main method.
See the [root README](../../README.md) for Java 21 setup and commands to compile
the complete repository and run all eight check runners.

| Order | Topic | Status for this request |
| --- | --- | --- |
| 1 | [ID generator](idgenerator/README.md) | Existing files preserved |
| 2 | [Fixed-window limiter](../ratelimiter/FixedWindowREADME.md) | Moved to root ratelimiter package |
| 3 | [Token-bucket limiter](../ratelimiter/TokenBucketREADME.md) | Moved to root ratelimiter package |
| 4 | [Expiring store](expiringstore/README.md) | Basic version added |
| 5 | [Idempotency registry](idempotencyregistry/README.md) | Basic version added |
| 6 | [Inventory reservation](inventoryreservation/README.md) | Basic version added |
| 7 | [Bounded queue](boundedqueue/README.md) | Basic version added |
| 8 | [Delayed scheduler](delayedscheduler/README.md) | Basic version added |

All five new examples target Java 17 and use `ReentrantLock`. They are single-JVM,
in-memory baselines. The three existing topics retain their algorithm behavior.
Both limiters now share the root [ratelimiter package and design](../ratelimiter/README.md).

Example from the project root (PowerShell):

```powershell
javac --release 17 -Xlint:all -Werror -d out/study-basic src/study/expiringstore/*.java
java -cp out/study-basic study.expiringstore.ExpiringStoreTest
```

Read the five new tutorials in order, then revisit one implementation at a time.
Supplied examples and passing checks do not establish independent mastery.

2026-10-19 setup validation: all 23 Java sources compiled targeting Java 21 with
`-Xlint:all -Werror`; all eight check runners passed. Performance harness not run.
