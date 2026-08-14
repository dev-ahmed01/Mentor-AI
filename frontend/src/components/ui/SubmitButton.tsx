"use client";

import { useFormStatus } from "react-dom";
import { Button } from "@/components/ui/Button";

export function SubmitButton({ idle, pending }: { idle: string; pending: string }) {
  const status = useFormStatus();
  return (
    <Button type="submit" disabled={status.pending} aria-disabled={status.pending}>
      {status.pending ? pending : idle}
    </Button>
  );
}
