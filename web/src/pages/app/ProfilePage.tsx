import { useNavigate } from "react-router-dom";
import { LANGS, useI18n } from "../../lib/i18n";
import { useAuth, type UserError } from "../../lib/auth";
import { useUserData } from "../../lib/data";
import { useToast } from "../../lib/toast";
import { useContent } from "../../lib/content";
import { GOALS, goalOf } from "../../lib/sections";
import { useTheme, type ThemeMode } from "../../lib/theme";

export function ProfilePage() {
  const { t, lang, setLang } = useI18n();
  const { profile, email, saveProfile, signOut } = useAuth();
  const { done, owned, interests } = useUserData();
  const { cards, live } = useContent();
  const toast = useToast();
  const nav = useNavigate();
  const [theme, setTheme] = useTheme();
  const total = cards.filter((c) => c.kind === "roadmap").length;
  const goal = goalOf(profile.goal);

  async function pickGoal(g: string) {
    try { await saveProfile({ goal: g }); toast(t("saved")); }
    catch (e) { const u = e as UserError; toast(t(u.key, ...(u.args ?? [])), "error"); }
  }

  return (
    <div className="page">
      <header className="profile-head">
        <span className="avatar avatar-lg" aria-hidden="true">{(profile.full_name || email || "?").slice(0, 1).toUpperCase()}</span>
        <div>
          <h1 className="h1">{profile.full_name || t("student")}</h1>
          <p className="muted">{email}</p>
          <p className={`live-note${live ? " is-live" : ""}`}>{live ? t("online_mode") : t("facts_seed")}</p>
        </div>
      </header>

      <dl className="stats">
        <div><dt>{t("stat_steps", total)}</dt><dd>{done.size}</dd></div>
        <div><dt>{t("stat_courses")}</dt><dd>{owned.size}</dd></div>
        <div><dt>{t("stat_unis")}</dt><dd>{interests.size}</dd></div>
      </dl>

      <section className="panel">
        <h2 className="h3">{t("my_goal")}</h2>
        <p className="muted small">{t("my_goal_hint")}</p>
        <div className="chips" role="radiogroup" aria-label={t("my_goal")}>
          {GOALS.map((g) => (
            <button key={g} role="radio" aria-checked={goal === g} className={`chip${goal === g ? " on" : ""}`} onClick={() => pickGoal(g)}>
              {t(`goal_${g}`)}
            </button>
          ))}
        </div>
      </section>

      <section className="panel">
        <h2 className="h3">{t("settings")}</h2>
        <div className="setting">
          <span>{t("language")}</span>
          <div className="chips">
            {LANGS.map((l) => (
              <button key={l.code} className={`chip${lang === l.code ? " on" : ""}`} aria-pressed={lang === l.code} onClick={() => setLang(l.code)}>{l.label}</button>
            ))}
          </div>
        </div>
        <div className="setting">
          <span>{t("theme")}</span>
          <div className="chips">
            {(["system", "light", "dark"] as ThemeMode[]).map((m) => (
              <button key={m} className={`chip${theme === m ? " on" : ""}`} aria-pressed={theme === m} onClick={() => setTheme(m)}>{t(`theme_${m}`)}</button>
            ))}
          </div>
        </div>
      </section>

      <button className="btn btn-danger" onClick={async () => { await signOut(); nav("/", { replace: true }); }}>{t("logout")}</button>
      <p className="small muted about">{t("about_line")}</p>
    </div>
  );
}
