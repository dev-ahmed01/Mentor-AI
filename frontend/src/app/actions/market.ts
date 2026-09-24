"use server";

import { redirect } from "next/navigation";
import { getToken } from "@/lib/auth";
import { apiRequest, ApiClientError } from "@/lib/api/client";
import type { ActionState } from "@/lib/forms";
import type { MarketDecision } from "@/types/market";

export async function evaluateMarketAction(snapshotId: string, _previous: ActionState): Promise<ActionState> {
  void _previous;
  const token = await getToken();
  if (!token) redirect("/login");
  let saved: MarketDecision;
  try {
    saved = await apiRequest<MarketDecision>(`/api/market/snapshots/${encodeURIComponent(snapshotId)}/decisions`, { method: "POST", token });
  } catch (error) {
    return { error: error instanceof ApiClientError ? error.detail.message : "The evidence comparison could not be saved. Try again." };
  }
  redirect(`/market/decisions/${saved.id}`);
}
