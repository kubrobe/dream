import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { motion } from "motion/react";
import { GlyphTile, Modal, ProgressRing, StatusBadge } from "../../components/ui";
import { pick, useI18n } from "../../lib/i18n";
import { useAuth } from "../../lib/auth";
import { useUserData } from "../../lib/data";
import { useToast } from "../../lib/toast";
import { useContent, type Card } from "../../lib/content";
import { Rooms, SECTIONS, sectionGlyph, sectionsOf, sectionTitleKey } from "../../lib/sections";
import type { UserError } from "../../lib/auth";

function useOpenCard() {
  const [q, setQ] = useSearchParams();
  return (id: string) => { const n = new URLSearchParams(q); n.set("card", id); setQ(n); };
}

function useToggleDone() {
  const { setDone } = useUserData();
  const { t } = useI18n();
  const toast = useToast();
  return (id: string, on: boolean) => setDone(id, on).catch((e: UserError) => toast(t(e.key, ...e.args), "error"));
}

function CheckButton({ card, done }: { card: Card; done: boolean }) {
  const { t, lang } = useI18n();
  const toggle = useToggleDone();
  return (
    <button type="button" className={`check-btn${done ? " on" : ""}`} aria-pressed={done}
      aria-label={`${done ? t("mark_undone") : t("mark_done")}: ${pick(card, "title", lang)}`}
      onClick={() => toggle(card.id, !done)}>
      <motion.svg viewBox="0 0 20 20" width="16" height="16" initial={false} animate={{ scale: done ? 1 : 0, rotate: done ? 0 : -30 }}
        transition={{ type: "spring", stiffness: 500, damping: 22 }}>
        <path d="M4 10.5l4 4 8-9" fill="none" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" />
      </motion.svg>
    </button>
  );
}

export function PathPage() {
  const { t, lang } = useI18n();
  const { profile } = useAuth();
  const { done } = useUserData();
  const { cards } = useContent();
  const open = useOpenCard();
  const toggle = useToggleDone();
  const steps = cards.filter((c) => c.kind === "roadmap");
  const doneCount = steps.filter((c) => done.has(c.id)).length;
  const next = steps.find((c) => !done.has(c.id));
  const mood = doneCount === 0 ? "roadmap_mood_start" : doneCount === steps.length ? "roadmap_mood_done"
    : doneCount * 2 >= steps.length ? "roadmap_mood_half" : "roadmap_mood_started";

  return (
    <div className="page">
      <header className="page-head path-head">
        <div>
          <p className="muted">{t("hello", profile.full_name || t("student"))}</p>
          <h1 className="h1">{t("roadmap_title")}</h1>
          <p className="lead">{t("roadmap_subtitle")}</p>
        </div>
        <div className="path-progress">
          <ProgressRing value={doneCount} total={steps.length} />
          <div><b>{t("done_of", doneCount, steps.length)}</b><p className="muted">{t(mood)}</p></div>
        </div>
      </header>

      {next ? (
        <section className="next-step" aria-labelledby="next-h">
          <GlyphTile glyph={sectionGlyph(next.section)} index={SECTIONS.findIndex((s) => s.key === next.section)} size={64} />
          <div className="next-body">
            <p className="next-cap" id="next-h">{t("next_step")} · {t(sectionTitleKey(next.section))}</p>
            <h2 className="h3">{pick(next, "title", lang)}</h2>
            <div className="row">
              <button className="btn btn-primary" onClick={() => toggle(next.id, true)}>{t("mark_done")}</button>
              <button className="btn btn-quiet" onClick={() => open(next.id)}>{t("read")}</button>
              <StatusBadge card={next} />
            </div>
          </div>
        </section>
      ) : (
        <section className="next-step done-all">
          <span className="done-glyph" aria-hidden="true">꿈</span>
          <div><h2 className="h3">{t("all_done_title")}</h2><p>{t("all_done_text")}</p>
            <Link className="btn btn-primary" to="/app/guide">{t("tab_guide")}</Link></div>
        </section>
      )}

      <ol className="stages">
        {sectionsOf("roadmap").map((s, i) => {
          const items = steps.filter((c) => c.section === s.key);
          const d = items.filter((c) => done.has(c.id)).length;
          return (
            <li key={s.key} className={`stage-block${d === items.length && items.length ? " complete" : ""}`}>
              <div className="stage-head">
                <GlyphTile glyph={s.glyph} index={i} size={52} />
                <div>
                  <p className="muted small">{t("stage_n", i + 1)} · {t("steps_in_stage", d, items.length)}</p>
                  <h2 className="h3">{t(sectionTitleKey(s.key))}</h2>
                </div>
                <Link className="btn btn-quiet btn-sm" to={`/app/rooms/${Rooms.topic(s.key)}`}>{t("open_room")}</Link>
              </div>
              <ul className="steps">
                {items.map((c) => (
                  <li key={c.id} className={`step${done.has(c.id) ? " is-done" : ""}`}>
                    <CheckButton card={c} done={done.has(c.id)} />
                    <button type="button" className="step-title" onClick={() => open(c.id)}>{pick(c, "title", lang)}</button>
                    <StatusBadge card={c} />
                  </li>
                ))}
              </ul>
            </li>
          );
        })}
      </ol>
    </div>
  );
}

export function GuidePage() {
  const { t, lang } = useI18n();
  const { cards } = useContent();
  const open = useOpenCard();
  const [filter, setFilter] = useState<string>("all");
  const guide = cards.filter((c) => c.kind === "guide" && (filter === "all" || c.section === filter));
  return (
    <div className="page">
      <header className="page-head">
        <h1 className="h1">{t("guide_title")}</h1>
        <p className="lead">{t("guide_subtitle")}</p>
      </header>
      <div className="chips" role="group">
        <button className={`chip${filter === "all" ? " on" : ""}`} aria-pressed={filter === "all"} onClick={() => setFilter("all")}>{t("filter_all")}</button>
        {sectionsOf("guide").map((s) => (
          <button key={s.key} className={`chip${filter === s.key ? " on" : ""}`} aria-pressed={filter === s.key} onClick={() => setFilter(s.key)}>
            <span className="hangul">{s.glyph}</span>{t(sectionTitleKey(s.key))}
          </button>
        ))}
      </div>
      <ul className="guide-grid">
        {guide.map((c) => (
          <motion.li key={c.id} layout initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
            <button type="button" className="guide-item" onClick={() => open(c.id)}>
              <span className="guide-item-top">
                <GlyphTile glyph={sectionGlyph(c.section)} index={SECTIONS.findIndex((s) => s.key === c.section)} size={40} />
                <StatusBadge card={c} />
              </span>
              <span className="guide-item-title">{pick(c, "title", lang)}</span>
              <span className="guide-item-text">{pick(c, "body", lang).split("\n")[0]}</span>
            </button>
          </motion.li>
        ))}
      </ul>
    </div>
  );
}

/** Full card: text, source, last checked, done toggle, link to the room. Opens over any page with ?card=<id>. */
export function CardModal() {
  const { t, lang } = useI18n();
  const [q, setQ] = useSearchParams();
  const { cards } = useContent();
  const { done } = useUserData();
  const toggle = useToggleDone();
  const id = q.get("card");
  const card = id ? cards.find((c) => c.id === id) : undefined;
  const close = () => { const n = new URLSearchParams(q); n.delete("card"); setQ(n); };
  const isDone = card ? done.has(card.id) : false;
  return (
    <Modal open={!!id} onClose={close} label={card ? pick(card, "title", lang) : t("card_not_found")}>
      {!card ? <p>{t("card_not_found")}</p> : (
        <article className="card-detail">
          <div className="fact-head">
            <span className="fact-section"><span className="hangul">{sectionGlyph(card.section)}</span>{t(sectionTitleKey(card.section))}</span>
            <StatusBadge card={card} />
          </div>
          <h2 className="h2">{pick(card, "title", lang)}</h2>
          {pick(card, "body", lang).split("\n\n").map((p, i) => <p key={i} className="card-p">{p}</p>)}
          <div className={`source-box source-${card.status}`}>
            <dl className="fact-meta">
              <div><dt>{t("source")}</dt><dd>{card.source_url
                ? <a href={card.source_url} target="_blank" rel="noreferrer">{pick(card, "source", lang)}</a>
                : pick(card, "source", lang)}</dd></div>
              {card.status === "verified" && <div><dt>{t("last_checked")}</dt><dd>{card.verified_at}</dd></div>}
            </dl>
            {card.status === "check" && <p className="small">{t("not_checked_yet")}</p>}
            {card.status === "tip" && <p className="small">{t("tip_not_fact")}</p>}
          </div>
          <div className="row">
            {card.kind === "roadmap" && (
              <button className={`btn ${isDone ? "btn-quiet" : "btn-primary"}`} onClick={() => toggle(card.id, !isDone)}>
                {isDone ? t("step_done_undo") : t("mark_done")}
              </button>
            )}
            <Link className="btn btn-quiet" to={`/app/rooms/${Rooms.topic(card.section)}`}>
              {t("discuss_in_room", t(sectionTitleKey(card.section)))}
            </Link>
          </div>
        </article>
      )}
    </Modal>
  );
}
