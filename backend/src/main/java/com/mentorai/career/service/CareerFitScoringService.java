package com.mentorai.career.service;

import com.mentorai.career.dto.AlternativeCareerResponse;
import com.mentorai.career.dto.CareerAnalysisResponse;
import com.mentorai.career.dto.CareerCandidateResponse;
import com.mentorai.career.dto.CareerFitFactors;
import com.mentorai.career.dto.FitBand;
import com.mentorai.career.dto.SkillGapResponse;
import com.mentorai.career.entity.Career;
import com.mentorai.career.entity.CareerSkill;
import com.mentorai.career.entity.EntryDifficulty;
import com.mentorai.career.entity.SkillRequirement;
import com.mentorai.profile.entity.StudentProfile;
import com.mentorai.skills.entity.SkillConfidence;
import com.mentorai.skills.entity.SkillProficiency;
import com.mentorai.skills.entity.StudentSkill;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class CareerFitScoringService {

    public static final String CALCULATION_VERSION = "career-fit-v1";
    public static final int AVAILABLE_EVIDENCE_WEIGHT = 85;
    public static final Map<String, Integer> WEIGHTS = weights();

    public CareerAnalysisResponse score(StudentProfile profile, List<Career> careers, int limit) {
        List<String> interestInputs = combined(
                profile.getInterests(), profile.getPreferredDomains(), profile.getProgrammingLanguages());
        List<String> goalInputs = combined(
                profile.getGoals(), Arrays.asList(profile.getShortTermGoal(), profile.getLongTermGoal()));
        Map<String, StudentSkill> studentSkills = studentSkillMap(profile);

        List<ScoredCareer> scored = careers.stream()
                .map(career -> scoreCareer(career, profile, interestInputs, goalInputs, studentSkills))
                .sorted(Comparator.comparingInt(ScoredCareer::indicator).reversed()
                        .thenComparing(item -> item.career().getName()))
                .toList();

        List<CareerCandidateResponse> candidates = new ArrayList<>();
        for (int index = 0; index < Math.min(limit, scored.size()); index++) {
            ScoredCareer item = scored.get(index);
            List<AlternativeCareerResponse> alternatives = scored.stream()
                    .filter(other -> !other.career().getId().equals(item.career().getId()))
                    .limit(2)
                    .map(other -> new AlternativeCareerResponse(
                            other.career().getId(), other.career().getSlug(),
                            other.career().getName(), other.indicator()))
                    .toList();
            candidates.add(item.toResponse(alternatives));
        }

        return new CareerAnalysisResponse(
                Instant.now(),
                CALCULATION_VERSION,
                "Career Fit Indicator",
                "PROFILE_ONLY_NO_MARKET_EVIDENCE",
                "Available profile factors are normalized across 85 configured weight points. "
                        + "The 15-point market factor is excluded until validated market evidence exists.",
                WEIGHTS,
                AVAILABLE_EVIDENCE_WEIGHT,
                "INSUFFICIENT_MARKET_EVIDENCE",
                profileLimitations(profile),
                candidates);
    }

    private ScoredCareer scoreCareer(
            Career career,
            StudentProfile profile,
            List<String> interestInputs,
            List<String> goalInputs,
            Map<String, StudentSkill> studentSkills) {
        MatchResult interest = alignment(interestInputs, career.getInterestSignals());
        MatchResult goals = alignment(goalInputs, career.getGoalSignals());
        int skill = skillAlignment(career, studentSkills);
        int accessibility = accessibility(career.getEntryDifficulty());
        int effort = effortCompatibility(profile.getTimeAvailablePerWeek(), career.getRecommendedWeeklyHours());
        int weightedSum = interest.score() * WEIGHTS.get("interestAlignment")
                + goals.score() * WEIGHTS.get("goalAlignment")
                + skill * WEIGHTS.get("skillAlignment")
                + accessibility * WEIGHTS.get("entryAccessibility")
                + effort * WEIGHTS.get("learningEffortCompatibility");
        int indicator = clamp((int) Math.round(weightedSum / (double) AVAILABLE_EVIDENCE_WEIGHT));
        CareerFitFactors factors = new CareerFitFactors(
                interest.score(), goals.score(), skill, accessibility, effort, null);
        List<SkillGapResponse> gaps = skillGaps(career, studentSkills);
        return new ScoredCareer(
                career,
                indicator,
                factors,
                reasons(interest, goals, skill, accessibility, effort),
                strengths(career, interest, goals, studentSkills),
                gaps,
                uncertainties(profile),
                nextStep(gaps));
    }

    private int skillAlignment(Career career, Map<String, StudentSkill> studentSkills) {
        double achieved = 0;
        double possible = 0;
        for (CareerSkill careerSkill : career.getSkills()) {
            double requirementMultiplier = careerSkill.getRequirement() == SkillRequirement.REQUIRED ? 1.0 : 0.6;
            double weight = careerSkill.getImportance() * requirementMultiplier;
            possible += weight;
            StudentSkill current = studentSkills.get(careerSkill.getSkill().getNormalizedName());
            if (current != null) {
                achieved += weight * proficiencyValue(current.getProficiency()) * confidenceValue(current.getConfidence());
            }
        }
        return possible == 0 ? 0 : clamp((int) Math.round(achieved / possible * 100));
    }

    private List<SkillGapResponse> skillGaps(Career career, Map<String, StudentSkill> studentSkills) {
        return career.getSkills().stream()
                .map(careerSkill -> {
                    StudentSkill current = studentSkills.get(careerSkill.getSkill().getNormalizedName());
                    boolean gap = current == null || proficiencyValue(current.getProficiency()) < 0.75;
                    if (!gap) {
                        return null;
                    }
                    return new SkillGapResponse(
                            careerSkill.getSkill().getId(),
                            careerSkill.getSkill().getName(),
                            careerSkill.getSkill().getCategory(),
                            careerSkill.getImportance(),
                            careerSkill.getRequirement(),
                            current == null ? null : current.getProficiency(),
                            gapPriority(careerSkill));
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator
                        .comparing((SkillGapResponse gap) -> priorityRank(gap.priority()))
                        .thenComparing(Comparator.comparingInt(SkillGapResponse::importance).reversed())
                        .thenComparing(SkillGapResponse::name))
                .limit(6)
                .toList();
    }

    private List<String> strengths(
            Career career,
            MatchResult interests,
            MatchResult goals,
            Map<String, StudentSkill> studentSkills) {
        List<String> strengths = new ArrayList<>();
        if (!interests.matches().isEmpty()) {
            strengths.add("Interest overlap: " + String.join(", ", interests.matches()));
        }
        if (!goals.matches().isEmpty()) {
            strengths.add("Goal overlap: " + String.join(", ", goals.matches()));
        }
        List<String> matchedSkills = career.getSkills().stream()
                .filter(item -> studentSkills.containsKey(item.getSkill().getNormalizedName()))
                .sorted(Comparator.comparingInt(CareerSkill::getImportance).reversed()
                        .thenComparing(item -> item.getSkill().getName()))
                .map(item -> item.getSkill().getName())
                .limit(4)
                .toList();
        if (!matchedSkills.isEmpty()) {
            strengths.add("Existing relevant skills: " + String.join(", ", matchedSkills));
        }
        if (strengths.isEmpty()) {
            strengths.add("No strong profile overlap is recorded yet; this path remains available for exploration.");
        }
        return strengths;
    }

    private List<String> reasons(
            MatchResult interests, MatchResult goals, int skills, int accessibility, int effort) {
        return List.of(
                factorReason("Interest alignment", interests.score()),
                factorReason("Goal alignment", goals.score()),
                factorReason("Current skill alignment", skills),
                factorReason("Entry accessibility", accessibility),
                factorReason("Available weekly learning time", effort));
    }

    private String factorReason(String name, int score) {
        String level = score >= 75 ? "strong" : score >= 50 ? "moderate" : score >= 25 ? "limited" : "not yet established";
        return name + " is " + level + " from the information currently recorded.";
    }

    private MatchResult alignment(List<String> inputs, Set<String> signals) {
        if (inputs.isEmpty() || signals.isEmpty()) {
            return new MatchResult(0, List.of());
        }
        Set<String> normalizedInputs = new LinkedHashSet<>();
        inputs.stream().map(this::normalize).filter(value -> !value.isBlank()).forEach(normalizedInputs::add);
        List<String> matches = signals.stream()
                .filter(signal -> normalizedInputs.stream().anyMatch(input -> overlaps(input, normalize(signal))))
                .sorted()
                .toList();
        int score = switch (Math.min(matches.size(), 3)) {
            case 1 -> 60;
            case 2 -> 85;
            case 3 -> 100;
            default -> 0;
        };
        return new MatchResult(score, matches);
    }

    private boolean overlaps(String input, String signal) {
        if (input.contains(signal) || signal.contains(input)) {
            return true;
        }
        Set<String> inputTokens = new HashSet<>(Arrays.asList(input.split(" ")));
        for (String token : signal.split(" ")) {
            if (token.length() >= 4 && inputTokens.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, StudentSkill> studentSkillMap(StudentProfile profile) {
        Map<String, StudentSkill> result = new LinkedHashMap<>();
        profile.getSkills().forEach(skill -> result.put(skill.getSkill().getNormalizedName(), skill));
        return result;
    }

    @SafeVarargs
    private final List<String> combined(List<String>... sources) {
        List<String> result = new ArrayList<>();
        for (List<String> source : sources) {
            source.stream().filter(java.util.Objects::nonNull).filter(value -> !value.isBlank()).forEach(result::add);
        }
        return result;
    }

    private int accessibility(EntryDifficulty difficulty) {
        return switch (difficulty) {
            case LOW -> 90;
            case MEDIUM -> 70;
            case HIGH -> 45;
            case VERY_HIGH -> 25;
        };
    }

    private int effortCompatibility(Integer availableHours, int recommendedHours) {
        if (availableHours == null || recommendedHours <= 0) {
            return 0;
        }
        return clamp((int) Math.round(availableHours / (double) recommendedHours * 100));
    }

    private double proficiencyValue(SkillProficiency proficiency) {
        return switch (proficiency) {
            case AWARENESS -> 0.25;
            case BEGINNER -> 0.50;
            case INTERMEDIATE -> 0.75;
            case ADVANCED -> 1.00;
        };
    }

    private double confidenceValue(SkillConfidence confidence) {
        return switch (confidence) {
            case LOW -> 0.80;
            case MEDIUM -> 0.90;
            case HIGH -> 1.00;
        };
    }

    private String gapPriority(CareerSkill skill) {
        if (skill.getRequirement() == SkillRequirement.REQUIRED && skill.getImportance() >= 4) return "HIGH";
        if (skill.getRequirement() == SkillRequirement.REQUIRED || skill.getImportance() >= 4) return "MEDIUM";
        return "LATER";
    }

    private int priorityRank(String priority) {
        return switch (priority) {
            case "HIGH" -> 0;
            case "MEDIUM" -> 1;
            default -> 2;
        };
    }

    private String nextStep(List<SkillGapResponse> gaps) {
        return gaps.stream()
                .filter(gap -> "HIGH".equals(gap.priority()))
                .findFirst()
                .map(gap -> "Explore the foundations of " + gap.name() + " before adding later tools.")
                .orElse("Review this career's responsibilities and compare it with the listed alternatives.");
    }

    private List<String> uncertainties(StudentProfile profile) {
        List<String> result = new ArrayList<>();
        result.add("No validated market observations are available in Phase 2.");
        result.add("Skill levels are self-reported unless their source says otherwise.");
        if (profile.getTimeAvailablePerWeek() == null) {
            result.add("Weekly learning time is missing, so effort compatibility is not established.");
        }
        return result;
    }

    private List<String> profileLimitations(StudentProfile profile) {
        List<String> result = new ArrayList<>();
        if (profile.getInterests().isEmpty() && profile.getPreferredDomains().isEmpty()) result.add("No interests or preferred domains recorded.");
        if (profile.getGoals().isEmpty() && profile.getShortTermGoal() == null && profile.getLongTermGoal() == null) result.add("No career goals recorded.");
        if (profile.getSkills().isEmpty()) result.add("No current skills recorded.");
        if (profile.getTimeAvailablePerWeek() == null) result.add("No weekly learning-time constraint recorded.");
        return result;
    }

    private FitBand fitBand(int score) {
        if (score >= 75) return FitBand.STRONG;
        if (score >= 60) return FitBand.PROMISING;
        if (score >= 40) return FitBand.EXPLORATORY;
        return FitBand.LIMITED_CURRENT_ALIGNMENT;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#./ ]", " ")
                .replaceAll("\\s+", " ")
                .strip();
    }

    private static Map<String, Integer> weights() {
        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("interestAlignment", 20);
        values.put("goalAlignment", 15);
        values.put("skillAlignment", 30);
        values.put("marketCompatibility", 15);
        values.put("entryAccessibility", 15);
        values.put("learningEffortCompatibility", 5);
        return Map.copyOf(values);
    }

    private record MatchResult(int score, List<String> matches) {
    }

    private record ScoredCareer(
            Career career,
            int indicator,
            CareerFitFactors factors,
            List<String> reasons,
            List<String> strengths,
            List<SkillGapResponse> gaps,
            List<String> uncertainties,
            String nextStep) {

        CareerCandidateResponse toResponse(List<AlternativeCareerResponse> alternatives) {
            FitBand band = indicator >= 75 ? FitBand.STRONG
                    : indicator >= 60 ? FitBand.PROMISING
                    : indicator >= 40 ? FitBand.EXPLORATORY
                    : FitBand.LIMITED_CURRENT_ALIGNMENT;
            return new CareerCandidateResponse(
                    career.getId(), career.getSlug(), career.getName(), indicator, band,
                    factors, AVAILABLE_EVIDENCE_WEIGHT, reasons, strengths, gaps,
                    career.getRisks().stream().sorted().limit(3).toList(),
                    alternatives, uncertainties, nextStep);
        }
    }
}
