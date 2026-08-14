import { redirect } from "next/navigation";
import { AppNav } from "@/components/AppNav";
import { getCurrentUser } from "@/lib/auth";

export default async function AppLayout({ children }: { children: React.ReactNode }) {
  const user = await getCurrentUser();
  if (!user) redirect("/login");
  return (
    <div className="app-shell">
      <AppNav user={user} />
      <main className="app-main">{children}</main>
    </div>
  );
}
