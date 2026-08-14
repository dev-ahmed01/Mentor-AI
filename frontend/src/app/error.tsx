"use client";

import { Button } from "@/components/ui/Button";

export default function ErrorPage({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return <main className="state-page"><p className="eyebrow">Something went wrong</p><h1>We could not load this page.</h1><p>Your information has not been changed. Try the request again.</p><Button onClick={reset}>Try again</Button></main>;
}
