import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import { Brand, GlyphTile, LangSwitch, StatusBadge } from "../components/ui";
import { pick, useI18n } from "../lib/i18n";
import { useAuth } from "../lib/auth";
import { useContent } from "../lib/content";
import { Rooms, sectionsOf, sectionTitleKey } from "../lib/sections";
import { groupDigits, platformFee, teacherShare } from "../lib/money";

function SiteHeader() {
  const { t } = useI18n();
  const { session } = useAuth();
  return (
    <header className="site-header">
      <a href="#main" className="skip">{t("skip_to_content")}</a>
      <Brand light />
      <nav className="site-nav" aria-label="Dream">
        <a href="#how">{t("nav_how")}</a>
        <a href="#facts">{t("nav_facts")}</a>
        <a href="#courses">{t("nav_courses")}</a>
      </nav>
      <div className="site-actions">
        <LangSwitch compact />
        {session ? (
          <Link to="/app" className="btn btn-saffron btn-sm">{t("nav_open_app")}</Link>
        ) : (
          <>
            <Link to="/login" className="btn btn-ghost-light btn-sm hide-sm">{t("nav_login")}</Link>
            <Link to="/register" className="btn btn-saffron btn-sm">{t("nav_signup")}</Link>
          </>
        )}
      </div>
    </header>
  );
}

/** The signature moment: the line from Tashkent to Seoul draws itself and the five stages light up on the way. */
function Route() {
  const { t } = useI18n();
  const { cards } = useContent();
  const stages = sectionsOf("roadmap");
  return (
    <div className="route" aria-label={t("route_label")} role="img">
      <div className="route-end route-start">
        <span className="route-dot" />
        <span className="route-city">{t("route_from")}</span>
      </div>
      <div className="route-track">
        <div className="route-line" />
        <div className="route-plane" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="22" height="22"><path fill="currentColor" d="M21 15.5v-2l-8-5V3.5a1.5 1.5 0 0 0-3 0V8.5l-8 5v2l8-2.5V18l-2 1.5V21l3.5-1 3.5 1v-1.5L13 18v-5l8 2.5Z" transform="rotate(90 12 12)"/></svg>
        </div>
        <ol className="route-stations">
          {stages.map((s, i) => {
            const n = cards.filter((c) => c.section === s.key).length;
            return (
              <li key={s.key} className="route-station" style={{ ["--i" as string]: i }}>
                <GlyphTile glyph={s.glyph} index={i} size={64} />
                <span className="route-num">{i + 1}</span>
                <span className="route-name">{t(sectionTitleKey(s.key))}</span>
                <span className="route-count">{t("route_steps", n)}</span>
              </li>
            );
          })}
        </ol>
      </div>
      <div className="route-end route-finish">
        <span className="route-dot" />
        <span className="route-city">{t("route_to")}</span>
      </div>
    </div>
  );
}

function TryPath() {
  const { t, lang } = useI18n();
  const { cards } = useContent();
  const steps = cards.filter((c) => c.kind === "roadmap").slice(0, 4);
  const [ticked, setTicked] = useState<Set<string>>(new Set());
  const done = steps.filter((s) => ticked.has(s.id)).length;
  const mood = done === 0 ? "roadmap_mood_start" : done === steps.length ? "roadmap_mood_done" : done * 2 >= steps.length ? "roadmap_mood_half" : "roadmap_mood_started";
  return (
    <div className="try">
      <p className="try-label">{t("part_path_try")}</p>
      <ul className="try-list">
        {steps.map((s) => {
          const on = ticked.has(s.id);
          return (
            <li key={s.id}>
              <button type="button" className={`try-step${on ? " on" : ""}`} aria-pressed={on}
                onClick={() => setTicked((p) => { const n = new Set(p); if (on) n.delete(s.id); else n.add(s.id); return n; })}>
                <span className="check" aria-hidden="true">
                  <motion.svg viewBox="0 0 20 20" width="14" height="14" initial={false} animate={{ scale: on ? 1 : 0 }}>
                    <path d="M4 10.5l4 4 8-9" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" />
                  </motion.svg>
                </span>
                {pick(s, "title", lang)}
              </button>
            </li>
          );
        })}
      </ul>
      <div className="try-bar" aria-hidden="true"><motion.span initial={false} animate={{ width: `${(done / steps.length) * 100}%` }} /></div>
      <p className="try-mood" aria-live="polite">{t(mood)}</p>
    </div>
  );
}

function Split() {
  const { t, lang } = useI18n();
  const { courses } = useContent();
  const [id, setId] = useState<string | null>(null);
  const course = courses.find((c) => c.id === id) ?? courses[1] ?? courses[0];
  if (!course) return null;
  const price = Number(course.price_uzs);
  return (
    <div className="split">
      <p className="split-pick" id="split-pick">{t("split_pick")}</p>
      <div className="chips" role="radiogroup" aria-labelledby="split-pick">
        {courses.map((c, i) => (
          <button key={c.id} type="button" role="radio" aria-checked={c.id === course.id}
            className={`chip${c.id === course.id ? " on" : ""}`} onClick={() => setId(c.id)}>
            <GlyphTile glyph={c.glyph} index={i} size={26} /> {pick(c, "title", lang)}
          </button>
        ))}
      </div>
      <div className="split-figures">
        <div className="split-teacher">
          <span className="split-cap">{t("pay_teacher")}</span>
          <AnimatePresence mode="popLayout" initial={false}>
            <motion.strong key={course.id} initial={{ y: 18, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ y: -18, opacity: 0 }}>
              {t("money", groupDigits(teacherShare(price)))}
            </motion.strong>
          </AnimatePresence>
        </div>
        <div className="split-platform">
          <span className="split-cap">{t("pay_platform")}</span>
          <AnimatePresence mode="popLayout" initial={false}>
            <motion.strong key={course.id} initial={{ y: 18, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ y: -18, opacity: 0 }}>
              {t("money", groupDigits(platformFee(price)))}
            </motion.strong>
          </AnimatePresence>
        </div>
      </div>
      <div className="split-bar" aria-hidden="true"><span className="a" /><span className="b" /></div>
      <p className="split-total">{t("pay_total")}: {t("money", groupDigits(price))}</p>
    </div>
  );
}

export default function Landing() {
  const { t, lang } = useI18n();
  const content = useContent();
  const { cards, courses, universities, live } = content;
  const roadmapCount = cards.filter((c) => c.kind === "roadmap").length;
  const showcase = useMemo(
    () => cards.find((c) => c.id === "r_goal") ?? cards.find((c) => c.status === "verified") ?? cards[0],
    [cards],
  );
  const counts = {
    verified: cards.filter((c) => c.status === "verified").length,
    check: cards.filter((c) => c.status === "check").length,
    tip: cards.filter((c) => c.status === "tip").length,
  };
  const [consent, setConsent] = useState(false);

  return (
    <div className="landing">
      <div className="hero-wrap">
        <SiteHeader />
        <section className="hero" id="main">
          <div className="hero-copy">
            <h1 className="hero-title">{t("hero_title")}</h1>
            <p className="hero-sub">{t("hero_sub")}</p>
            <div className="hero-ctas">
              <Link to="/register" className="btn btn-saffron btn-lg">{t("hero_cta")}</Link>
              <a href="#how" className="btn btn-ghost-light btn-lg">{t("hero_cta2")}</a>
            </div>
            <p className="hero-free">{t("hero_free")}</p>
          </div>
          <span className="hero-kkum" aria-hidden="true">꿈</span>
        </section>
        <Route />
      </div>

      <main>
        <section className="band" id="how">
          <h2 className="h2">{t("parts_title")}</h2>
          <div className="parts">
            <article className="part part-path">
              <GlyphTile glyph="길" index={0} size={72} />
              <h3 className="h3">{t("part_path_title")}</h3>
              <p>{t("part_path_body", roadmapCount)}</p>
              <TryPath />
            </article>
            <article className="part part-guide">
              <GlyphTile glyph="첫" index={2} size={56} />
              <h3 className="h3">{t("part_guide_title")}</h3>
              <p>{t("part_guide_body")}</p>
              <ul className="guide-list">
                {sectionsOf("guide").map((s) => (
                  <li key={s.key}><span className="hangul">{s.glyph}</span>{t(sectionTitleKey(s.key))}</li>
                ))}
              </ul>
            </article>
            <article className="part part-courses">
              <h3 className="h3">{t("part_courses_title")}</h3>
              <p>{t("part_courses_body")}</p>
              <div className="course-strip">
                {courses.map((c, i) => (
                  <div key={c.id} className="course-mini">
                    <GlyphTile glyph={c.glyph} index={i} size={84} />
                    <span className="course-mini-title">{pick(c, "title", lang)}</span>
                    <span className="course-mini-meta">{pick(c, "level", lang)}</span>
                  </div>
                ))}
              </div>
            </article>
          </div>
        </section>

        <section className="band band-facts" id="facts">
          <div className="facts-copy">
            <h2 className="h2">{t("facts_title")}</h2>
            <p className="lead">{t("facts_body")}</p>
            <dl className="legend">
              <div><dt><span className="badge badge-ok">{t("status_verified", showcase?.verified_at || "")}</span></dt><dd><b>{counts.verified}</b> {t("facts_count", counts.verified)}. {t("facts_verified")}</dd></div>
              <div><dt><span className="badge badge-check">{t("status_check")}</span></dt><dd><b>{counts.check}</b> {t("facts_count", counts.check)}. {t("facts_check")}</dd></div>
              <div><dt><span className="badge badge-tip">{t("status_tip")}</span></dt><dd><b>{counts.tip}</b> {t("facts_count", counts.tip)}. {t("facts_tip")}</dd></div>
            </dl>
            <p className={`live-note${live ? " is-live" : ""}`}>{live ? t("facts_live") : t("facts_seed")}</p>
          </div>
          {showcase && (
            <article className="fact-card">
              <div className="fact-head">
                <span className="fact-section">{t(sectionTitleKey(showcase.section))}</span>
                <StatusBadge card={showcase} />
              </div>
              <h3 className="h3">{pick(showcase, "title", lang)}</h3>
              {pick(showcase, "body", lang).split("\n\n").map((p, i) => <p key={i}>{p}</p>)}
              <dl className="fact-meta">
                <div><dt>{t("source")}</dt><dd>{showcase.source_url
                  ? <a href={showcase.source_url} target="_blank" rel="noreferrer">{pick(showcase, "source", lang)}</a>
                  : pick(showcase, "source", lang)}</dd></div>
                {showcase.verified_at && <div><dt>{t("last_checked")}</dt><dd>{showcase.verified_at}</dd></div>}
              </dl>
            </article>
          )}
        </section>

        <section className="band band-split" id="courses">
          <div>
            <h2 className="h2">{t("split_title")}</h2>
            <p className="lead">{t("split_body")}</p>
            <p className="small muted">{t("demo_catalog")}</p>
          </div>
          <Split />
        </section>

        <section className="band band-rooms">
          <h2 className="h2">{t("rooms_title")}</h2>
          <p className="lead">{t("rooms_body")}</p>
          <div className="rooms-cloud">
            <div>
              <h3 className="h4">{t("rooms_topic")}</h3>
              <ul className="room-chips">
                {[...sectionsOf("roadmap"), ...sectionsOf("guide")].map((s) => (
                  <li key={Rooms.topic(s.key)}><span className="hangul">{s.glyph}</span>{t(sectionTitleKey(s.key))}</li>
                ))}
              </ul>
            </div>
            <div>
              <h3 className="h4">{t("rooms_course")}</h3>
              <ul className="room-chips">
                {courses.map((c) => (
                  <li key={Rooms.course(c.id)}><span className="hangul">{c.glyph}</span>{pick(c, "title", lang)}</li>
                ))}
              </ul>
            </div>
          </div>
        </section>

        <section className="band band-unis">
          <div>
            <h2 className="h2">{t("unis_land_title")}</h2>
            <p className="lead">{t("unis_land_body")}</p>
            <label className="consent-demo">
              <input type="checkbox" checked={consent} onChange={(e) => setConsent(e.target.checked)} />
              <span>{t("consent_checkbox")}</span>
            </label>
          </div>
          <ul className="uni-list">
            {universities.map((u) => (
              <li key={u.id} className={consent ? "lit" : ""}>
                <span className="uni-name">{u.name}</span>
                <span className="uni-city">{pick(u, "city", lang)}</span>
              </li>
            ))}
          </ul>
        </section>

        <section className="cta">
          <span className="cta-glyph" aria-hidden="true">꿈</span>
          <div>
            <h2 className="h2">{t("cta_title")}</h2>
            <p className="lead">{t("cta_body")}</p>
            <div className="hero-ctas">
              <Link to="/register" className="btn btn-saffron btn-lg">{t("hero_cta")}</Link>
              <Link to="/login" className="btn btn-ghost-light btn-lg">{t("nav_login")}</Link>
            </div>
            <p className="small cta-meaning">{t("dream_meaning")}</p>
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <Brand />
        <p>{t("footer_demo")}</p>
        <p className="footer-links">
          <a href="https://github.com/kubrobe/dream" target="_blank" rel="noreferrer">{t("footer_github")}</a>
          <a href="/api/health" target="_blank" rel="noreferrer">{t("footer_api")}</a>
          <LangSwitch />
        </p>
        <p className="small muted">{t("about_line")}</p>
      </footer>
    </div>
  );
}
