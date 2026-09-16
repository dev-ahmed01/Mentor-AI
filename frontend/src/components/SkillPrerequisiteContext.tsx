import type { SkillPrerequisites } from "@/types/api";

export function SkillPrerequisiteContext({ readiness }: { readiness: SkillPrerequisites }) {
  if (readiness.coverage === "NO_RECORDED_PREREQUISITES") {
    return <small>No prerequisites recorded in this starter graph.</small>;
  }

  return (
    <details className="skill-prerequisites">
      <summary>
        {readiness.eligible ? "Recorded foundations meet the prerequisites" : "Foundations to review"}
        <span className="sr-only"> for {readiness.name}</span>
      </summary>
      <ul>
        {readiness.prerequisites.map((prerequisite) => (
          <li key={prerequisite.skillId}>
            <strong>{prerequisite.name}</strong>
            {prerequisite.direct ? "" : " (earlier foundation)"}
            {" — "}
            {prerequisite.satisfied
              ? `Recorded: ${prerequisite.currentProficiency?.toLowerCase()}`
              : prerequisite.currentProficiency
                ? "Awareness only; practice the basics first"
                : "Not recorded in your profile"}
          </li>
        ))}
      </ul>
    </details>
  );
}
