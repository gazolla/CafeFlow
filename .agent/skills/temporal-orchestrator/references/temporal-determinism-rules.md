# Temporal Determinism Rules

Workflows in Temporal must be **deterministic**. This is because Temporal achieves fault tolerance by replaying the workflow code.

## The Golden Rule
**If you run the same workflow code with the same history multiple times, it must result in the same state.**

## What NOT to do in a Workflow:

1.  **No direct I/O**: Do not use `File`, `Socket`, or `HttpClient` directly in a workflow. Move these to **Activities**.
2.  **No Mutability in Static Variables**: Shared mutable state between different workflow executions is a source of bugs.
3.  **No Native Language Time**: Do not use `System.currentTimeMillis()` or `LocalDateTime.now()`. Use `Workflow.currentTimeMillis()`.
4.  **No Random Numbers**: Do not use `Math.random()` or `UUID.randomUUID()`. Use `Workflow.sideEffect` or move to an Activity.
5.  **No Direct Threading**: Do not use `new Thread()` or `ExecutorService`. Use `Async.function()` or `Workflow.newPromise()`.
6.  **No Non-deterministic Iteration**: Be careful with `HashMap` or `HashSet` iteration if the order matters for the workflow logic. Use `LinkedHashMap`.

## Best Practice
If you need any of the above, wrap it in an **Activity** or a **Side Effect**.
