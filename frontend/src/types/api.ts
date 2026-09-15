export type User = {
  id: string;
  email: string;
  displayName: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
  user: User;
};

export type ProfileSkill = {
  id: string;
  name: string;
  category: string;
  proficiency: "AWARENESS" | "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
  confidence: "LOW" | "MEDIUM" | "HIGH";
  source: "SELF_REPORTED" | "ASSESSMENT" | "PROJECT" | "IMPORTED";
};

export type Profile = {
  id: string;
  degree?: string;
  year?: number;
  semester?: number;
  interests: string[];
  goals: string[];
  programmingLanguages: string[];
  preferredDomains: string[];
  targetLocations: string[];
  remotePreference?: "REMOTE" | "HYBRID" | "ON_SITE" | "FLEXIBLE";
  timeAvailablePerWeek?: number;
  skills: ProfileSkill[];
  currentProjects: string[];
  experience?: string;
  certifications: string[];
  shortTermGoal?: string;
  longTermGoal?: string;
  avoidances: string[];
  updatedAt: string;
};

export type ApiError = {
  status: number;
  code: string;
  message: string;
  requestId?: string;
  fieldErrors?: Record<string, string>;
};

export type EntryDifficulty = "LOW" | "MEDIUM" | "HIGH" | "VERY_HIGH";
export type SkillRequirement = "REQUIRED" | "PREFERRED";
export type FitBand = "STRONG" | "PROMISING" | "EXPLORATORY" | "LIMITED_CURRENT_ALIGNMENT";

export type CareerSkill = {
  id: string;
  name: string;
  category: string;
  importance: number;
  requirement: SkillRequirement;
};

export type CareerSummary = {
  id: string;
  slug: string;
  name: string;
  description: string;
  entryDifficulty: EntryDifficulty;
  recommendedWeeklyHours: number;
  coreSkills: CareerSkill[];
  dataLabel: string;
};

export type CareerDetail = CareerSummary & {
  degreeRelevance: string;
  projectExpectations: string;
  internshipExpectations: string;
  commonMisconceptions: string;
  realitySummary: string;
  responsibilities: string[];
  commonJobTitles: string[];
  skills: CareerSkill[];
  risks: string[];
  marketConsiderations: string[];
  marketEvidenceStatus: string;
  updatedAt: string;
};

export type CareerFitFactors = {
  interestAlignment: number;
  goalAlignment: number;
  skillAlignment: number;
  entryAccessibility: number;
  learningEffortCompatibility: number;
  marketCompatibility?: number;
};

export type SkillGap = Omit<CareerSkill, "id"> & {
  skillId: string;
  currentProficiency?: ProfileSkill["proficiency"];
  priority: "HIGH" | "MEDIUM" | "LATER";
};

export type CareerCandidate = {
  careerId: string;
  slug: string;
  careerName: string;
  careerFitIndicator: number;
  fitBand: FitBand;
  factors: CareerFitFactors;
  evidenceCoverage: number;
  reasons: string[];
  strengths: string[];
  skillGaps: SkillGap[];
  risks: string[];
  alternatives: Array<{ id: string; slug: string; name: string; careerFitIndicator: number }>;
  uncertainties: string[];
  nextStep: string;
};

export type CareerAnalysis = {
  calculatedAt: string;
  calculationVersion: string;
  scoreName: string;
  scoreMode: string;
  methodology: string;
  configuredWeights: Record<string, number>;
  availableEvidenceWeight: number;
  marketEvidenceStatus: string;
  profileLimitations: string[];
  candidates: CareerCandidate[];
};

export type SkillPrerequisites = {
  skillId: string;
  name: string;
  eligible: boolean;
  coverage: "NO_RECORDED_PREREQUISITES" | "MODELED_PREREQUISITES";
  minimumProficiency: ProfileSkill["proficiency"];
  calculationVersion: string;
  dataLabel: string;
  prerequisites: Array<{
    skillId: string;
    name: string;
    direct: boolean;
    currentProficiency?: ProfileSkill["proficiency"];
    satisfied: boolean;
  }>;
};
