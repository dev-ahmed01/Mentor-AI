import type { Metadata } from "next";
import { ProfileEditor } from "@/components/ProfileEditor";
import { getProfile } from "@/lib/profile";

export const metadata: Metadata = { title: "Profile" };

export default async function ProfilePage() {
  const profile = await getProfile();
  return (
    <div className="narrow-page">
      <div className="page-heading">
        <div><p className="eyebrow">Observed information</p><h1>Your student profile</h1><p>Edit the information MentorAI may use for future analysis. Changes never select a career for you.</p></div>
      </div>
      <ProfileEditor profile={profile} />
    </div>
  );
}
