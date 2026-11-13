import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import { GlyphTile, Spinner } from "../../components/ui";
import { pick, useI18n } from "../../lib/i18n";
import { useAuth, type UserError } from "../../lib/auth";
import { useRoom, useUserData } from "../../lib/data";
import { useToast } from "../../lib/toast";
import { useContent } from "../../lib/content";
import { Rooms, SECTIONS, sectionGlyph, sectionsOf, sectionTitleKey } from "../../lib/sections";

export function RoomsPage() {
  const { t, lang } = useI18n();
  const { courses } = useContent();
  const { owned } = useUserData();
  return (
    <div className="page">
      <header className="page-head">
        <h1 className="h1">{t("tab_rooms")}</h1>
        <p className="lead">{t("rooms_body")}</p>
      </header>
      <h2 className="h4">{t("rooms_topic")}</h2>
      <ul className="room-grid">
        {[...sectionsOf("roadmap"), ...sectionsOf("guide")].map((s) => (
          <li key={s.key}>
            <Link className="room-tile" to={`/app/rooms/${Rooms.topic(s.key)}`}>
              <GlyphTile glyph={s.glyph} index={SECTIONS.indexOf(s)} size={44} />
              <span>{t(sectionTitleKey(s.key))}</span>
            </Link>
          </li>
        ))}
      </ul>
      <h2 className="h4">{t("rooms_course")}</h2>
      <ul className="room-grid">
        {courses.map((c, i) => (
          <li key={c.id}>
            {owned.has(c.id) ? (
              <Link className="room-tile" to={`/app/rooms/${Rooms.course(c.id)}`}>
                <GlyphTile glyph={c.glyph} index={i} size={44} /><span>{pick(c, "title", lang)}</span>
              </Link>
            ) : (
              <Link className="room-tile locked" to={`/app/courses/${c.id}`}>
                <GlyphTile glyph={c.glyph} index={i} size={44} style={{ filter: "grayscale(.8)", opacity: 0.6 }} />
                <span>{pick(c, "title", lang)}<small className="muted">{t("course_room_locked")}</small></span>
              </Link>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

/** 13.11 14:05 — the same in every language */
const time = (iso: string) => {
  const d = new Date(iso);
  const two = (n: number) => String(n).padStart(2, "0");
  return `${two(d.getDate())}.${two(d.getMonth() + 1)} ${two(d.getHours())}:${two(d.getMinutes())}`;
};

export function RoomPage() {
  const { roomId = "" } = useParams();
  const { t, lang } = useI18n();
  const { userId } = useAuth();
  const { courses } = useContent();
  const { owned } = useUserData();
  const toast = useToast();
  const { messages, loaded, send } = useRoom(roomId);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const end = useRef<HTMLDivElement>(null);

  const isCourse = Rooms.isCourse(roomId);
  const course = isCourse ? courses.find((c) => c.id === Rooms.courseId(roomId)) : undefined;
  const title = isCourse ? (course ? pick(course, "title", lang) : t("course_room")) : t(sectionTitleKey(Rooms.sectionKey(roomId)));
  const glyph = isCourse ? course?.glyph ?? "꿈" : sectionGlyph(Rooms.sectionKey(roomId));
  const locked = isCourse && !owned.has(Rooms.courseId(roomId));

  useEffect(() => { end.current?.scrollIntoView({ behavior: "smooth", block: "end" }); }, [messages.length]);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!text.trim()) return;
    setBusy(true);
    try { await send(text); setText(""); }
    catch (err) { const u = err as UserError; toast(t(u.key, ...(u.args ?? [])), "error"); }
    finally { setBusy(false); }
  }

  return (
    <div className="page room-page">
      <Link to="/app/rooms" className="back">{t("all_rooms")}</Link>
      <header className="room-head">
        <GlyphTile glyph={glyph} index={isCourse ? courses.indexOf(course!) : SECTIONS.findIndex((s) => s.key === Rooms.sectionKey(roomId))} size={56} />
        <div><h1 className="h2">{title}</h1><p className="muted">{t("room_subtitle")}</p></div>
      </header>

      {locked ? (
        <div className="panel locked-panel">
          <p>{t("course_room_locked")}</p>
          <Link className="btn btn-saffron" to={`/app/courses/${Rooms.courseId(roomId)}`}>{t("go_to_course")}</Link>
        </div>
      ) : (
        <>
          <div className="chat" aria-live="polite">
            {!loaded && <div className="page-center"><Spinner /></div>}
            {loaded && messages.length === 0 && <p className="chat-empty">{t("room_empty")}</p>}
            <AnimatePresence initial={false}>
              {messages.map((m) => {
                const mine = m.user_id === userId;
                return (
                  <motion.div key={m.id} className={`bubble${mine ? " mine" : ""}`} layout
                    initial={{ opacity: 0, y: 12, scale: 0.97 }} animate={{ opacity: 1, y: 0, scale: 1 }}>
                    <span className="bubble-meta"><b>{mine ? t("you") : m.author}</b> {time(m.created_at)}</span>
                    <span className="bubble-body">{m.body}</span>
                  </motion.div>
                );
              })}
            </AnimatePresence>
            <div ref={end} />
          </div>
          <form className="composer" onSubmit={submit}>
            <label className="sr-only" htmlFor="msg">{t("message_hint")}</label>
            <textarea id="msg" rows={1} maxLength={1000} placeholder={t("message_hint")} value={text}
              onChange={(e) => setText(e.target.value)}
              onKeyDown={(e) => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); e.currentTarget.form?.requestSubmit(); } }} />
            <button className="btn btn-primary" disabled={busy || !text.trim()}>{busy ? <Spinner /> : t("send")}</button>
          </form>
          <p className="small muted">{t("room_poll_note")}</p>
        </>
      )}
    </div>
  );
}
