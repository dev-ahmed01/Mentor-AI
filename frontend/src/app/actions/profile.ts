"use server";

import { redirect } from "next/navigation";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import { csv, type ActionState } from "@/lib/forms";
import type { Profile } from "@/types/api";

function optionalNumber(value: FormDataEntryValue | null): number | undefined {
  const text = String(value ?? "").trim();
  return text ? Number(text) : undefined;
}

export async function saveProfileAction(_: ActionState, formData: FormData): Promise<ActionState> {
  const token = await getToken();
  if (!token) redirect("/login");

  const skills = csv(formData.get("skills")).map((name) => ({
    name,
    category: "Self-reported",
    proficiency: "BEGINNER",
    confidence: "MEDIUM",
    source: "SELF_REPORTED",
  }));
  const body = {
    degree: String(formData.get("degree") ?? ""),
    year: optionalNumber(formData.get("year")),
    semester: optionalNumber(formData.get("semester")),
    interests: csv(formData.get("interests")),
    goals: csv(formData.get("goals")),
    programmingLanguages: csv(formData.get("programmingLanguages")),
    preferredDomains: csv(formData.get("preferredDomains")),
    targetLocations: csv(formData.get("targetLocations")),
    remotePreference: String(formData.get("remotePreference") || "FLEXIBLE"),
    timeAvailablePerWeek: optionalNumber(formData.get("timeAvailablePerWeek")),
    skills,
    currentProjects: csv(formData.get("currentProjects")),
    experience: String(formData.get("experience") ?? ""),
    certifications: csv(formData.get("certifications")),
    shortTermGoal: String(formData.get("shortTermGoal") ?? ""),
    longTermGoal: String(formData.get("longTermGoal") ?? ""),
    avoidances: csv(formData.get("avoidances")),
  };
  try {
    await apiRequest<Profile>("/api/profile", {
      method: "PUT",
      token,
      body: JSON.stringify(body),
    });
  } catch (error) {
    if (error instanceof ApiClientError) {
      return { error: error.detail.message, fieldErrors: error.detail.fieldErrors };
    }
    return { error: "Your profile could not be saved." };
  }
  redirect("/dashboard");
}
