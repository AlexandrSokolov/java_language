
### What is `CopyOnWriteArrayList` for?
<details><summary>Show answer</summary>

For a list that is read often and written rarely — an observer list is the classic fit.

Every write makes a fresh copy of the whole backing array. Because the array is never changed in place, reads take
no lock and never see a half-changed state — many threads can iterate at once, fast. The cost sits entirely on
writes: each one copies the entire array.

So it wins when reads vastly outnumber writes, and is a bad choice when writes are frequent — copying the whole
array every write is far too expensive then.

</details>