import Link from "next/link";
import type { Metadata } from "next";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { getProfile } from "@/lib/profile";

export const metadata: Metadata = { title: "Dashboard" };

export default async function DashboardPage() {
  const profile = await getProfile();
  const hasProfile = Boolean(profile.degree || profile.interests.length || profile.skills.length);
  return (
    <div className="dashboard-page">
      <div className="page-heading">
        <div><p className="eyebrow">Your navigation workspace</p><h1>Career dashboard</h1></div>
        <Badge>{hasProfile ? "Profile ready" : "Profile incomplete"}</Badge>
      </div>

      {!hasProfile && (
        <div className="callout" role="status">
          <div><strong>Start with your profile.</strong><p>Your career analysis needs a reliable starting point.</p></div>
          <Link href="/onboarding" className="button button-primary">Complete onboarding</Link>
        </div>
      )}

      <div className="dashboard-grid">
        <Card className="dashboard-primary">
          <div className="card-heading"><span>Current direction</span><Badge>Not calculated yet</Badge></div>
          <h2>Career analysis comes next</h2>
          <p>Phase 1 deliberately does not invent a career fit or market signal. Your saved profile will feed documented deterministic scoring and evidence-aware analysis in the next phase.</p>
          <div className="priority-block">
            <span className="eyebrow">Current priority</span>
            <strong>{hasProfile ? "Review your starting profile" : "Complete onboarding"}</strong>
            <p>{profile.skills.length} skills and {profile.interests.length} interests currently recorded.</p>
          </div>
          <Link href="/profile" className="text-link">Review profile <span aria-hidden="true">→</span></Link>
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
          <div className="card-heading"><span>Product status</span><Badge>Foundation</Badge></div>
          <h3>No roadmap has been generated</h3>
          <p>This is intentional: roadmap generation will be added with skill dependencies, validation, and explicit “not yet” priorities.</p>
          <div className="empty-state">Not implemented in Phase 1</div>
        </Card>
      </div>
    </div>
  );
}
