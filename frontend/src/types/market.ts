import type { CareerCandidate, LearningPrioritiesResponse } from "@/types/api";

export type MarketObservation = {
  id: string; source: string; sourceId: string; sourceUrl: string; collectedAt: string; publishedAt: string;
  title: string; company: string; location: string; remote: boolean; processingVersion: string;
  skills: { skillId: string; name: string; requirement: string }[];
};
export type MarketSnapshot = {
  id: string; careerId: string; careerName: string; source: string; sourceUrl: string; sourceContext: string;
  collectedAt: string; windowStart: string; windowEnd: string; freshUntil: string;
  sampleSize: number; employerCount: number; listingsWithSkills: number; minimumSampleSize: number;
  processingVersion: string; matchedTitles: string[];
  skills: { skillId: string; name: string; mentions: number; required: number; preferred: number; unspecified: number }[];
  observations: MarketObservation[]; limitations: string[];
};
export type MarketEvidence = { status: string; snapshot?: MarketSnapshot; message: string };
export type MarketDecision = {
  id: string; calculatedAt: string; calculationVersion: string; profileUpdatedAt: string;
  recordedSkills: Record<string, string>; weeklyHours?: number; evidence: MarketEvidence;
  profileOnlyCareer: CareerCandidate; marketCompatibility?: number; marketWeight: number; careerFitIndicator: number;
  priorities: Omit<LearningPrioritiesResponse, "marketEvidenceStatus"> & { marketEvidenceStatus: string };
  methodology: string;
};
