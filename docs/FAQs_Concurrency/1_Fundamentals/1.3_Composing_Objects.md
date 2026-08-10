### Stopping clients and subclasses from touching your lock?
<details><summary>Show answer</summary>

Lock on a private object the outside world can't reach, instead of on `this`:

```java
private final Object lock = new Object();
public void foo() {
  synchronized (lock) {
    // ...
  }
}
```

A `synchronized` method locks `this`, which is public. That lets a client hold your lock forever — a denial-of-
service, accidental or deliberate — and lets a subclass lock `this` for its own reasons and collide with the base
class on the same lock. A private lock is invisible outside the class, so neither can interfere. It's encapsulating
the lock inside the object it guards.

</details>

### Why must a lock field be final?
<details><summary>Show answer</summary>

So the lock can't be reassigned. If the field could change, two threads might synchronize on different objects —
one on the old lock, one on the new — and get no mutual exclusion at all, so they run the guarded code at the same
time with no error. `final` pins every thread to the same lock. Lock fields should always be `final`, whether it's
a plain monitor object or a `java.util.concurrent.locks` lock.

</details>

### When can't you hide the lock from clients?
<details><summary>Show answer</summary>

Only unconditionally thread-safe classes can use a private lock. A conditionally thread-safe class must tell
clients which lock to acquire for the sequences that need external synchronization — so that lock has to be
publicly reachable, not hidden. You can't document "lock on X" and make X private at the same time.

</details>
