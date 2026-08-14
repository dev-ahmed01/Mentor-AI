export type ActionState = {
  error?: string;
  fieldErrors?: Record<string, string>;
};

export const initialActionState: ActionState = {};

export function csv(value: FormDataEntryValue | null): string[] {
  return String(value ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}
