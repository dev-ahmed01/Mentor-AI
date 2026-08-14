import type { Metadata } from "next";
import { OnboardingForm } from "@/components/OnboardingForm";

export const metadata: Metadata = { title: "Onboarding" };

export default function OnboardingPage() {
  return <div className="onboarding-shell"><OnboardingForm /></div>;
}
