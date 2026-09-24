import Link from "next/link";
import type { Metadata } from "next";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { SkillPrerequisiteContext } from "@/components/SkillPrerequisiteContext";
import { getCareer, getCareerPrerequisites } from "@/lib/careers";

export const metadata: Metadata = { title: "Career reality" };

export default async function CareerDetailPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const career = await getCareer(slug);
  const prerequisites = new Map((await getCareerPrerequisites(career.id)).map((item) => [item.skillId, item]));

  return (
    <article className="career-detail">
      <Link href="/careers" className="back-link">← All careers</Link>
      <header className="career-detail-hero">
        <div>
          <p className="eyebrow">Career reality · {career.dataLabel}</p>
          <h1>{career.name}</h1>
          <p>{career.description}</p>
          <Link href={`/dashboard?careerId=${career.id}#learning-priorities`} className="button button-primary">See learning priorities</Link>
          <p><Link href={`/simulator?careerId=${career.id}`} className="text-link">Simulate what learning a skill could unlock</Link></p>
        </div>
        <Card className="reality-facts">
          <div><span>Entry difficulty</span><strong>{career.entryDifficulty.toLowerCase().replaceAll("_", " ")}</strong></div>
          <div><span>Suggested study time</span><strong>{career.recommendedWeeklyHours} hours/week</strong></div>
          <div><span>Market evidence</span><Link href={`/market?careerId=${career.id}`} className="text-link">Inspect source samples</Link></div>
        </Card>
      </header>

      <div className="reality-callout"><Badge>Reality check</Badge><p>{career.realitySummary}</p></div>

      <div className="career-detail-grid">
        <Card><h2>What the work involves</h2><ul>{career.responsibilities.map((item) => <li key={item}>{item}</li>)}</ul><h3>Common titles</h3><div className="skill-chip-list">{career.commonJobTitles.map((title) => <span key={title}>{title}</span>)}</div></Card>
        <Card>
          <h2>Core skill expectations</h2>
          <p>Prerequisites use a small illustrative learning graph: <strong>DEMO DATA</strong>. Beginner-level or higher recorded knowledge meets each prerequisite. These are self-reported foundations, not an assessment or hiring evidence.</p>
          <div className="career-skill-list">
            {career.skills.map((skill) => {
              const readiness = prerequisites.get(skill.id);
              return (
                <div key={skill.id}>
                  <div>
                    <strong>{skill.name}</strong><span>{skill.category}</span>
                    {readiness ? <SkillPrerequisiteContext readiness={readiness} /> : <small>Prerequisite context is unavailable.</small>}
                  </div>
                  <Badge>{skill.requirement.toLowerCase()} · {skill.importance}/5</Badge>
                </div>
              );
            })}
          </div>
        </Card>
        <Card><h2>Education and proof</h2><h3>Degree relevance</h3><p>{career.degreeRelevance}</p><h3>Projects</h3><p>{career.projectExpectations}</p><h3>Internships</h3><p>{career.internshipExpectations}</p></Card>
        <Card><h2>Risks and misconceptions</h2><p><strong>Common misconception:</strong> {career.commonMisconceptions}</p><ul>{career.risks.map((risk) => <li key={risk}>{risk}</li>)}</ul></Card>
      </div>

      <section className="market-boundary" aria-labelledby="market-boundary-heading">
        <p className="eyebrow">Evidence boundary</p><h2 id="market-boundary-heading">Questions to validate in your market</h2>
        <p>These catalog prompts are not demand claims. <Link href={`/market?careerId=${career.id}`} className="text-link">Inspect collected evidence and its source, location and sample limits</Link> before using a market comparison.</p>
        <ul>{career.marketConsiderations.map((item) => <li key={item}>{item}</li>)}</ul>
      </section>
    </article>
  );
}
