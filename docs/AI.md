# AI architecture

Provider keys exist only on the backend.

```text
Student app → API (quota from entitlements) → LLM provider
Admin → API → generation job → questions with status AI_GENERATED / DRAFT
Reviewer → APPROVED → PUBLISHED → student catalog
```

## Tutor (Phase 9)

- System prompts stored in the database.
- Ground first in published explanations and topic text.
- Label output: “AI assistance, not an official answer key.”
- Rate-limit and log prompts without extra PII.

## Similar questions

Default: same exam + topic + nearby difficulty, exclude the same id and recently seen ids. Embeddings are optional later.

## Generation

Admin requests N questions for an exam/subject. Jobs write draft questions. **Never auto-publish.**

Provider choice (Gemini vs OpenAI) is deferred until Phase 9.
