package com.mentorai.progress.dto;
import java.util.List;
public record CheckInHistoryResponse(List<Item> items,int page,boolean hasNext) {
    public record Item(WeeklyPlanResponse plan,CheckInResponse checkIn) { }
}
