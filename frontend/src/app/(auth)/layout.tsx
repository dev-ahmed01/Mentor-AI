import { redirect } from "next/navigation";
import { Logo } from "@/components/Logo";
import { getCurrentUser } from "@/lib/auth";

export default async function AuthLayout({ children }: { children: React.ReactNode }) {
  if (await getCurrentUser()) redirect("/dashboard");
  return (
    <main className="auth-shell">
      <div className="auth-brand"><Logo /></div>
      {children}
    </main>
  );
}
