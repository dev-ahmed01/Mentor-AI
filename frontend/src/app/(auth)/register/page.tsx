import type { Metadata } from "next";
import { registerAction } from "@/app/actions/auth";
import { AuthForm } from "@/components/AuthForm";

export const metadata: Metadata = { title: "Create account" };

export default function RegisterPage() {
  return (
    <section className="auth-card" aria-labelledby="register-title">
      <p className="eyebrow">Your starting point</p>
      <h1 id="register-title">Create your MentorAI account</h1>
      <p>We only ask for information that helps personalize your career navigation.</p>
      <AuthForm mode="register" action={registerAction} />
    </section>
  );
}
