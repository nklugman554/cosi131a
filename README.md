# A Unix-ish Shell

In PA1A, you will write a simplified Unix shell, a command-line program that reads one command at a time, runs it, and prints its output. 
This part covers the individual commands and a REPL that can run one command at a time.
Then in PA1B pipes, redirects, background jobs, and `cd` all arrive in Part B.

## Getting started

**1. JDK 21 or newer.** Check what you have: `java -version`. If that's below 21, install one:

- macOS: `brew install openjdk@21` ([Homebrew](https://brew.sh)), or download from
  [Adoptium](https://adoptium.net/temurin/releases/).
- Windows: install the Temurin 21 `.msi` from [Adoptium](https://adoptium.net/temurin/releases/)
  — check "Set JAVA_HOME" in the installer.
- Linux: `sudo apt install openjdk-21-jdk` (Debian/Ubuntu) or your distro's equivalent.

If `java -version` still shows an old version after installing, you likely have more than one JDK on your machine — check
`JAVA_HOME` and your shell's `PATH` ordering.

**2. Maven.** Check with `mvn -version` (its output also shows which Java it's using — make sure
that's 21+, too). If missing: `brew install maven` (macOS), `sudo apt install maven` (Linux), or
see the [official install guide](https://maven.apache.org/install.html) (Windows included).

**3. Sanity-check the build.** From this directory:

```
mvn clean test
```

Expect test *failures*, not compile errors — every method you need to implement currently just
throws `UnsupportedOperationException` (see "Project structure" below). If instead the build fails
to *compile*, or `mvn`/`java` isn't found at all, that's a setup problem, not a code problem — fix
it before you start implementing anything.

For the full generated API reference (every class/method signature and doc comment), open
`doc/apidocs/index.html` in a browser (regenerate it anytime with `mvn javadoc:aggregate`).

Running a command means dynamically loading and invoking its class from `commandBin` — `shell`
has no compile-time dependency on that module (check `shell/pom.xml`), so `Shell.java` can't write
`new Wc(args)` directly. That pulls in two ideas most of you haven't used in Java before.
`Shell.executeCommand()` — given to you, fully working — is the worked example; you don't have to
write this kind of code yourself, but you'll want to understand it (its Javadoc has a concrete
step-by-step walkthrough).

- **Finding your own class on disk.** `Shell.class.getProtectionDomain().getCodeSource().getLocation()`
  returns a `URL`; converting it to a `Path` goes through a
  [`URI`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html)
  (`Paths.get(url.toURI())`) — `URL` has no direct conversion.
- **Loading and calling a method you only know the name of.** A
  [`URLClassLoader`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URLClassLoader.html)
  pointed at `commandBin`'s compiled classes loads the class by name, at runtime; then
  [reflection](https://docs.oracle.com/javase/tutorial/reflect/) — `class.getMethod("main",
  String[].class)` then `.invoke(...)` — calls its `main`. `ShellCommand.start()` does the same
  trick in miniature (on a constructor).

## Project structure

Two modules:

- **`commandBin`** — one class per command (`Cat`, `Grep`, `Ls`, `Pwd`, `Wc`, `Sleep`). Each one
  extends `ShellCommand` (given to you, don't modify it — read it first, it's short). **`Pwd` and
  `Sleep` are given to you, fully implemented, as worked examples** — start by reading them.
  You implement `runCommand()` for `Ls`, `Cat`, `Grep`, and `Wc`.
- **`shell`** — `Shell.java`, a REPL that runs one command at a time. `getCurrentClassPath`,
  `getPath`, `executeCommand`, and `classNameToCommandName` are given; you implement `runRepl`
  and `findCommandClass`.

Every method you need to implement currently looks like this:

```java
// TODO: implement <method>
throw new UnsupportedOperationException("TODO: implement <method>");
```

Replace the `throw` with your implementation. The Javadoc directly above each method explains what
it needs to do; read it before you start.

## Commands

| Command | Description |
|---|---|
| `pwd` | Prints the shell's current working directory. |
| `ls [<path>...]` | Lists files/directories. With no args, lists the current directory. |
| `cat <file>...` | Prints the contents of one or more files. |
| `grep <pattern> [<file>...]` | Prints lines matching a regex pattern. |
| `wc [<file>...]` | Prints line, word, and byte counts. |
| `sleep <seconds>` | Blocks for the given number of seconds, then exits. |
| `exit` | Terminates the shell. |

## Error handling

Every command should report errors to `System.err` and keep running rather than crashing. Look at
the `*Test.java` files for the exact expected error message format for each case (nonexistent
file, file that's actually a directory, invalid command, etc.) — the tests are the source of
truth here, not this table.

## Building and testing

Run Maven from the command line (see "Getting started" above for install steps) — running tests
through your IDE may not exercise the Maven plugins this project relies on.

```
mvn clean test
```

Include `clean`, or results from a previous run can affect the current one. You can run a single
module's tests with `mvn clean test -pl :module-name`, but run both modules together before you
consider yourself done — `commandBin`'s compiled classes are what `shell`'s tests dynamically
load at runtime, so a broken `commandBin` build can cause confusing failures in `shell`'s tests.

**Do not modify the `*Test.java` files** — they're the specification for this assignment. If your
code doesn't match what a test expects, the test is right and your code needs to change.

## What's next

Part B expands this into a real, process-based shell with pipes, redirects, background jobs, and
`cd`. Keep your working `commandBin` and `findCommandClass` implementation around — you'll bring
them forward into the Part B project yourself.
