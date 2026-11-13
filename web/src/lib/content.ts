import { useEffect, useState } from "react";
import { supabase } from "./supabase";
import SEED from "../data/seed.json";

export type Card = {
  id: string; kind: "roadmap" | "guide"; section: string; sort: number;
  title: string; body: string; source: string; source_url: string;
  status: "verified" | "check" | "tip"; verified_at: string;
  title_en?: string; title_uz?: string; body_en?: string; body_uz?: string; source_en?: string; source_uz?: string;
};
export type Course = {
  id: string; sort: number; title: string; teacher: string; level: string; price_uzs: number;
  glyph: string; description: string;
  title_en?: string; title_uz?: string; level_en?: string; level_uz?: string; description_en?: string; description_uz?: string;
};
export type Lesson = {
  id: string; course_id: string; sort: number; title: string; minutes: number; free: boolean; content: string;
  title_en?: string; title_uz?: string; content_en?: string; content_uz?: string;
};
export type University = {
  id: string; sort: number; name: string; city: string; type: string; url: string;
  city_en?: string; city_uz?: string; type_en?: string; type_uz?: string;
};
export type Content = { cards: Card[]; courses: Course[]; lessons: Lesson[]; universities: University[]; live: boolean };

const seed: Content = { ...(SEED as unknown as Omit<Content, "live">), live: false };

async function fetchTable<T>(table: string): Promise<T[]> {
  const { data, error } = await supabase.from(table).select("*").order("sort");
  if (error) throw error;
  return (data ?? []) as T[];
}

function withTimeout<T>(p: Promise<T>, ms: number): Promise<T> {
  return Promise.race([p, new Promise<T>((_, reject) => setTimeout(() => reject(new Error("timeout")), ms))]);
}

let cache: Promise<Content> | null = null;

/** Content from Supabase; if the database cannot be reached, the built-in copy (same rows) is used. */
export function loadContent(): Promise<Content> {
  if (!cache) {
    cache = withTimeout(
      Promise.all([
        fetchTable<Card>("cards"),
        fetchTable<Course>("courses"),
        fetchTable<Lesson>("lessons"),
        fetchTable<University>("universities"),
      ]),
      9000,
    )
      .then(([cards, courses, lessons, universities]) =>
        cards.length ? { cards, courses, lessons, universities, live: true } : seed,
      )
      .catch(() => seed);
  }
  return cache;
}

export function useContent(): Content {
  const [content, setContent] = useState<Content>(seed);
  useEffect(() => {
    let alive = true;
    loadContent().then((c) => alive && setContent(c));
    return () => { alive = false; };
  }, []);
  return content;
}
