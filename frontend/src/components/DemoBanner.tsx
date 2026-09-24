import Link from "next/link";
import type { DemoStatus } from "@/types/demo";
export function DemoBanner({ status }: { status: DemoStatus | null }) {
  if (!status) return <p className="callout" role="status">Account demo status is unavailable. Reload before presenting this account as real data.</p>;
  if (!status.run) return null;
  return <aside className="callout demo-banner" aria-label="Synthetic demo account"><div><strong>Synthetic demo account</strong><p>This profile and initial learning history were seeded for a demonstration. They do not describe a real student.</p></div><Link href="/demo" className="text-link">Open demo guide</Link></aside>;
}
