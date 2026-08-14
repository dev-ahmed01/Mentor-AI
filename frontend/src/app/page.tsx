import Link from "next/link";
import { Badge } from "@/components/ui/Badge";
import { Card } from "@/components/ui/Card";
import { Logo } from "@/components/Logo";

export default function HomePage() {
  return (
    <main>
      <header className="marketing-header">
        <Logo />
        <nav aria-label="Public navigation">
          <Link href="/login" className="button button-quiet">Sign in</Link>
          <Link href="/register" className="button button-primary">Get started</Link>
        </nav>
      </header>

      <section className="hero shell">
        <div className="hero-copy">
          <Badge>Career decisions, with evidence</Badge>
          <h1>Know what to learn next—and why it matters.</h1>
          <p className="hero-lede">MentorAI connects your interests, skills, goals, and constraints to realistic career paths. It is designed to explain options, surface gaps, and keep you in control.</p>
          <div className="hero-actions">
            <Link href="/register" className="button button-primary">Build your profile</Link>
            <a href="#principles" className="button button-secondary">How it works</a>
          </div>
          <p className="microcopy">Local-first AI architecture. No job or salary guarantees. No invented market data.</p>
        </div>
        <Card className="direction-card">
          <div className="direction-topline"><span>Current direction</span><Badge>Student controlled</Badge></div>
          <h2>Start with your profile</h2>
          <p>Career analysis is built from information you provide, documented scoring rules, and traceable market evidence.</p>
          <div className="priority-block">
            <span className="eyebrow">First priority</span>
            <strong>Understand where you are now</strong>
            <p>Capture education, interests, current skills, constraints, and goals.</p>
          </div>
          <div className="evidence-row"><span>Observed</span><strong>Your profile</strong></div>
          <div className="evidence-row"><span>Calculated later</span><strong>Fit and skill gaps</strong></div>
          <div className="evidence-row"><span>AI interpretation</span><strong>Clearly labeled</strong></div>
        </Card>
      </section>

      <section id="principles" className="principles shell" aria-labelledby="principles-title">
        <div><p className="eyebrow">A trustworthy foundation</p><h2 id="principles-title">Designed around better decisions</h2></div>
        <div className="principle-grid">
          <Card><span className="card-number">01</span><h3>Several plausible paths</h3><p>MentorAI will compare alternatives instead of prescribing one “correct” career.</p></Card>
          <Card><span className="card-number">02</span><h3>Evidence before confidence</h3><p>Market claims need a source, collection date, scope, and honest sample-size warning.</p></Card>
          <Card><span className="card-number">03</span><h3>Priorities, not overload</h3><p>Your roadmap will separate what matters now, what comes next, and what not to learn yet.</p></Card>
        </div>
      </section>
    </main>
  );
}
