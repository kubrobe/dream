import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import APP from "../i18n/app-strings.json";
import { WEB_STRINGS } from "../i18n/web-strings";

export type Lang = "uz" | "en" | "ru";
export const LANGS: { code: Lang; label: string }[] = [
  { code: "uz", label: "Oʻzbek" },
  { code: "en", label: "English" },
  { code: "ru", label: "Русский" },
];

const app = APP as Record<Lang, Record<string, string>>;

/** Russian plural forms: "{шаг|шага|шагов}" picks the form for the first number argument. */
function ruPlural(n: number, forms: string[]): string {
  const a = Math.abs(n) % 100, b = a % 10;
  if (a > 10 && a < 20) return forms[2];
  if (b === 1) return forms[0];
  if (b >= 2 && b <= 4) return forms[1];
  return forms[2];
}

function format(text: string, args: (string | number)[]): string {
  const n = Number(args.find((a) => typeof a === "number") ?? 0);
  return text
    .replace(/\{([^|{}]+)\|([^|{}]+)\|([^{}]+)\}/g, (_, one, few, many) => ruPlural(n, [one, few, many])).replace(/%(\d+)\$[sd]/g, (_, n) => String(args[Number(n) - 1] ?? ""));
}

function initialLang(): Lang {
  try {
    const saved = localStorage.getItem("dream.lang");
    if (saved === "uz" || saved === "en" || saved === "ru") return saved;
  } catch { /* storage blocked */ }
  const nav = (navigator.language || "").toLowerCase();
  if (nav.startsWith("ru")) return "ru";
  if (nav.startsWith("en")) return "en";
  return "uz";
}

type Ctx = { lang: Lang; setLang: (l: Lang) => void; t: (key: string, ...args: (string | number)[]) => string };
const I18n = createContext<Ctx>(null as unknown as Ctx);

export function LangProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(initialLang);
  useEffect(() => {
    document.documentElement.lang = lang;
    try { localStorage.setItem("dream.lang", lang); } catch { /* ignore */ }
  }, [lang]);
  const t = useCallback(
    (key: string, ...args: (string | number)[]) => {
      const text = WEB_STRINGS[key]?.[lang] ?? app[lang]?.[key] ?? app.en[key] ?? key;
      return format(text, args);
    },
    [lang],
  );
  const value = useMemo(() => ({ lang, setLang: setLangState, t }), [lang, t]);
  return <I18n.Provider value={value}>{children}</I18n.Provider>;
}

export const useI18n = () => useContext(I18n);

/** A database row keeps Russian in `title` and translations in `title_en`, `title_uz`. Falls back to Russian. */
export function pick(row: Record<string, unknown>, field: string, lang: Lang): string {
  const base = String(row[field] ?? "");
  if (lang === "ru") return base;
  const tr = row[`${field}_${lang}`];
  return typeof tr === "string" && tr.trim() ? tr : base;
}
