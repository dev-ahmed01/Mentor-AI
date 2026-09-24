export type DemoRun = { id: string; roadmapId: string; planId: string; weekStart: string; roadmapRevision: number; planRevision: number; scenarioVersion: string; createdAt: string; examCheckInId?: string };
export type DemoStatus = { enabled: boolean; run?: DemoRun };
