package com.mentorai.roadmap.entity;

public enum TaskState {
    NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED, NEEDS_REVIEW;

    public boolean terminal() { return this == COMPLETED || this == SKIPPED; }

    public boolean canTransitionTo(TaskState next) {
        if (this == next) return true;
        return switch (this) {
            case NOT_STARTED, IN_PROGRESS, NEEDS_REVIEW -> true;
            case COMPLETED -> next == NEEDS_REVIEW;
            case SKIPPED -> next == NOT_STARTED || next == NEEDS_REVIEW;
        };
    }
}
