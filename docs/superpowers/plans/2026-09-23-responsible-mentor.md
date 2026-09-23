# Responsible Mentor Implementation Plan

> Execute inline with executing-plans / test-driven-development; one fresh final review.

**Spec:** `docs/MentorAI_Hackathon_Phases_and_Codex_Prompt.md`, Phase 9.
**Goal:** Private conversational guidance grounded in existing deterministic results, without changing student plans.
**Architecture:** Spring AI 1.1.8 Ollama adapter behind AiProvider; bounded context retrieval, strict structured-output validator, deterministic response rendering, private conversation/turn persistence and Next.js mentor UI. Additive V9 only.

## Design and rulings
- Continue the approved phase sequence in the current feature checkout from `b88e9b2`. API/build-only verification persists. Local checkpoint only, no push/deployment/Phase 10.
- New `/mentor` and `/mentor/{id}` pages; authenticated status/list/create/read/message APIs. Conversation pins a selected active career and optional owner-only saved job analysis. Context refreshes each turn; past replies stay frozen with their cited facts.
- Retrieve up to 12 relevant priority facts, profile weekly hours, current phase/task/progress and weekly check-in counts for the matching career, latest market snapshot eligibility/provenance, and optional saved job result. Do not include credentials, email, raw job descriptions, raw market payloads or all profile/history data. Keep at most four recent turn excerpts and a deterministic bounded summary of previously discussed topic categories.
- The model selects 1–6 supplied fact IDs and one allowed next-step code in strict JSON. It cannot generate displayed scores, market claims or arbitrary advice prose. The application resolves selected IDs into grounded explanation text and fixed read-only navigation suggestions. This constrained mentor explains and recommends from supplied evidence rather than free-form unsupported claims. Invalid output produces a visible unavailable turn, never raw model text.
- The versioned system prompt separates policy from JSON user/context/history data and treats all user-originated strings as untrusted. No model tools, external browsing, write actions or automatic profile/roadmap mutation. Missing/ineligible market evidence always includes exactly `Insufficient market evidence available.`
- The exam/constraint next step links to existing weekly check-ins; proposed and accepted adaptations remain under the deterministic confirmation flow. The mentor never submits a check-in or accepts an adaptation.
- Ollama disabled by default, local endpoint only, configured model, no startup model pull. Bound connect/read timeouts and output size, one request with no format retry. Provider errors are sanitized and persisted as UNAVAILABLE turns; other product APIs remain usable.
- V9 stores owner-only conversations, revision and bounded topic memory, plus immutable turns containing question, context snapshot, status and response. Unique client request ID per conversation supports retries; optimistic revision prevents stale concurrent commits. Model I/O occurs outside database transactions; no persistent in-flight lock. Limit 60 turns/conversation and expose 20-turn pages. Other owners receive 404.

## Tasks
1. [x] Write failing API contracts; implement dependency/provider/context/persistence/validator/service and V9. Add grounding, injection, ownership, history/memory, idempotency/concurrency and outage tests; verify H2.
2. [x] Add server actions/types/pages/navigation with loading/errors, history, evidence provenance, no claims of plan changes; verify lint/build.
3. [x] Fresh review completed. Verify final H2/PostgreSQL, V8-to-V9 preservation, HTTP actions/SSR and provider transport. Real model run explicitly skipped by user. Update docs/Graphify and prepare the local checkpoint.

## Review focus
1. Model output or injected text cannot replace scores, cited facts, links or apply a plan change.
2. Cross-owner conversations/job attachments and concurrent/retried messages cannot leak or overwrite results.
3. Context/history/memory must remain bounded and avoid unrelated careers or private raw documents.
4. Stale/sparse evidence must not become a market claim; saved turn provenance remains reproducible.
5. Provider outages/invalid output and UI retries must preserve conversation state and other product flows.

## Ledger
- Baseline `b88e9b2`: tracked tree clean, Phase 8 fully verified. Graphify query completed. Existing phase specification authorizes architectural work; no repeated design gate/worktree requested.
- Spring AI upstream compatibility specifies 1.1.x for Boot 3.5.x; Maven Central metadata lists stable 1.1.8. Use pinned `spring-ai-ollama` without auto-configuration or tool callbacks. Local Ollama endpoint responds with no listed models; optional model preference requested while implementation continues.
- Pre-flight: context produces immutable fact IDs/text; provider receives only a bounded context; validator allows only those IDs and fixed next steps; renderer/persistence/UI share the same saved turn DTO. Existing scoring and adaptation services remain authoritative and read-only from mentor.
- User chose API/build verification only and explicitly skipped a real model run. No model download or generated real-model response will be claimed. Spring AI transport is exercised against a controlled local HTTP fixture.
- Initial test environment had a stale missing compiled class; recompilation resolved it, then all three contracts failed at missing mentor endpoints. Implemented V9 and provider/context/conversation layers. Missing optional roadmap reads initially marked a shared transaction rollback-only; removed the outer transaction so optional service reads use independent read transactions. Priority calculation now uses the same captured profile values as the profile context fact.
- Grounding regressions exposed trailing JSON acceptance, and a transport regression exposed oversized body acceptance. Both observed RED before adding strict trailing-token rejection and a 256 KiB response stream cap. Other fixtures cover invalid IDs/keys/actions, no raw job text or credentials, private attachments, bounded topic/recent memory, immutable responses, outages and concurrent compare-and-swap.
- Initial full suite exposed shared market fixtures rather than application failures: unrelated earlier tests left eligible evidence. Isolated the new test fixtures and added explicit stale/fresh provenance verification. Full H2 suite passed 105 tests with zero failures/errors/skips. Frontend lint/build passed after separating data-fetch error handling from JSX; final build includes the job-to-mentor link. Independent review is running.
- Final review completed: no Critical/Important defects. Minor deferred: simultaneous identical-request concurrency lacks its own regression fixture; current tests cover sequential identical retries and simultaneous distinct requests, and the reviewer found the implementation sound. Real-model quality/performance and interactive/browser/no-JavaScript behavior remain explicitly unverified per user scope. PostgreSQL/preservation/live checks are implementer-owned, not delegated.
- Final PostgreSQL 17.11 suite passed 105 tests with zero failures/errors/skips and packaged the backend. V8-to-V9 upgrade preserved all 38 existing tables with identical row counts and ordered row-content hashes, checked before creating new verification records.
- Live API and production SSR/action checks passed: owner-only conversation/history, disabled-provider persisted response, identical retry and stale revision handling, frozen context, unchanged profile/roadmap/weekly plan, escaped question text, and create/send redirects using the installed React action encoder.
- Final frontend lint, TypeScript and production build passed. Graphify AST update completed with 2443 nodes, 6152 edges and 152 communities. No real model download/run or interactive browser checks were performed. Phase 10 remains separate.
