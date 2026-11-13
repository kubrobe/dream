import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { supabase } from "./supabase";
import { toUserError, UserError, useAuth } from "./auth";
import { platformFee, QUESTION_LIMIT } from "./money";

type Ctx = {
  done: Set<string>;
  owned: Set<string>;
  interests: Set<string>;
  loaded: boolean;
  setDone: (cardId: string, on: boolean) => Promise<void>;
  buy: (courseId: string, price: number) => Promise<{ fromServer: boolean }>;
  setInterest: (uniId: string, on: boolean) => Promise<void>;
};

const DataCtx = createContext<Ctx>(null as unknown as Ctx);

/** The signed-in user's rows: finished steps, purchased courses, universities of interest. */
export function UserDataProvider({ children }: { children: ReactNode }) {
  const { userId, session } = useAuth();
  const [done, setDoneIds] = useState<Set<string>>(new Set());
  const [owned, setOwned] = useState<Set<string>>(new Set());
  const [interests, setInterests] = useState<Set<string>>(new Set());
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    if (!userId) { setDoneIds(new Set()); setOwned(new Set()); setInterests(new Set()); setLoaded(false); return; }
    let alive = true;
    Promise.all([
      supabase.from("progress").select("card_id"),
      supabase.from("enrollments").select("course_id"),
      supabase.from("interests").select("university_id"),
    ]).then(([p, e, i]) => {
      if (!alive) return;
      setDoneIds(new Set((p.data ?? []).map((r) => r.card_id as string)));
      setOwned(new Set((e.data ?? []).map((r) => r.course_id as string)));
      setInterests(new Set((i.data ?? []).map((r) => r.university_id as string)));
      setLoaded(true);
    });
    return () => { alive = false; };
  }, [userId]);

  const flip = (set: Set<string>, id: string, on: boolean) => {
    const next = new Set(set);
    if (on) next.add(id); else next.delete(id);
    return next;
  };

  const setDone = useCallback(async (cardId: string, on: boolean) => {
    if (!userId) throw new UserError("err_sign_in_first");
    setDoneIds((s) => flip(s, cardId, on)); // optimistic
    const { error } = on
      ? await supabase.from("progress").upsert({ user_id: userId, card_id: cardId }, { onConflict: "user_id,card_id", ignoreDuplicates: true })
      : await supabase.from("progress").delete().eq("user_id", userId).eq("card_id", cardId);
    if (error) { setDoneIds((s) => flip(s, cardId, !on)); throw toUserError(error); }
  }, [userId]);

  const buy = useCallback(async (courseId: string, price: number) => {
    if (!userId || !session) throw new UserError("err_sign_in_first");
    // 1) The Dream API (server/index.mjs) reads the price from the database and calculates the 85/15 split.
    try {
      const res = await fetch("/api/enroll", {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${session.access_token}` },
        body: JSON.stringify({ course_id: courseId }),
      });
      if (res.ok) { setOwned((s) => flip(s, courseId, true)); return { fromServer: true }; }
      if (res.status !== 404 && res.status !== 502 && res.status !== 503) {
        const body = await res.json().catch(() => ({}));
        throw new UserError(body.error_key || "err_generic");
      }
    } catch (e) {
      if (e instanceof UserError) throw e;
      // network error: the site is served without the API (for example `vite preview`), fall through
    }
    // 2) No API available: save the purchase straight to Supabase (same split, same table).
    const fee = platformFee(price);
    const { error } = await supabase.from("enrollments").upsert(
      { user_id: userId, course_id: courseId, price_uzs: price, platform_fee: fee, teacher_share: price - fee },
      { onConflict: "user_id,course_id", ignoreDuplicates: true },
    );
    if (error) throw toUserError(error);
    setOwned((s) => flip(s, courseId, true));
    return { fromServer: false };
  }, [userId, session]);

  const setInterest = useCallback(async (uniId: string, on: boolean) => {
    if (!userId) throw new UserError("err_sign_in_first");
    const { error } = on
      ? await supabase.from("interests").upsert({ user_id: userId, university_id: uniId, consent: true }, { onConflict: "user_id,university_id", ignoreDuplicates: true })
      : await supabase.from("interests").delete().eq("user_id", userId).eq("university_id", uniId);
    if (error) throw toUserError(error);
    setInterests((s) => flip(s, uniId, on));
  }, [userId]);

  const value = useMemo(() => ({ done, owned, interests, loaded, setDone, buy, setInterest }),
    [done, owned, interests, loaded, setDone, buy, setInterest]);
  return <DataCtx.Provider value={value}>{children}</DataCtx.Provider>;
}

export const useUserData = () => useContext(DataCtx);

// ---------------------------------------------------------------- rooms
export type Message = { id: number; room_id: string; user_id: string; author: string; body: string; created_at: string };

/** Messages of one room. Refreshes every 4 seconds while the room is open, like the app. */
export function useRoom(roomId: string) {
  const { userId, profile } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [loaded, setLoaded] = useState(false);
  const roomRef = useRef(roomId);
  roomRef.current = roomId;

  const refresh = useCallback(async () => {
    const { data, error } = await supabase.from("messages").select("*").eq("room_id", roomId)
      .order("created_at", { ascending: false }).limit(200);
    if (error) throw toUserError(error);
    if (roomRef.current === roomId) { setMessages((data ?? []).reverse() as Message[]); setLoaded(true); }
  }, [roomId]);

  useEffect(() => {
    setMessages([]); setLoaded(false);
    refresh().catch(() => setLoaded(true));
    const timer = setInterval(() => { if (!document.hidden) refresh().catch(() => {}); }, 4000);
    return () => clearInterval(timer);
  }, [refresh]);

  const send = useCallback(async (text: string) => {
    const body = text.trim();
    if (!body) return;
    if (body.length > 1000) throw new UserError("err_too_long", [1000]);
    if (!userId) throw new UserError("err_sign_in_first");
    const { error } = await supabase.from("messages")
      .insert({ room_id: roomId, user_id: userId, author: profile.full_name || "Student", body });
    if (error) throw toUserError(error);
    await refresh();
  }, [roomId, userId, profile.full_name, refresh]);

  return { messages, loaded, send };
}

// ---------------------------------------------------------------- questions to the teacher
export type Question = { id: number; course_id: string; body: string; answer: string; created_at: string };

export function useQuestions(courseId: string, enabled: boolean) {
  const { userId } = useAuth();
  const [questions, setQuestions] = useState<Question[]>([]);

  const refresh = useCallback(async () => {
    if (!enabled || !userId) return;
    const { data } = await supabase.from("questions").select("*").eq("course_id", courseId).order("created_at");
    setQuestions((data ?? []) as Question[]);
  }, [courseId, enabled, userId]);

  useEffect(() => { refresh(); }, [refresh]);

  const ask = useCallback(async (text: string) => {
    const body = text.trim();
    if (!body) throw new UserError("err_question_empty");
    if (body.length > 1000) throw new UserError("err_too_long", [1000]);
    if (questions.length >= QUESTION_LIMIT) throw new UserError("err_question_limit", [QUESTION_LIMIT]);
    if (!userId) throw new UserError("err_sign_in_first");
    const { error } = await supabase.from("questions").insert({ course_id: courseId, user_id: userId, body });
    if (error) throw toUserError(error);
    await refresh();
  }, [courseId, userId, questions.length, refresh]);

  return { questions, ask, left: Math.max(0, QUESTION_LIMIT - questions.length) };
}
