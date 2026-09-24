import type { LearningPrioritiesResponse, Roadmap } from "@/types/api";
export type PivotSkill = { skillId: string; name: string; currentProficiency: string | null; targetProficiency: string };
export type PivotSummary = { id: string; createdAt: string; sourceCareerName: string; targetCareerName: string; status: "PREVIEW" | "ACCEPTED"; acceptedRoadmapId: string | null };
export type PivotDetail = {
  id: string; createdAt: string; status: "PREVIEW" | "ACCEPTED"; acceptedRoadmapId: string | null; acceptedAt: string | null;
  comparison: {
    policyVersion: string; sourceRoadmap: Roadmap; profileUpdatedAt: string;
    before: LearningPrioritiesResponse; after: LearningPrioritiesResponse;
    credits: Array<{ taskId: string; skillId: string; name: string; proficiency: string; source: string }>;
    transferableSkills: PivotSkill[]; newlyRequiredSkills: PivotSkill[]; satisfiedPrerequisites: PivotSkill[]; skippableSkills: PivotSkill[];
    changedPriorities: Array<{ skillId: string; name: string; beforePriority: string; afterPriority: string; beforePoints: number | null; afterPoints: number | null; beforeRelevance: string | null; afterRelevance: string | null }>;
    effort: { band: string; illustrativeHours: number; capacityWeeks: number; explanation: string };
    proposedStages: Array<{ title: string; tasks: Array<{ skillId: string; name: string; estimatedHours: number; targetMet: boolean; prerequisites: string[] }> }>;
  };
};
