"use client";

import Link from "next/link";
import { useActionState } from "react";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState, type ActionState } from "@/lib/forms";

type AuthAction = (state: ActionState, formData: FormData) => Promise<ActionState>;

export function AuthForm({ mode, action }: { mode: "login" | "register"; action: AuthAction }) {
  const [state, formAction] = useActionState(action, initialActionState);
  const isRegister = mode === "register";

  return (
    <form action={formAction} className="form-stack" noValidate>
      {isRegister && (
        <label className="field">
          <span>Display name</span>
          <input name="displayName" autoComplete="name" required maxLength={100} />
          {state.fieldErrors?.displayName && <small className="field-error">{state.fieldErrors.displayName}</small>}
        </label>
      )}
      <label className="field">
        <span>Email</span>
        <input name="email" type="email" autoComplete="email" required maxLength={320} />
        {state.fieldErrors?.email && <small className="field-error">{state.fieldErrors.email}</small>}
      </label>
      <label className="field">
        <span>Password</span>
        <input
          name="password"
          type="password"
          autoComplete={isRegister ? "new-password" : "current-password"}
          minLength={12}
          maxLength={128}
          required
          aria-describedby={isRegister ? "password-help" : undefined}
        />
        {isRegister && <small id="password-help">Use at least 12 characters.</small>}
        {state.fieldErrors?.password && <small className="field-error">{state.fieldErrors.password}</small>}
      </label>
      {state.error && <div className="form-error" role="alert">{state.error}</div>}
      <SubmitButton
        idle={isRegister ? "Create account" : "Sign in"}
        pending={isRegister ? "Creating account…" : "Signing in…"}
      />
      <p className="form-switch">
        {isRegister ? "Already have an account?" : "New to MentorAI?"}{" "}
        <Link href={isRegister ? "/login" : "/register"}>
          {isRegister ? "Sign in" : "Create an account"}
        </Link>
      </p>
    </form>
  );
}
