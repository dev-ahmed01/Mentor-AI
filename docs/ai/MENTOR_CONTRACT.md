# Responsible mentor contract (Phase 9)

The mentor explains recorded learning decisions and suggests where to act next.
It is intentionally constrained: the model selects relevant evidence IDs and one
allowed next-step code. MentorAI renders the cited facts and fixed suggestion.
There is no arbitrary generated advice prose, invented score or tool execution.
Relevance can still be imperfect; users can inspect the underlying source and ask
another question. This is not a replacement for professional career advice.

## Local provider

Spring AI `spring-ai-ollama` 1.1.8 matches Spring Boot 3.5.x, as documented by
[the upstream compatibility table](https://github.com/spring-projects/spring-ai/tree/1.1.x).
The adapter uses the [Ollama chat model](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
without auto-configuration, startup pulls, callbacks, tool execution or embeddings.

Set `MENTOR_AI_ENABLED=true`, `OLLAMA_BASE_URL=http://localhost:11434` and
`OLLAMA_MODEL` to an already installed chat model. Defaults are disabled and
`qwen3:8b`. Only loopback HTTP origins are allowed; this implementation does not
send profile context to external providers. The backend runs on the host, as in
the existing setup, and can reach the Compose Ollama port on localhost.
Transport: 3-second connection timeout, 45-second read timeout, no redirects,
one attempt, 256 KiB response-body limit, 256 predicted tokens, 8192 context tokens,
temperature 0 and JSON output. No model is downloaded automatically.

## Context and memory

Each turn selects up to 12 current career priority facts, favoring skill names
mentioned in the question. It includes recorded weekly availability, relevant
current roadmap phase/completion count/next task, current weekly plan and check-in
summary, latest career market eligibility/provenance and an optional private saved
job comparison. Only a roadmap for the selected career contributes progress.
Scoring and readiness remain in the existing deterministic services.

The job context contains the historical calculated summary, not pasted text or
its editable metadata. It is attached only after an ownership check. Market
context contains bounded source metadata and counts only when freshness/sample/
employer/coverage guardrails pass. Otherwise every saved turn carries exactly
`Insufficient market evidence available.` No salary, trend, global-demand or
hiring-probability claim is generated.

The prompt receives at most four recent turn excerpts (question capped at 500
characters, cited IDs, status) and a deterministic topic summary containing only
fixed category labels. It is not a detailed memory of old commitments or personal
circumstances. Full raw conversation history is stored privately, not sent to the
model. The complete serialized request must fit 40,000 characters.

## Validation and boundaries

`prompts/mentor-v1.txt` is the system message; all question/context/history fields
are untrusted data in a separate user message. The only accepted response is an
object containing `factIds` (1–6 distinct supplied IDs) and `nextStep` from the
allowlist. Reject extra keys, unknown/duplicate IDs, duplicate JSON keys, trailing
tokens, invalid types, oversized output and a job next step without an attachment.
Only application-owned facts and links can enter the rendered response.

Allowed next steps lead to priorities, profile, market, the attached job analysis,
or Weekly check-in. The exam/temporary-constraint suggestion asks the student to
record capacity and dates in the existing form. That deterministic flow produces
adaptation proposals; acceptance still requires its existing explicit confirmation.
Mentor code has no calls to mutate profiles, goals, roadmaps, check-ins or adaptations.

## Persistence and concurrency

V9 adds conversations owned by a user, their career and optional job analysis,
revision/topic memory, and immutable JSON turns. A turn freezes question, context,
selected facts and provenance, answer/status, navigation suggestion, provider model
name, prompt version and time. Model name is configuration, not a weights digest.
Later profile/evidence edits never change old replies. Data is retained until
owning-account deletion cascades; this phase adds no conversation deletion API.

Creation/message/retrieval enforce authentication. Foreign-owner IDs return 404.
Sending requires a unique client request UUID and expected revision. A completed
retry with identical question/revision returns the same saved turn; reuse for
different content returns 409. Model I/O occurs outside transactions. Atomic
compare-and-swap commits only one competing turn; a losing response returns 409
and cannot overwrite history. Concurrent identical requests may both invoke the
provider but converge on the committed result. No in-flight database lock can be
left behind after a crash.

Each conversation allows 60 turns, read in 20-turn pages. The conversation list
shows the latest 20; older URLs still work. Provider disabled/network failure is
`UNAVAILABLE`; invalid model output is `INVALID_OUTPUT`; a valid selection is
`ANSWERED`. Unavailable turns are saved with no fabricated reply. Users can inspect
history and resend after fixing the model; deterministic features remain usable.

## Verification scope

Automated fixtures exercise the actual Spring AI HTTP adapter plus success,
outage, output validation, injection, privacy, memory and concurrency paths.
The user requested API/build verification and explicitly skipped a real model
run. A passing fixture does not establish real-model relevance or performance.
Interactive browser, keyboard/mobile and no-JavaScript behavior remain unverified.
Application rollback can leave the additive V9 tables intact; never rewrite
applied migrations or delete conversation history as a code rollback.
