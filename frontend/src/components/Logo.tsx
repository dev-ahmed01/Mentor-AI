import Link from "next/link";

export function Logo() {
  return (
    <Link href="/" className="logo" aria-label="MentorAI home">
      <span aria-hidden="true" className="logo-mark">M</span>
      <span>MentorAI</span>
    </Link>
  );
}
