import Link from "next/link";
import type { Metadata } from "next";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { getProfile } from "@/lib/profile";
import { LearningPriorities } from "@/components/LearningPriorities";

export const metadata: Metadata = { title: "Dashboard" };

export default async function DashboardPage({ searchParams }: { searchParams: Promise<{ careerId?: string | string[] }> }) {
  const { careerId } = await searchParams;
  const profile = await getProfile();
  const hasProfile = Boolean(profile.degree || profile.interests.length || profile.skills.length);
  return (
    <div className="dashboard-page">
      <div className="page-heading">
        <div><p className="eyebrow">Your navigation workspace</p><h1>Career dashboard</h1></div>
        <Badge>{hasProfile ? "Profile ready" : "Profile incomplete"}</Badge>
      </div>

      {!hasProfile ? (
        <div className="callout" role="status">
          <div><strong>Start with your profile.</strong><p>Your career analysis needs a reliable starting point.</p></div>
          <Link href="/onboarding" className="button button-primary">Complete onboarding</Link>
        </div>
      ) : null}

      <LearningPriorities careerId={careerId} />

      <div className="dashboard-grid">
        <Card className="dashboard-primary">
          <div className="card-heading"><span>Career intelligence</span><Badge>Phase 2 available</Badge></div>
          <h2>Compare plausible career directions</h2>
          <p>Your profile can now be compared with ten controlled career paths. The indicator is deterministic and explicitly excludes unverified market data.</p>
          <div className="priority-block">
            <span className="eyebrow">Recommended next step</span>
            <strong>{hasProfile ? "Run your career comparison" : "Complete onboarding first"}</strong>
            <p>{profile.skills.length} skills and {profile.interests.length} interests currently recorded.</p>
          </div>
          <Link href={hasProfile ? "/careers" : "/onboarding"} className="text-link">{hasProfile ? "Explore careers" : "Complete profile"} <span aria-hidden="true">→</span></Link>
        </Card>

        <Card>
          <div className="card-heading"><span>Profile signal</span><Badge>Observed</Badge></div>
          <dl className="profile-summary">
            <div><dt>Education</dt><dd>{profile.degree ? `${profile.degree}${profile.year ? ` · Year ${profile.year}` : ""}` : "Not provided"}</dd></div>
            <div><dt>Weekly time</dt><dd>{profile.timeAvailablePerWeek ? `${profile.timeAvailablePerWeek} hours` : "Not provided"}</dd></div>
            <div><dt>Skills</dt><dd>{profile.skills.length || "None yet"}</dd></div>
            <div><dt>Interests</dt><dd>{profile.interests.length || "None yet"}</dd></div>
          </dl>
        </Card>

        <Card className="roadmap-preview">
          <div className="card-heading"><span>Your learning path</span><Badge>Roadmaps available</Badge></div>
          <h3>Turn priorities into a plan</h3>
          <p>Create an ordered roadmap, adjust task effort, and record progress at a pace that fits your available time.</p>
          <Link href="/roadmap" className="text-link">Open or create your roadmap</Link>
          <p><Link href="/progress" className="text-link">Plan your week or check in</Link></p>
        </Card>
      </div>
    </div>
  );
}
