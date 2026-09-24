export type WeeklyOutcome = "COMPLETED" | "PARTIAL" | "MISSED" | "DEFERRED";
export type CapacityBand = "LOW" | "MEDIUM" | "HIGH";
export type Blocker = "NO_TIME" | "TOO_DIFFICULT" | "UNCLEAR_NEXT_STEP" | "RESOURCE_ACCESS" | "OTHER";
export type ConstraintType = "EXAMS" | "ASSIGNMENTS" | "INTERNSHIP" | "HEALTH_OR_PERSONAL" | "TRAVEL" | "PLACEMENT_PREP" | "OTHER";
export type TemporaryConstraint = { type: ConstraintType; startDate: string; endDate: string };
export type WeeklyPlan = {
  id: string; roadmapId: string; roadmapTitle: string; weekStart: string;
  capacityHours: number; plannedHours: number; roadmapRevision: number;
  tasks: { taskId: string; title: string; plannedHours: number }[];
  reason: string; createdAt: string;
  revision: number; mode: "NORMAL" | "MAINTENANCE";
};
export type WeeklyCheckIn = {
  id: string; planId: string; roadmapId: string; weekStart: string;
  plannedHours: number; actualHours: number; availableHoursNextWeek: number;
  difficultyRating: number | null; confidenceRating: number | null;
  energyOrCapacityBand: CapacityBand; blockers: Blocker[]; notes: string | null;
  constraint: TemporaryConstraint | null;
  tasks: { taskId: string; title: string; outcome: WeeklyOutcome }[];
  nextPlan: WeeklyPlan; explanation: string; createdAt: string;
  adaptation?: Adaptation | null;
};

export type AllocationSnapshot = {
  capacityHours: number; mode: "NORMAL" | "MAINTENANCE";
  tasks: { taskId: string; title: string; plannedHours: number }[];
};
export type Adaptation = {
  id: string; checkInId: string; roadmapId: string; planId: string; weekStart: string;
  status: "PENDING" | "ACCEPTED"; policyVersion: string; trigger: string; reason: string;
  createdAt: string; acceptedAt?: string | null; roadmapRevision: number; planRevision: number; canAccept: boolean;
  before: AllocationSnapshot; proposed: AllocationSnapshot; accepted?: AllocationSnapshot | null;
  resumeTaskId?: string | null; resumeTitle?: string | null;
  blockerQuestions: { taskId: string; title: string; question: string }[];
  candidates: { taskId: string; title: string; maxHours: number }[];
};
export type ProgressHistory = {
  items: { plan: WeeklyPlan; checkIn: WeeklyCheckIn | null }[];
  page: number; hasNext: boolean;
};
