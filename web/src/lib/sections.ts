// Same sections, glyphs and room ids as the Android app (data/Sections.kt).
export type SectionKey = "lang" | "univ" | "docs" | "visa" | "arrival" | "first90" | "health" | "work" | "life";
export type Section = { key: SectionKey; kind: "roadmap" | "guide"; glyph: string };

export const SECTIONS: Section[] = [
  { key: "lang", kind: "roadmap", glyph: "말" },
  { key: "univ", kind: "roadmap", glyph: "학" },
  { key: "docs", kind: "roadmap", glyph: "문" },
  { key: "visa", kind: "roadmap", glyph: "비" },
  { key: "arrival", kind: "roadmap", glyph: "길" },
  { key: "first90", kind: "guide", glyph: "첫" },
  { key: "health", kind: "guide", glyph: "몸" },
  { key: "work", kind: "guide", glyph: "일" },
  { key: "life", kind: "guide", glyph: "삶" },
];

export const sectionsOf = (kind: "roadmap" | "guide") => SECTIONS.filter((s) => s.kind === kind);
export const findSection = (key: string) => SECTIONS.find((s) => s.key === key);
export const sectionTitleKey = (key: string) => (findSection(key) ? `section_${key}` : "section_unknown");
export const sectionGlyph = (key: string) => findSection(key)?.glyph ?? "꿈";

export const Rooms = {
  topic: (key: string) => `topic_${key}`,
  course: (id: string) => `course_${id}`,
  isCourse: (room: string) => room.startsWith("course_"),
  courseId: (room: string) => room.replace(/^course_/, ""),
  sectionKey: (room: string) => room.replace(/^topic_/, ""),
};

export const GOALS = ["bachelor", "master", "language", "exchange"] as const;
export type Goal = (typeof GOALS)[number];

/** Also understands the Russian words saved by the first version of the app. */
export function goalOf(saved: string): Goal | null {
  const map: Record<string, Goal> = {
    bachelor: "bachelor", "Бакалавриат": "bachelor",
    master: "master", "Магистратура": "master",
    language: "language", "Языковые курсы": "language",
    exchange: "exchange", "Обмен": "exchange",
  };
  return map[saved.trim()] ?? null;
}

/** Tile colours, same pairs as the app's hangul tiles. */
export const TILE_GRADIENTS = [
  ["#0B7F7A", "#16B5AB"],
  ["#E59A12", "#F6C453"],
  ["#0E5E7A", "#2A9CC4"],
  ["#C4553F", "#EE8A6F"],
  ["#4C5BB0", "#7E8CE0"],
];
export const tileStyle = (i: number) => {
  const [a, b] = TILE_GRADIENTS[((i % TILE_GRADIENTS.length) + TILE_GRADIENTS.length) % TILE_GRADIENTS.length];
  return { background: `linear-gradient(140deg, ${a}, ${b})` };
};
