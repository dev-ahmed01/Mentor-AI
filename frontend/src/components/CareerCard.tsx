import Link from "next/link";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import type { CareerSummary } from "@/types/api";

const difficultyLabels = {
  LOW: "Accessible entry",
  MEDIUM: "Moderate entry",
  HIGH: "Demanding entry",
  VERY_HIGH: "Very demanding entry",
} as const;

export function CareerCard({ career }: { career: CareerSummary }) {
  return (
    <Card className="career-card">
      <div className="card-heading">
        <Badge>{difficultyLabels[career.entryDifficulty]}</Badge>
        <span>{career.recommendedWeeklyHours} hrs/week suggested</span>
      </div>
      <h2>{career.name}</h2>
      <p>{career.description}</p>
      <div className="skill-chip-list" aria-label="Core skills">
        {career.coreSkills.map((skill) => <span key={skill.id}>{skill.name}</span>)}
      </div>
      <Link className="text-link" href={`/careers/${career.slug}`}>
        See career reality <span aria-hidden="true">→</span>
      </Link>
    </Card>
  );
}
