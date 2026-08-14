# AI Architecture

AI is deliberately not wired into Phase 1. Profile, authentication, browsing,
deterministic calculations, saved roadmaps, and progress must continue to work
when Ollama is unavailable.

## Planned services

```text
AiProvider <- OllamaAiProvider (Spring AI)
EmbeddingService
RetrievalService
EvidenceService
```

No controller or domain service will call Ollama directly. Chat and embedding
model IDs come from environment variables. Important prompts are versioned under
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

Retrieved content is untrusted data. System policy, trusted application facts,
and retrieved/job-description text will be separated and prompt-injection test
fixtures will verify that embedded instructions are ignored. Invalid structured
outputs may receive one bounded format retry; otherwise the feature returns an
explicit “AI mentor is currently unavailable” error.
