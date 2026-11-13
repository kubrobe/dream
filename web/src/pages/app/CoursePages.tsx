import { useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import { GlyphTile, Modal, Spinner } from "../../components/ui";
import { pick, useI18n } from "../../lib/i18n";
import { useQuestions, useUserData } from "../../lib/data";
import { useToast } from "../../lib/toast";
import { useContent, type Course } from "../../lib/content";
import { Rooms } from "../../lib/sections";
import { groupDigits, platformFee, QUESTION_LIMIT, teacherShare } from "../../lib/money";
import type { UserError } from "../../lib/auth";

function SplitBanner() {
  const { t } = useI18n();
  return (
    <div className="split-banner">
      <p><b>{t("split_title")}</b></p>
      <div className="split-bar" aria-hidden="true"><span className="a" /><span className="b" /></div>
      <p className="split-legend"><span><i className="dot a" />{t("split_teacher")}</span><span><i className="dot b" />{t("split_platform")}</span></p>
    </div>
  );
}

export function CoursesPage() {
  const { t, lang } = useI18n();
  const { courses, lessons } = useContent();
  const { owned } = useUserData();
  return (
    <div className="page">
      <header className="page-head">
        <h1 className="h1">{t("courses_title")}</h1>
        <p className="lead">{t("courses_subtitle")}</p>
      </header>
      <SplitBanner />
      <p className="demo-note">{t("demo_catalog")}</p>
      <ul className="course-grid">
        {courses.map((c, i) => (
          <li key={c.id}>
            <Link to={`/app/courses/${c.id}`} className="course-card">
              <GlyphTile glyph={c.glyph} index={i} size={96} />
              <span className="course-card-body">
                <span className="course-card-title">{pick(c, "title", lang)}</span>
                <span className="muted small">{c.teacher} · {t("level", pick(c, "level", lang))} · {t("perk_lessons", lessons.filter((l) => l.course_id === c.id).length)}</span>
                <span className="course-card-desc">{pick(c, "description", lang)}</span>
              </span>
              <span className={`price-tag${owned.has(c.id) ? " owned" : ""}`}>
                {owned.has(c.id) ? t("owned") : t("money", groupDigits(c.price_uzs))}
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}

function BuyModal({ course, open, onClose }: { course: Course; open: boolean; onClose: () => void }) {
  const { t, lang } = useI18n();
  const { buy } = useUserData();
  const toast = useToast();
  const [busy, setBusy] = useState(false);
  const price = Number(course.price_uzs);
  async function confirm() {
    setBusy(true);
    try {
      const r = await buy(course.id, price);
      toast(t("notice_course_opened") + (r.fromServer ? ` ${t("split_from_server")}.` : ""));
      onClose();
    } catch (e) { toast(t((e as UserError).key, ...((e as UserError).args ?? [])), "error"); }
    finally { setBusy(false); }
  }
  return (
    <Modal open={open} onClose={onClose} label={t("pay_title")}>
      <h2 className="h2">{t("pay_title")}</h2>
      <p className="muted">{pick(course, "title", lang)}</p>
      <table className="pay-table">
        <tbody>
          <tr><th scope="row">{t("pay_teacher")}</th><td>{t("money", groupDigits(teacherShare(price)))}</td></tr>
          <tr><th scope="row">{t("pay_platform")}</th><td>{t("money", groupDigits(platformFee(price)))}</td></tr>
          <tr className="total"><th scope="row">{t("pay_total")}</th><td>{t("money", groupDigits(price))}</td></tr>
        </tbody>
      </table>
      <div className="split-bar" aria-hidden="true"><span className="a" /><span className="b" /></div>
      <p className="small muted">{t("pay_demo_note")}</p>
      <button className="btn btn-saffron btn-lg btn-block" onClick={confirm} disabled={busy}>{busy ? <Spinner /> : t("pay_confirm")}</button>
    </Modal>
  );
}

function Questions({ courseId }: { courseId: string }) {
  const { t } = useI18n();
  const toast = useToast();
  const { questions, ask, left } = useQuestions(courseId, true);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    try { await ask(text); setText(""); }
    catch (err) { const u = err as UserError; toast(t(u.key, ...(u.args ?? [])), "error"); }
    finally { setBusy(false); }
  }
  return (
    <section className="panel" aria-labelledby="q-h">
      <div className="panel-head">
        <h2 className="h3" id="q-h">{t("questions_title")}</h2>
        <span className="pill">{t("questions_left", left, QUESTION_LIMIT)}</span>
      </div>
      {questions.length === 0 && <p className="muted">{t("questions_empty")}</p>}
      <ol className="questions">
        {questions.map((q) => (
          <li key={q.id}>
            <p className="q-body">{q.body}</p>
            {q.answer ? <p className="q-answer"><b>{t("answer")}:</b> {q.answer}</p> : <p className="muted small">{t("no_answer_yet")}</p>}
          </li>
        ))}
      </ol>
      {left > 0 ? (
        <form onSubmit={submit} className="ask">
          <label className="field"><span>{t("your_question")}</span>
            <textarea rows={3} maxLength={1000} value={text} onChange={(e) => setText(e.target.value)} />
          </label>
          <button className="btn btn-primary" disabled={busy || !text.trim()}>{busy ? <Spinner /> : t("send_question")}</button>
        </form>
      ) : <p className="muted">{t("questions_used_up")}</p>}
    </section>
  );
}

export function CoursePage() {
  const { id = "" } = useParams();
  const { t, lang } = useI18n();
  const { courses, lessons } = useContent();
  const { owned } = useUserData();
  const [buying, setBuying] = useState(false);
  const index = courses.findIndex((c) => c.id === id);
  const course = courses[index];
  if (!course) return <div className="page"><p>{t("err_course_missing")}</p><Link to="/app/courses">{t("back")}</Link></div>;
  const list = lessons.filter((l) => l.course_id === id);
  const isOwned = owned.has(id);
  return (
    <div className="page">
      <Link to="/app/courses" className="back">{t("back")}</Link>
      <header className="course-hero">
        <GlyphTile glyph={course.glyph} index={index} size={140} />
        <div>
          <h1 className="h1">{pick(course, "title", lang)}</h1>
          <p className="muted">{course.teacher} · {t("level", pick(course, "level", lang))}</p>
          <p className="lead">{pick(course, "description", lang)}</p>
        </div>
      </header>

      <div className="course-layout">
        <div className="course-main">
          <section className="panel">
            <h2 className="h3">{t("lessons")}</h2>
            <ol className="lessons">
              {list.map((l, i) => {
                const open = l.free || isOwned;
                const inner = (
                  <>
                    <span className="lesson-n">{i + 1}</span>
                    <span className="lesson-title">{pick(l, "title", lang)}</span>
                    <span className="muted small">{t("minutes", l.minutes)}</span>
                    <span className={`pill ${l.free ? "pill-free" : open ? "pill-open" : "pill-locked"}`}>
                      {l.free ? t("free") : open ? t("lesson_open") : t("lesson_locked")}
                    </span>
                  </>
                );
                return (
                  <li key={l.id}>
                    {open
                      ? <Link className="lesson-row" to={`/app/courses/${id}/lessons/${l.id}`}>{inner}</Link>
                      : <button type="button" className="lesson-row locked" onClick={() => setBuying(true)}>{inner}</button>}
                  </li>
                );
              })}
            </ol>
          </section>
          {isOwned && <Questions courseId={id} />}
        </div>

        <aside className="course-side">
          <AnimatePresence mode="wait" initial={false}>
            {isOwned ? (
              <motion.div key="owned" className="buy-box owned" initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}>
                <h2 className="h3">{t("owned_title")}</h2>
                <p>{t("owned_text")}</p>
                <Link className="btn btn-primary btn-block" to={`/app/rooms/${Rooms.course(id)}`}>{t("course_room")}</Link>
              </motion.div>
            ) : (
              <motion.div key="buy" className="buy-box" exit={{ scale: 0.95, opacity: 0 }}>
                <p className="muted small">{t("price")}</p>
                <p className="buy-price">{t("money", groupDigits(course.price_uzs))}</p>
                <button className="btn btn-saffron btn-lg btn-block" onClick={() => setBuying(true)}>{t("buy")}</button>
                <p className="small muted">{t("pay_demo_note")}</p>
              </motion.div>
            )}
          </AnimatePresence>
          <div className="panel perks">
            <h2 className="h4">{t("course_includes")}</h2>
            <ul>
              <li>{t("perk_lessons", list.length)}</li>
              <li>{t("perk_room")}</li>
              <li>{t("perk_questions", QUESTION_LIMIT)}</li>
            </ul>
          </div>
        </aside>
      </div>
      <BuyModal course={course} open={buying} onClose={() => setBuying(false)} />
    </div>
  );
}

function Flash({ korean, meaning, hidden }: { korean: string; meaning: string; hidden: boolean }) {
  const [shown, setShown] = useState(false);
  const visible = !hidden || shown;
  return (
    <button type="button" className={`flash${visible ? " shown" : ""}`} onClick={() => setShown(true)} aria-expanded={visible}>
      <span className="flash-ko" lang="ko">{korean}</span>
      <span className="flash-meaning">
        <AnimatePresence mode="wait" initial={false}>
          {visible
            ? <motion.span key="m" initial={{ rotateX: 90, opacity: 0 }} animate={{ rotateX: 0, opacity: 1 }}>{meaning}</motion.span>
            : <motion.span key="q" className="flash-q" exit={{ rotateX: -90, opacity: 0 }}>?</motion.span>}
        </AnimatePresence>
      </span>
    </button>
  );
}

export function LessonPage() {
  const { id = "", lessonId = "" } = useParams();
  const { t, lang } = useI18n();
  const { lessons } = useContent();
  const { owned } = useUserData();
  const [practice, setPractice] = useState(false);
  const list = lessons.filter((l) => l.course_id === id);
  const i = list.findIndex((l) => l.id === lessonId);
  const lesson = list[i];
  if (!lesson) return <div className="page"><Link to={`/app/courses/${id}`} className="back">{t("back")}</Link></div>;
  const open = lesson.free || owned.has(id);
  const lines = pick(lesson, "content", lang).split("\n");
  const prev = list[i - 1];
  const next = list[i + 1];
  return (
    <div className="page lesson-page">
      <Link to={`/app/courses/${id}`} className="back">{t("back")}</Link>
      <p className="muted">{t("lesson_n", i + 1)} · {t("minutes", lesson.minutes)}</p>
      <h1 className="h1">{pick(lesson, "title", lang)}</h1>
      {!open ? (
        <div className="panel locked-panel">
          <p>{t("lesson_locked_text")}</p>
          <Link className="btn btn-saffron" to={`/app/courses/${id}`}>{t("go_to_course")}</Link>
        </div>
      ) : (
        <>
          <p className="demo-note">{t("lesson_note")}</p>
          <label className="switch-row">
            <span><b>{t("practice_mode")}</b><small className="muted">{t("practice_hint")}</small></span>
            <input type="checkbox" role="switch" checked={practice} onChange={(e) => setPractice(e.target.checked)} />
          </label>
          <div className="lesson-content" key={String(practice)}>
            {lines.map((line, k) => {
              const parts = line.split(" — ");
              if (parts.length >= 2 && parts[0].length <= 24) {
                return <Flash key={k} korean={parts[0]} meaning={parts.slice(1).join(" — ")} hidden={practice} />;
              }
              return line.trim() ? <p key={k}>{line}</p> : <span key={k} className="gap" />;
            })}
          </div>
        </>
      )}
      <nav className="lesson-nav">
        {prev ? <Link className="btn btn-quiet" to={`/app/courses/${id}/lessons/${prev.id}`}>{pick(prev, "title", lang)}</Link> : <span />}
        {next && <Link className="btn btn-quiet" to={`/app/courses/${id}/lessons/${next.id}`}>{pick(next, "title", lang)}</Link>}
      </nav>
    </div>
  );
}
