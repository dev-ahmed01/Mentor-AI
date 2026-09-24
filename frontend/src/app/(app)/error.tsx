"use client";
import Link from "next/link";
export default function ErrorPage({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return <section className="card" aria-labelledby="workspace-error"><h1 id="workspace-error">Your workspace could not be loaded</h1><p role="alert">The service may be temporarily unavailable. Your saved work has not been cleared.</p><button className="button button-primary" onClick={reset}>Try again</button><p><Link href="/dashboard">Return to dashboard</Link></p></section>;
}
