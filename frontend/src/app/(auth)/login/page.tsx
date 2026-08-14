import type { Metadata } from "next";
import { loginAction } from "@/app/actions/auth";
import { AuthForm } from "@/components/AuthForm";

export const metadata: Metadata = { title: "Sign in" };

export default function LoginPage() {
  return (
    <section className="auth-card" aria-labelledby="login-title">
      <p className="eyebrow">Welcome back</p>
      <h1 id="login-title">Continue your direction</h1>
      <p>Sign in to update your profile and continue from your current priorities.</p>
      <AuthForm mode="login" action={loginAction} />
    </section>
  );
}
