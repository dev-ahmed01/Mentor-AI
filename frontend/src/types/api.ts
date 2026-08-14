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
