"use client";

import Link from "next/link";
import { useActionState } from "react";
import { analyzeCareersAction, type CareerAnalysisState } from "@/app/actions/careers";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { SubmitButton } from "@/components/ui/SubmitButton";
import type { CareerCandidate, CareerFitFactors } from "@/types/api";

const initialState: CareerAnalysisState = {};
const factorLabels: Array<[keyof CareerFitFactors, string]> = [
  ["interestAlignment", "Interests"],
  ["goalAlignment", "Goals"],
  ["skillAlignment", "Current skills"],
  ["entryAccessibility", "Entry access"],
  ["learningEffortCompatibility", "Weekly effort"],
];

function readableBand(value: string): string {
  return value.toLowerCase().replaceAll("_", " ");
}

function CandidateResult({ candidate, rank }: { candidate: CareerCandidate; rank: number }) {
  return (
    <Card className="candidate-card">
      <div className="candidate-score-row">
        <div>
          <span className="eyebrow">Candidate {rank}</span>
          <h3>{candidate.careerName}</h3>
          <Badge>{readableBand(candidate.fitBand)}</Badge>
        </div>
        <div className="score-ring" aria-label={`Career Fit Indicator ${candidate.careerFitIndicator} out of 100`}>
          <strong>{candidate.careerFitIndicator}</strong><span>/100</span>
        </div>
      </div>

      <div className="factor-grid" aria-label="Factor scores">
        {factorLabels.map(([key, label]) => (
          <div key={key}>
            <span>{label}</span>
            <meter min="0" max="100" value={candidate.factors[key] ?? 0}>{candidate.factors[key] ?? 0}</meter>
            <strong>{candidate.factors[key] ?? 0}</strong>
          </div>
        ))}
      </div>

      <div className="analysis-columns">
        <div>
          <h4>What supports this</h4>
          <ul>{candidate.strengths.map((item) => <li key={item}>{item}</li>)}</ul>
        </div>
        <div>
          <h4>Priority gaps</h4>
          {candidate.skillGaps.length > 0 ? (
            <ul>{candidate.skillGaps.slice(0, 4).map((gap) => <li key={gap.skillId}>{gap.name} <small>({gap.priority.toLowerCase()})</small></li>)}</ul>
          ) : <p>No priority skill gaps are visible from the current catalog.</p>}
        </div>
      </div>

      <div className="next-step"><span>Next honest step</span><strong>{candidate.nextStep}</strong></div>
      <div className="candidate-links">
        <Link className="text-link" href={`/careers/${candidate.slug}`}>Open career reality <span aria-hidden="true">→</span></Link>
        <span>Alternatives: {candidate.alternatives.map((item, index) => (
          <span key={item.id}>{index > 0 ? ", " : ""}<Link href={`/careers/${item.slug}`}>{item.name}</Link></span>
        ))}</span>
      </div>
    </Card>
  );
}

export function CareerAnalysisPanel() {
  const [state, formAction] = useActionState(analyzeCareersAction, initialState);
  const analysis = state.analysis;

  return (
    <section className="analysis-section" aria-labelledby="career-analysis-heading">
      <div className="analysis-intro">
        <div>
          <p className="eyebrow">Profile-based comparison</p>
          <h2 id="career-analysis-heading">Find plausible directions</h2>
          <p>This indicator compares your saved interests, goals, skills, entry accessibility, and weekly time. It is guidance for exploration—not a probability of employment.</p>
        </div>
        <form action={formAction}><SubmitButton idle="Analyze my profile" pending="Comparing careers…" /></form>
      </div>

      {state.error ? <div className="form-error" role="alert">{state.error} <Link href="/profile">Review your profile</Link></div> : null}

      {analysis ? (
        <div className="analysis-results" aria-live="polite">
          <div className="evidence-notice">
            <Badge>85% evidence coverage</Badge>
            <div><strong>Market evidence is intentionally excluded.</strong><p>{analysis.methodology}</p></div>
          </div>
          {analysis.profileLimitations.length > 0 ? (
            <div className="profile-limitations"><strong>Improve this comparison</strong><ul>{analysis.profileLimitations.map((item) => <li key={item}>{item}</li>)}</ul></div>
          ) : null}
          <div className="candidate-list">
            {analysis.candidates.map((candidate, index) => <CandidateResult key={candidate.careerId} candidate={candidate} rank={index + 1} />)}
          </div>
          <p className="method-note">Calculation {analysis.calculationVersion}. Scores are reproducible from the current profile and controlled catalog; no live hiring or salary data was used.</p>
        </div>
      ) : null}
    </section>
  );
}
