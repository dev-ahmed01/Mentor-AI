import type { LearningDecision, ProfileSkill } from "@/types/api";
type SkillProficiency = ProfileSkill["proficiency"];

export type JobDraft = {
  extractionVersion: string; description: string; title: string; responsibilities: string;
  experience: string; location: string; technologies: string;
  requiredSkills: string[]; preferredSkills: string[]; unclassifiedSkills: string[]; limitation: string;
};
export type JobSkillMatch = {
  skillId?: string; name: string; requirement: "REQUIRED" | "PREFERRED" | "UNCLASSIFIED";
  currentProficiency?: SkillProficiency; comparisonTarget?: SkillProficiency;
  status: "MATCHED" | "PARTIAL" | "MISSING" | "UNASSESSED"; weight: number;
};
export type JobAnalysis = {
  id: string; calculatedAt: string; calculationVersion: string; originalExtraction: JobDraft;
  reviewedJob: Omit<JobDraft, "extractionVersion" | "limitation"> & { reviewed: boolean };
  profileInputs: { updatedAt: string; weeklyHours?: number; skills: { id: string; name: string; proficiency: SkillProficiency }[] };
  status: string; matchIndicator?: number; skills: JobSkillMatch[]; priorities: LearningDecision[];
  preparationVersion: string; methodology: string;
};
export type JobExtractState = { draft?: JobDraft; error?: string };
