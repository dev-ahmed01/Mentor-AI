import Link from "next/link";
import { logoutAction } from "@/app/actions/auth";
import { Logo } from "@/components/Logo";
import type { User } from "@/types/api";

export function AppNav({ user }: { user: User }) {
  return (
    <header className="app-header">
      <div className="app-header-inner">
        <Logo />
        <nav aria-label="Primary navigation">
          <Link href="/dashboard">Dashboard</Link>
          <Link href="/roadmap">Roadmap</Link>
          <Link href="/progress">Weekly check-in</Link>
          <details className="app-tools"><summary>More tools</summary><div className="app-tools-links">
            <Link href="/careers">Careers</Link>
            <Link href="/pivot">Career pivot</Link>
            <Link href="/simulator">Simulator</Link>
            <Link href="/market">Market evidence</Link>
            <Link href="/jobs/analyze">Analyze a job</Link>
            <Link href="/mentor">Mentor</Link>
            <Link href="/profile">Profile</Link>
            <Link href="/demo">Demo guide</Link>
          </div></details>
        </nav>
        <div className="account-menu">
          <span className="account-name">{user.displayName}</span>
          <form action={logoutAction}><button className="text-button" type="submit">Sign out</button></form>
        </div>
      </div>
    </header>
  );
}
