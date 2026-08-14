"use client";

import { useActionState } from "react";
import { saveProfileAction } from "@/app/actions/profile";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState } from "@/lib/forms";
import type { Profile } from "@/types/api";

function joined(values: string[]) {
  return values.join(", ");
}

export function ProfileEditor({ profile }: { profile: Profile }) {
  const [state, formAction] = useActionState(saveProfileAction, initialActionState);
  return (
    <form action={formAction} className="card form-stack profile-form">
      <div className="form-grid">
        <label className="field"><span>Degree</span><input name="degree" defaultValue={profile.degree} /></label>
        <label className="field"><span>Year</span><input name="year" type="number" min={1} max={8} defaultValue={profile.year} /></label>
        <label className="field"><span>Semester</span><input name="semester" type="number" min={1} max={16} defaultValue={profile.semester} /></label>
        <label className="field"><span>Hours per week</span><input name="timeAvailablePerWeek" type="number" min={1} max={168} defaultValue={profile.timeAvailablePerWeek} /></label>
      </div>
      <label className="field"><span>Skills</span><input name="skills" defaultValue={joined(profile.skills.map((skill) => skill.name))} /></label>
      <label className="field"><span>Programming languages</span><input name="programmingLanguages" defaultValue={joined(profile.programmingLanguages)} /></label>
      <label className="field"><span>Interests</span><input name="interests" defaultValue={joined(profile.interests)} /></label>
      <label className="field"><span>Goals</span><input name="goals" defaultValue={joined(profile.goals)} /></label>
      <label className="field"><span>Preferred domains</span><input name="preferredDomains" defaultValue={joined(profile.preferredDomains)} /></label>
      <label className="field"><span>Target locations</span><input name="targetLocations" defaultValue={joined(profile.targetLocations)} /></label>
      <label className="field"><span>Work preference</span>
        <select name="remotePreference" defaultValue={profile.remotePreference ?? "FLEXIBLE"}>
          <option value="FLEXIBLE">Flexible</option><option value="REMOTE">Remote</option>
          <option value="HYBRID">Hybrid</option><option value="ON_SITE">On-site</option>
        </select>
      </label>
      <label className="field"><span>Current projects</span><input name="currentProjects" defaultValue={joined(profile.currentProjects)} /></label>
      <label className="field"><span>Experience</span><textarea name="experience" rows={3} defaultValue={profile.experience} /></label>
      <label className="field"><span>Certifications</span><input name="certifications" defaultValue={joined(profile.certifications)} /></label>
      <label className="field"><span>Short-term goal</span><textarea name="shortTermGoal" rows={3} defaultValue={profile.shortTermGoal} /></label>
      <label className="field"><span>Long-term goal</span><textarea name="longTermGoal" rows={3} defaultValue={profile.longTermGoal} /></label>
      <label className="field"><span>Things to avoid</span><input name="avoidances" defaultValue={joined(profile.avoidances)} /></label>
      {state.error && <div className="form-error" role="alert">{state.error}</div>}
      <div><SubmitButton idle="Save changes" pending="Saving changes…" /></div>
    </form>
  );
}
