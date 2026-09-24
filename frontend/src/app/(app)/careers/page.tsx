import type { Metadata } from "next";
import { CareerAnalysisPanel } from "@/components/CareerAnalysisPanel";
import { CareerCard } from "@/components/CareerCard";
import { Badge } from "@/components/ui/Badge";
import { getCareers } from "@/lib/careers";

export const metadata: Metadata = { title: "Career explorer" };

export default async function CareersPage() {
  const careers = await getCareers();
  return (
    <div className="careers-page">
      <div className="page-heading career-heading">
        <div>
          <p className="eyebrow">Phase 2 · Career intelligence</p>
          <h1>Explore the work, not just the title.</h1>
          <p>Ten structured career paths with expectations, entry difficulty, risks, and foundational skills. Catalog content is controlled guidance, not live market evidence.</p>
        </div>
        <Badge>{careers.length} career paths</Badge>
      </div>

      <CareerAnalysisPanel />

      <section aria-labelledby="catalog-heading" className="catalog-section">
        <div className="section-heading"><div><p className="eyebrow">Controlled catalog</p><h2 id="catalog-heading">Compare career realities</h2></div><p>Use these pages to challenge assumptions before committing to a roadmap.</p></div>
        <div className="career-grid">{careers.map((career) => <CareerCard key={career.id} career={career} />)}</div>
      </section>
    </div>
  );
}
