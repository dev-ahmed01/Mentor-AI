"use client";

import Link from "next/link";
import { useActionState, useState } from "react";
import { saveProfileAction } from "@/app/actions/profile";
import { Button } from "@/components/ui/Button";
import { SubmitButton } from "@/components/ui/SubmitButton";
import { initialActionState } from "@/lib/forms";

const steps = [
  { title: "Education", hint: "Tell us where you are now." },
  { title: "Current skills", hint: "A starting point, not a test." },
  { title: "Interests", hint: "What kinds of work hold your attention?" },
  { title: "Career goals", hint: "What outcome are you working toward?" },
  { title: "Preferences", hint: "Add practical constraints and preferences." },
  { title: "Time", hint: "Choose a sustainable weekly pace." },
  { title: "Review", hint: "You can change every answer later." },
];

export function OnboardingForm() {
  const [step, setStep] = useState(0);
  const [state, formAction] = useActionState(saveProfileAction, initialActionState);
  const lastStep = step === steps.length - 1;

  return (
    <form action={formAction} className="onboarding-card">
      <div className="step-header">
        <div>
          <p className="eyebrow">Step {step + 1} of {steps.length}</p>
          <h1>{steps[step].title}</h1>
          <p>{steps[step].hint}</p>
        </div>
        <div className="step-meter" aria-label={`Onboarding step ${step + 1} of ${steps.length}`}>
          <span style={{ width: `${((step + 1) / steps.length) * 100}%` }} />
        </div>
      </div>

      <div hidden={step !== 0} className="form-grid">
        <label className="field"><span>Degree</span><input name="degree" placeholder="BCA" maxLength={120} /></label>
        <label className="field"><span>Current year</span><input name="year" type="number" min={1} max={8} placeholder="2" /></label>
        <label className="field"><span>Semester</span><input name="semester" type="number" min={1} max={16} placeholder="3" /></label>
      </div>

      <div hidden={step !== 1} className="form-stack">
        <label className="field"><span>Skills, separated by commas</span><input name="skills" placeholder="Java, SQL, Git" /></label>
        <label className="field"><span>Programming languages</span><input name="programmingLanguages" placeholder="Java, JavaScript" /></label>
        <label className="field"><span>Current projects</span><input name="currentProjects" placeholder="Student REST API, portfolio" /></label>
        <label className="field"><span>Experience</span><textarea name="experience" rows={3} placeholder="Coursework, internships, or projects" /></label>
      </div>

      <div hidden={step !== 2} className="form-stack">
        <label className="field"><span>Interests</span><input name="interests" placeholder="Backend development, problem solving" /></label>
        <label className="field"><span>Preferred domains</span><input name="preferredDomains" placeholder="Backend, data engineering" /></label>
        <label className="field"><span>Things you would rather avoid</span><input name="avoidances" placeholder="Heavy visual design, too many tools at once" /></label>
      </div>

      <div hidden={step !== 3} className="form-stack">
        <label className="field"><span>Goals, separated by commas</span><input name="goals" placeholder="Get an internship, become job-ready" /></label>
        <label className="field"><span>Short-term goal</span><textarea name="shortTermGoal" rows={3} placeholder="Build and deploy a Spring Boot API" /></label>
        <label className="field"><span>Long-term goal</span><textarea name="longTermGoal" rows={3} placeholder="Start a backend development role" /></label>
      </div>

      <div hidden={step !== 4} className="form-stack">
        <label className="field"><span>Target locations</span><input name="targetLocations" placeholder="India, Bengaluru, Remote" /></label>
        <label className="field"><span>Work preference</span>
          <select name="remotePreference" defaultValue="FLEXIBLE">
            <option value="FLEXIBLE">Flexible</option>
            <option value="REMOTE">Remote</option>
            <option value="HYBRID">Hybrid</option>
            <option value="ON_SITE">On-site</option>
          </select>
        </label>
        <label className="field"><span>Certifications</span><input name="certifications" placeholder="Separate multiple entries with commas" /></label>
      </div>

      <div hidden={step !== 5} className="form-stack">
        <label className="field"><span>Hours available each week</span><input name="timeAvailablePerWeek" type="number" min={1} max={168} defaultValue={10} /></label>
        <div className="notice"><strong>Keep it realistic.</strong><span>MentorAI will prioritize a smaller current focus instead of filling every hour.</span></div>
      </div>

      <div hidden={step !== 6} className="review-panel">
        <h2>Ready to build your starting point?</h2>
        <p>Your answers create an editable starting profile. You can then compare career directions using your interests, goals, skills, and weekly availability. Live market evidence is not available yet.</p>
        <ul className="check-list">
          <li>Profile details stay editable.</li>
          <li>Scores will be calculated by documented rules.</li>
          <li>AI recommendations will show evidence and uncertainty.</li>
        </ul>
      </div>

      {state.error && <div className="form-error" role="alert">{state.error}</div>}
      <div className="form-actions">
        {step > 0 ? (
          <Button type="button" variant="secondary" onClick={() => setStep((value) => value - 1)}>Back</Button>
        ) : (
          <Link href="/dashboard" className="button button-quiet">Skip for now</Link>
        )}
        {lastStep ? (
          <SubmitButton idle="Save profile" pending="Saving profile…" />
        ) : (
          <Button type="button" onClick={() => setStep((value) => value + 1)}>Continue</Button>
        )}
      </div>
    </form>
  );
}
