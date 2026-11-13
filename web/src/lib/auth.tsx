import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import type { Session } from "@supabase/supabase-js";
import { supabase } from "./supabase";

export type Profile = { full_name: string; goal: string };

/** An error the screen can show: `key` is a string key in the i18n tables. */
export class UserError extends Error {
  constructor(public key: string, public args: (string | number)[] = []) { super(key); }
}

/** Turns a Supabase / network error into a message key the user understands. */
export function toUserError(e: unknown): UserError {
  if (e instanceof UserError) return e;
  const err = e as { message?: string; status?: number; code?: string };
  const msg = (err?.message || "").toLowerCase();
  if (msg.includes("already registered") || msg.includes("already been registered")) return new UserError("err_email_taken");
  if (msg.includes("invalid login")) return new UserError("err_wrong_login");
  if (msg.includes("email not confirmed")) return new UserError("err_email_not_confirmed");
  if (msg.includes("password") && (msg.includes("weak") || msg.includes("at least"))) return new UserError("err_password_weak");
  if (err?.status === 429 || msg.includes("rate limit")) return new UserError("err_rate_limit");
  if (err?.code === "42P01" || err?.code === "PGRST205") return new UserError("err_no_table");
  if (err?.code === "PGRST301" || msg.includes("jwt")) return new UserError("err_session_expired");
  if (msg.includes("failed to fetch") || msg.includes("network") || msg.includes("timeout")) return new UserError("err_offline");
  return new UserError("err_generic");
}

type Ctx = {
  session: Session | null;
  ready: boolean;
  profile: Profile;
  email: string;
  userId: string | null;
  signUp: (name: string, email: string, password: string) => Promise<"in" | "confirm">;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  saveProfile: (p: Partial<Profile>) => Promise<void>;
};

const AuthCtx = createContext<Ctx>(null as unknown as Ctx);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [ready, setReady] = useState(false);
  const [profile, setProfile] = useState<Profile>({ full_name: "", goal: "" });

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => { setSession(data.session); setReady(true); });
    const { data } = supabase.auth.onAuthStateChange((_event, s) => setSession(s));
    return () => data.subscription.unsubscribe();
  }, []);

  const userId = session?.user.id ?? null;
  const metaName = (session?.user.user_metadata?.full_name as string | undefined) ?? "";

  useEffect(() => {
    if (!userId) { setProfile({ full_name: "", goal: "" }); return; }
    setProfile((p) => ({ ...p, full_name: p.full_name || metaName }));
    supabase.from("profiles").select("full_name, goal").eq("id", userId).maybeSingle().then(({ data }) => {
      if (data) setProfile({ full_name: data.full_name || metaName, goal: data.goal || "" });
    });
  }, [userId, metaName]);

  const signUp = useCallback(async (name: string, email: string, password: string) => {
    const { data, error } = await supabase.auth.signUp({ email, password, options: { data: { full_name: name } } });
    if (error) throw toUserError(error);
    // Supabase answers a repeated sign-up with a user that has no identities.
    if (data.user && data.user.identities && data.user.identities.length === 0) throw new UserError("err_email_taken");
    return data.session ? "in" : "confirm";
  }, []);

  const signIn = useCallback(async (email: string, password: string) => {
    const { error } = await supabase.auth.signInWithPassword({ email, password });
    if (error) throw toUserError(error);
  }, []);

  const signOut = useCallback(async () => { await supabase.auth.signOut(); }, []);

  const saveProfile = useCallback(async (p: Partial<Profile>) => {
    if (!userId) throw new UserError("err_sign_in_first");
    const next = { ...profile, ...p };
    const { error } = await supabase.from("profiles").upsert({ id: userId, full_name: next.full_name, goal: next.goal });
    if (error) throw toUserError(error);
    setProfile(next);
  }, [userId, profile]);

  const value = useMemo<Ctx>(() => ({
    session, ready, profile, userId, email: session?.user.email ?? "",
    signUp, signIn, signOut, saveProfile,
  }), [session, ready, profile, userId, signUp, signIn, signOut, saveProfile]);

  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>;
}

export const useAuth = () => useContext(AuthCtx);
