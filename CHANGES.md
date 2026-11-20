# Creature on the home page

Copy these files over the repo (same paths). Also DELETE: client/src/components/home/XpCard.tsx
(replaced by CreatureCard.tsx, which keeps the same level/XP content).

New:      lib/creature.ts (mood logic), components/creature/*, components/home/CreatureCard.tsx, public/creature/**
Changed:  components/home/HomeDashboard.tsx (layout), components/challenge/DailyChallengeCard.tsx (button pinned to bottom),
          components/challenge/QuizScreen.tsx (creature on the result panel)
