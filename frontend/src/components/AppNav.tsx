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
          <Link href="/careers">Careers</Link>
          <Link href="/roadmap">Roadmap</Link>
          <Link href="/progress">Weekly check-in</Link>
          <Link href="/simulator">Simulator</Link>
          <Link href="/profile">Profile</Link>
        </nav>
        <div className="account-menu">
          <span className="account-name">{user.displayName}</span>
          <form action={logoutAction}><button className="text-button" type="submit">Sign out</button></form>
        </div>
      </div>
    </header>
  );
}
