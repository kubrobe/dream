import { useEffect, useRef, type CSSProperties, type ReactNode } from "react";
import { Link } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import { LANGS, useI18n } from "../lib/i18n";
import { tileStyle } from "../lib/sections";
import type { Card } from "../lib/content";

export function Brand({ to = "/", light = false }: { to?: string; light?: boolean }) {
  return (
    <Link to={to} className={`brand${light ? " brand-light" : ""}`} aria-label="Dream">
      <span className="brand-glyph" aria-hidden="true">꿈</span>
      <span className="brand-word">Dream</span>
    </Link>
  );
}

export function LangSwitch({ compact = false }: { compact?: boolean }) {
  const { lang, setLang } = useI18n();
  return (
    <div className="lang-switch" role="group" aria-label="Language / Til / Язык">
      {LANGS.map((l) => (
        <button
          key={l.code}
          type="button"
          className={lang === l.code ? "on" : ""}
          aria-pressed={lang === l.code}
          onClick={() => setLang(l.code)}
          title={l.label}
        >
          {compact ? l.code.toUpperCase() : l.label}
        </button>
      ))}
    </div>
  );
}

/** A big hangul glyph on a coloured tile: the Dream signature for sections and courses. */
export function GlyphTile({ glyph, index, size = 56, style }: { glyph: string; index: number; size?: number; style?: CSSProperties }) {
  return (
    <span className="glyph-tile" aria-hidden="true" style={{ ...tileStyle(index), width: size, height: size, fontSize: size * 0.56, ...style }}>
      {glyph}
    </span>
  );
}

export function StatusBadge({ card }: { card: Pick<Card, "status" | "verified_at"> }) {
  const { t } = useI18n();
  if (card.status === "verified") return <span className="badge badge-ok">{t("status_verified", card.verified_at)}</span>;
  if (card.status === "tip") return <span className="badge badge-tip">{t("status_tip")}</span>;
  return <span className="badge badge-check">{t("status_check")}</span>;
}

export function Modal({ open, onClose, label, children, wide = false }: {
  open: boolean; onClose: () => void; label: string; children: ReactNode; wide?: boolean;
}) {
  const { t } = useI18n();
  const box = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!open) return;
    const prev = document.activeElement as HTMLElement | null;
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    setTimeout(() => box.current?.focus(), 30);
    return () => { document.removeEventListener("keydown", onKey); document.body.style.overflow = ""; prev?.focus?.(); };
  }, [open, onClose]);
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="modal-scrim" onMouseDown={(e) => e.target === e.currentTarget && onClose()}
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
          <motion.div ref={box} className={`modal${wide ? " modal-wide" : ""}`} role="dialog" aria-modal="true" aria-label={label} tabIndex={-1}
            initial={{ y: 40, opacity: 0, scale: 0.98 }} animate={{ y: 0, opacity: 1, scale: 1 }} exit={{ y: 24, opacity: 0 }}
            transition={{ type: "spring", stiffness: 380, damping: 34 }}>
            <button type="button" className="modal-close" onClick={onClose} aria-label={t("close")}>×</button>
            {children}
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export function Spinner() {
  const { t } = useI18n();
  return <span className="spinner" role="status" aria-label={t("loading")} />;
}

export function ProgressRing({ value, total, size = 120 }: { value: number; total: number; size?: number }) {
  const r = (size - 14) / 2;
  const c = 2 * Math.PI * r;
  const share = total ? value / total : 0;
  return (
    <svg className="ring" width={size} height={size} viewBox={`0 0 ${size} ${size}`} aria-hidden="true">
      <circle cx={size / 2} cy={size / 2} r={r} className="ring-track" />
      <motion.circle cx={size / 2} cy={size / 2} r={r} className="ring-fill"
        strokeDasharray={c} initial={false} animate={{ strokeDashoffset: c * (1 - share) }}
        transition={{ type: "spring", stiffness: 90, damping: 20 }}
        transform={`rotate(-90 ${size / 2} ${size / 2})`} />
      <text x="50%" y="50%" dominantBaseline="central" textAnchor="middle" className="ring-text">
        {value}/{total}
      </text>
    </svg>
  );
}
