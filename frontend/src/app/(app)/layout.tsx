import { redirect } from "next/navigation";
import { AppNav } from "@/components/AppNav";
import { getCurrentUser } from "@/lib/auth";
import { getDemoStatus } from "@/lib/demo";
import { DemoBanner } from "@/components/DemoBanner";

export default async function AppLayout({ children }: { children: React.ReactNode }) {
  const [user, demo] = await Promise.all([getCurrentUser(), getDemoStatus()]);
  if (!user) redirect("/login");
  return (
    <div className="app-shell">
      <a href="#main-content" className="skip-link">Skip to main content</a>
      <AppNav user={user} />
      <main className="app-main" id="main-content" tabIndex={-1}><DemoBanner status={demo} />{children}</main>
    </div>
  );
}
