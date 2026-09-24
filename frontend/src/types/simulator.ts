import type { LearningPrioritiesResponse, ProfileSkill, SkillPrerequisites } from "@/types/api";

export type SimulationState = {
  requiredSatisfied: number; requiredTotal: number; preferredSatisfied: number; preferredTotal: number;
  eligibleUnfinishedSkills: number; priorities: LearningPrioritiesResponse;
};
export type SkillSimulation = {
  calculationVersion: string; resultLabel: "SIMULATION"; dataLabel: "DEMO DATA"; marketEvidenceStatus: "UNAVAILABLE";
  targetCareerId: string; careerName: string; skillId: string; skillName: string;
  recordedProficiency?: ProfileSkill["proficiency"]; assumedProficiency: ProfileSkill["proficiency"];
  profileUpdatedAt: string; assumption: string; selectedSkillPrerequisites: SkillPrerequisites;
  before: SimulationState; after: SimulationState;
  newlySatisfiedRequirements: { skillId: string; name: string; requirement: "REQUIRED" | "PREFERRED" }[];
  newlyEligibleSkills: { skillId: string; name: string }[]; noChange: boolean;
};
export type SimulationActionState = { error?: string; result?: SkillSimulation };
