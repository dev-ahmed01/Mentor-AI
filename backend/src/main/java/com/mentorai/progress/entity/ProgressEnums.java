package com.mentorai.progress.entity;

public final class ProgressEnums {
    private ProgressEnums() { }
    public enum Outcome { COMPLETED, PARTIAL, MISSED, DEFERRED }
    public enum EnergyBand { LOW, MEDIUM, HIGH }
    public enum Blocker { NO_TIME, TOO_DIFFICULT, UNCLEAR_NEXT_STEP, RESOURCE_ACCESS, OTHER }
    public enum ConstraintType { EXAMS, ASSIGNMENTS, INTERNSHIP, HEALTH_OR_PERSONAL, TRAVEL, PLACEMENT_PREP, OTHER }
}
