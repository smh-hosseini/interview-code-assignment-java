# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor toward a unified architecture pattern, specifically adopting the Store/Warehouse approach (Hexagonal Architecture + DDD) across all domains. Here's why:
Why Hexagonal Architecture + DDD?
Hexagonal Architecture (Ports & Adapters):
- Testability: Business logic (use cases) can be tested without database/HTTP layer
- Flexibility: Swap implementations without touching business logic
- Clear Boundaries: Adapters (REST, DB) depend on domain, not vice versa

Domain-Driven Design Benefits:
- Ubiquitous Language: Domain models reflect business concepts
- Business Rules Centralization: Rules live in use cases, not scattered across layers
- Explicit Transactions: Use case boundaries define transaction scope
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
For me, using an OpenAPI spec like with the Warehouse API brings structure and alignment — it forces clear contracts, makes collaboration easier, and keeps the API consistent across teams. The downside is that it adds a bit of overhead and slows you down early on.

On the other hand, coding endpoints directly, like with Product and Store, is faster and more flexible, but it often leads to inconsistencies and harder maintenance later.

If I had to choose, I’d go with the OpenAPI-first approach for anything that’s shared or growing across teams, and use the code-first style only for quick internal endpoints or early prototypes.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I’d prioritize testing by business impact and reliability.

Unit tests for core business logic first — warehouse validation, fulfillment cost rules, and store outbox — since they’re fast and high-value.

Integration tests for key API flows like warehouse creation and fulfillment cost calculation to validate end-to-end behavior.

Repository tests to cover complex queries and persistence edge cases.

Contract tests to ensure the Warehouse API stays consistent with the OpenAPI spec.

E2E and performance tests can come later once the system stabilizes.
To maintain coverage, I’d use Jacoco with thresholds, automate checks in CI, apply mutation testing for critical logic, and review tests regularly as business rules evolve.
```