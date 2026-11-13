// Dream web server: serves the website and a small API.
// Runs on Render as one web service (see render.yaml in the repo root).
//
//   GET  /api               list of endpoints
//   GET  /api/health        is the server up, can it reach Supabase
//   GET  /api/stats         how much content is in the database (cards by status, courses, lessons, universities)
//   GET  /api/courses       courses with the 85/15 split of each price
//   GET  /api/courses/:id/quote
//   POST /api/enroll        demo purchase: the server reads the price, calculates the split and saves it
//
// The server uses only the public (publishable) Supabase key. For /api/enroll it forwards the
// signed-in user's own token, so Row Level Security still decides what is allowed.
import express from "express";
import { readFileSync, existsSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { split, PLATFORM_PERCENT } from "./money.mjs";

const here = dirname(fileURLToPath(import.meta.url));
const DIST = join(here, "../dist");
const PORT = Number(process.env.PORT) || 3000;
const SUPABASE_URL = (process.env.SUPABASE_URL || "https://fryulyyrppyimbshsnvf.supabase.co").replace(/\/$/, "");
const SUPABASE_KEY = process.env.SUPABASE_ANON_KEY || "sb_publishable_W5GebSnGN1vd2umX-qKGkw_qSNNFSIi";
const VERSION = JSON.parse(readFileSync(join(here, "../package.json"), "utf8")).version;
const STARTED = Date.now();

/** A call to Supabase. `token` is the user's access token; without it the call is anonymous. */
async function sb(path, { token, method = "GET", body, prefer, timeoutMs = 6000 } = {}) {
  const headers = { apikey: SUPABASE_KEY, Authorization: `Bearer ${token || SUPABASE_KEY}` };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (prefer) headers.Prefer = prefer;
  const res = await fetch(`${SUPABASE_URL}${path}`, {
    method, headers, body: body === undefined ? undefined : JSON.stringify(body), signal: AbortSignal.timeout(timeoutMs),
  });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  return { ok: res.ok, status: res.status, data, headers: res.headers };
}

const app = express();
app.disable("x-powered-by");
app.use(express.json({ limit: "16kb" }));
app.use((req, res, next) => {
  res.set({
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "strict-origin-when-cross-origin",
    "X-Frame-Options": "SAMEORIGIN",
    "Permissions-Policy": "camera=(), microphone=(), geolocation=()",
  });
  next();
});

// ------------------------------------------------------------------ API
const api = express.Router();
api.use((req, res, next) => { res.set("Cache-Control", "no-store"); next(); });

api.get("/", (req, res) => {
  res.json({
    name: "Dream API",
    version: VERSION,
    endpoints: [
      "GET /api/health", "GET /api/stats", "GET /api/courses", "GET /api/courses/:id/quote",
      "POST /api/enroll  (Authorization: Bearer <user access token>, body {course_id})",
    ],
    database: "Supabase (PostgreSQL + Auth + Row Level Security), shared with the Android app",
  });
});

api.get("/health", async (req, res) => {
  const t0 = Date.now();
  let supabase = { reachable: false };
  try {
    const r = await sb("/rest/v1/courses?select=id&limit=1", { timeoutMs: 4000 });
    supabase = { reachable: r.ok, status: r.status, ms: Date.now() - t0 };
  } catch (e) {
    supabase = { reachable: false, error: e.name === "TimeoutError" ? "timeout" : "network" };
  }
  res.status(supabase.reachable ? 200 : 503).json({
    ok: supabase.reachable, service: "dream-web", version: VERSION,
    time: new Date().toISOString(), uptime_s: Math.round((Date.now() - STARTED) / 1000), supabase,
  });
});

let statsCache = { at: 0, value: null };
api.get("/stats", async (req, res) => {
  if (statsCache.value && Date.now() - statsCache.at < 60_000) return res.json(statsCache.value);
  try {
    const [cards, courses, lessons, unis] = await Promise.all([
      sb("/rest/v1/cards?select=kind,status"),
      sb("/rest/v1/courses?select=id"),
      sb("/rest/v1/lessons?select=id"),
      sb("/rest/v1/universities?select=id"),
    ]);
    if (![cards, courses, lessons, unis].every((r) => r.ok)) return res.status(502).json({ error_key: "err_server" });
    const by = (k, v) => cards.data.filter((c) => c[k] === v).length;
    const value = {
      cards: {
        total: cards.data.length, roadmap: by("kind", "roadmap"), guide: by("kind", "guide"),
        verified: by("status", "verified"), check: by("status", "check"), tip: by("status", "tip"),
      },
      courses: courses.data.length, lessons: lessons.data.length, universities: unis.data.length,
      updated: new Date().toISOString(),
    };
    statsCache = { at: Date.now(), value };
    res.json(value);
  } catch {
    res.status(503).json({ error_key: "err_offline" });
  }
});

api.get("/courses", async (req, res) => {
  try {
    const r = await sb("/rest/v1/courses?select=*&order=sort");
    if (!r.ok) return res.status(502).json({ error_key: "err_server" });
    res.json({ platform_percent: PLATFORM_PERCENT, courses: r.data.map((c) => ({ ...c, ...split(Number(c.price_uzs)) })) });
  } catch {
    res.status(503).json({ error_key: "err_offline" });
  }
});

api.get("/courses/:id/quote", async (req, res) => {
  try {
    const r = await sb(`/rest/v1/courses?select=id,price_uzs&id=eq.${encodeURIComponent(req.params.id)}`);
    if (!r.ok) return res.status(502).json({ error_key: "err_server" });
    if (!r.data.length) return res.status(404).json({ error_key: "err_course_missing" });
    res.json({ course_id: req.params.id, platform_percent: PLATFORM_PERCENT, ...split(Number(r.data[0].price_uzs)) });
  } catch {
    res.status(503).json({ error_key: "err_offline" });
  }
});

api.post("/enroll", async (req, res) => {
  const token = (req.get("authorization") || "").replace(/^Bearer\s+/i, "");
  const courseId = typeof req.body?.course_id === "string" ? req.body.course_id : "";
  if (!token) return res.status(401).json({ error_key: "err_sign_in_first" });
  if (!courseId || courseId.length > 64) return res.status(400).json({ error_key: "err_course_missing" });
  try {
    // 1) who is this? Supabase Auth checks the token.
    const user = await sb("/auth/v1/user", { token });
    if (!user.ok || !user.data?.id) return res.status(401).json({ error_key: "err_session_expired" });
    // 2) the price comes from the database, never from the browser
    const course = await sb(`/rest/v1/courses?select=id,price_uzs&id=eq.${encodeURIComponent(courseId)}`);
    if (!course.ok) return res.status(502).json({ error_key: "err_server" });
    if (!course.data.length) return res.status(400).json({ error_key: "err_course_missing" });
    const s = split(Number(course.data[0].price_uzs));
    // 3) save as the user (RLS: a user can only insert their own row)
    const saved = await sb("/rest/v1/enrollments?on_conflict=user_id,course_id", {
      token, method: "POST", prefer: "resolution=ignore-duplicates,return=representation",
      body: { user_id: user.data.id, course_id: courseId, ...s },
    });
    if (!saved.ok) return res.status(saved.status === 401 ? 401 : 502).json({ error_key: saved.status === 401 ? "err_session_expired" : "err_server" });
    res.status(201).json({ ok: true, course_id: courseId, ...s, calculated_by: "dream-api" });
  } catch {
    res.status(503).json({ error_key: "err_offline" });
  }
});

api.use((req, res) => res.status(404).json({ error_key: "not_found" }));
app.use("/api", api);

// ------------------------------------------------------------------ website
if (existsSync(DIST)) {
  app.use("/assets", express.static(join(DIST, "assets"), { immutable: true, maxAge: "1y" }));
  app.use(express.static(DIST, { index: false, maxAge: "1h" }));
  // every other path is a page of the single-page app
  app.use((req, res) => {
    if (req.method !== "GET") return res.status(404).end();
    res.set("Cache-Control", "no-cache").sendFile(join(DIST, "index.html"));
  });
} else {
  app.use((req, res) => res.status(503).send("Website not built yet: run `npm run build`."));
}

app.listen(PORT, () => console.log(`Dream web on http://localhost:${PORT}  (Supabase: ${SUPABASE_URL})`));
