# AI Architecture

Phase 9 adds an opt-in local mentor. Profile, authentication, browsing,
deterministic calculations, saved roadmaps, and progress must continue to work
when Ollama is unavailable.

## Implemented mentor services

```text
AiProvider <- OllamaAiProvider (Spring AI)
MentorContextService -> MentorOutputValidator -> MentorService
MentorRepository (owner-only conversations and immutable turns)
```

Only the provider calls Ollama. The chat model comes from environment variables.
Important prompts are versioned under
`backend/src/main/resources/prompts` rather than embedded as Java strings.

## Evidence pipeline

```text
validated profile + deterministic metrics + retrieved evidence
  -> bounded context package
  -> structured-output prompt
  -> schema validation
  -> recommendation rules
  -> labeled response
```

Responses distinguish observed evidence, calculated metrics, AI inference,
recommendation, confidence, and uncertainty. The LLM cannot create fit scores,
market counts, percentages, salary claims, permissions, or ownership decisions.
Market statements require stored evidence containing source, URL where allowed,
collection time, region, data window, sample size, and processing version.

Retrieved content is untrusted data. The versioned system policy is separate from
the JSON question/context/history message. Raw job descriptions, raw market
payloads and account credentials are not sent. Invalid structured output receives
no retry and becomes an explicit unavailable turn. See the
[mentor contract](MENTOR_CONTRACT.md) for implemented limits and behavior.

Embedding/retrieval infrastructure is deferred: direct owner-scoped service reads
already provide the small relevant context this phase needs. No vector store or
tool execution is introduced.
## Final demo boundary

Synthetic demo setup and the release smoke script never invoke a model. AI remains
disabled by default and production Compose disables it explicitly. The guide
discloses unavailable AI instead of substituting fabricated success. Controlled
provider tests verify contracts; no real-model quality or latency claim follows
from API/build verification. Deterministic planning remains usable independently.
