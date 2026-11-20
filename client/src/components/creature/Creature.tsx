import Image from "next/image";
import { CreatureForm, creatureSrc, Mood } from "@/lib/creature";
import styles from "./Creature.module.css";

const MOOD_LABEL: Record<Mood, string> = {
  neutral: "calm",
  happy: "happy",
  sad: "sad",
  excited: "excited",
  sleeping: "asleep",
};

// The Nibbling creature. Size it from outside with className (e.g. "w-40"):
// the sprites share one canvas size and baseline, so swapping moods never
// makes it jump. Re-keying on mood replays the little pop animation.
export function Creature({
  mood,
  form = "caterpillar",
  animate = true,
  className = "w-40",
}: {
  mood: Mood;
  form?: CreatureForm;
  animate?: boolean;
  className?: string;
}) {
  return (
    <div className={`relative aspect-[360/444] ${className}`}>
      {animate && <span className={styles.shadow} aria-hidden="true" />}
      <div key={mood} className={`relative h-full w-full ${animate ? styles.pop : ""}`}>
        <div className={`h-full w-full ${animate ? styles.bob : ""}`}>
          <Image
            src={creatureSrc(form, mood)}
            alt={`Your Nibbling looks ${MOOD_LABEL[mood]}`}
            width={360}
            height={444}
            sizes="(min-width: 640px) 200px, 160px"
            className="h-full w-full select-none object-contain"
            draggable={false}
          />
        </div>
      </div>
    </div>
  );
}
