import "server-only";

import type { ApiError } from "@/types/api";

const API_URL = process.env.API_URL ?? process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ApiClientError extends Error {
  constructor(public readonly detail: ApiError) {
    super(detail.message);
    this.name = "ApiClientError";
  }
}

type RequestOptions = RequestInit & { token?: string };

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");
  if (options.body) {
    headers.set("Content-Type", "application/json");
  }
  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers,
      cache: "no-store",
      signal: options.signal ?? AbortSignal.timeout(60_000),
    });
  } catch {
    throw new ApiClientError({
      status: 503,
      code: "API_UNAVAILABLE",
      message: "MentorAI is temporarily unavailable. Please try again shortly.",
    });
  }

  if (!response.ok) {
    const fallback: ApiError = {
      status: response.status,
      code: "REQUEST_FAILED",
      message: "The request could not be completed.",
    };
    const detail = (await response.json().catch(() => fallback)) as ApiError;
    throw new ApiClientError(detail);
  }
  return (await response.json()) as T;
}
