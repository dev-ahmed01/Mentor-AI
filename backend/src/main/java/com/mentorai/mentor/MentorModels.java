package com.mentorai.mentor;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public final class MentorModels {
    private MentorModels() { }
    public record CreateRequest(@NotNull UUID careerId,UUID jobAnalysisId) { }
    public record MessageRequest(@NotNull UUID requestId,@Min(0) long expectedRevision,@NotBlank @Size(max=2000) String question) { }
    public record Conversation(UUID id,UUID careerId,String careerName,UUID jobAnalysisId,Instant createdAt,Instant updatedAt,long revision,String memory) { }
    public record Fact(String id,String kind,String text,String href,Map<String,String> provenance) { }
    public record Recent(String question,List<String> citedFactIds,String status) { }
    public record Context(Instant capturedAt,UUID careerId,String careerName,List<Fact> facts,String marketNotice,List<Recent> recent,String memory) { }
    public record NextStep(String code,String label,String href) { }
    public record Turn(UUID id,UUID requestId,long revision,Instant createdAt,String question,String status,String answer,
                       String marketNotice,List<Fact> citations,NextStep nextStep,Context context,String promptVersion,String providerModel) { }
    public record History(Conversation conversation,List<Turn> turns,int page,boolean hasOlder) { }
    public record Selection(List<String> factIds,String nextStep) { }
}
