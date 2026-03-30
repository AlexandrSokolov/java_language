
# ✅ README: Enabling Fully Offline Maven Builds in IntelliJ IDEA (2026.x)

This project requires IntelliJ IDEA to run Maven **fully offline**, without attempting to contact Nexus or any external repositories.
In IntelliJ IDEA **2026.x**, the only configuration that reliably enforces offline mode is adding a local Maven configuration file inside the project’s `.idea` directory.

---

## ✅ Why this is needed

IntelliJ IDEA (2026.x) sometimes ignores:

- the global “Work Offline” setting
- Maven’s built‑in offline flag (`-o`)
- proxy settings
- disabled snapshot updates

As a result, it may still try to download metadata from Nexus or Maven Central during project sync or builds.

---

## ✅ Working Solution

To enforce offline mode **for all Maven actions started from IntelliJ**, create the following structure in your project:

```
.idea/
    .mvn/
        maven.config
```

If `.idea/.mvn/` does not exist, create both folders manually.

Then add this content to `maven.config`:

```bash
# Run Maven fully offline
-o
```

Maven will now **always** run in offline mode when invoked from IntelliJ (builds, imports, tests, sync, run configurations, etc.).

---

## ✅ Notes

- This configuration applies **only** to IntelliJ’s internal Maven execution.
- It does **not** affect command‑line Maven (`mvn`), unless you replicate the same `.mvn/maven.config` in the project root.
- You can temporarily disable offline mode by commenting out the line:

```bash
# -o
```

---

## ✅ Result

After adding this file, IntelliJ:

- ✅ stops contacting Nexus
- ✅ stops refreshing remote metadata
- ✅ performs all Maven builds fully offline
- ✅ works consistently, even after restarts or project reloads

This is the **only approach** known to consistently enforce offline behavior in IntelliJ IDEA 2026.x.

