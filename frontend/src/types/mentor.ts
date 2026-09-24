export type MentorConversation = { id: string; careerId: string; careerName: string; jobAnalysisId?: string; createdAt: string; updatedAt: string; revision: number; memory: string };
export type MentorFact = { id: string; kind: string; text: string; href: string; provenance: Record<string, string> };
export type MentorTurn = { id: string; requestId: string; revision: number; createdAt: string; question: string; status: "ANSWERED" | "UNAVAILABLE" | "INVALID_OUTPUT"; answer: string; marketNotice: string; citations: MentorFact[]; nextStep?: { code: string; label: string; href: string }; context: { capturedAt: string }; promptVersion: string; providerModel: string };
export type MentorHistory = { conversation: MentorConversation; turns: MentorTurn[]; page: number; hasOlder: boolean };
export type MentorStatus = { enabled: boolean; model: string; mode: string; promptVersion: string };
