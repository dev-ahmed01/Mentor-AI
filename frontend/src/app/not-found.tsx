import Link from "next/link";

export default function NotFound() {
  return <main className="state-page"><p className="eyebrow">404</p><h1>That page is not part of this path.</h1><p>Return to the MentorAI starting point.</p><Link href="/" className="button button-primary">Go home</Link></main>;
}
