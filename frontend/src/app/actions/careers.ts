"use server";

import { redirect } from "next/navigation";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import { getToken } from "@/lib/auth";
import type { CareerAnalysis } from "@/types/api";

export type CareerAnalysisState = {
  analysis?: CareerAnalysis;
  error?: string;
};

export async function analyzeCareersAction(
  _previousState: CareerAnalysisState,
  _formData: FormData,
): Promise<CareerAnalysisState> {
  void _previousState;
  void _formData;
  const token = await getToken();
  if (!token) redirect("/login");
  try {
    const analysis = await apiRequest<CareerAnalysis>("/api/careers/analyze?limit=5", {
      method: "POST",
      token,
    });
    return { analysis };
  } catch (error) {
    if (error instanceof ApiClientError) return { error: error.detail.message };
    return { error: "Career analysis could not be completed. Please try again." };
  }
}
