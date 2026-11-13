import { useState, type FormEvent } from "react";
import { Link, Navigate, useNavigate, useSearchParams } from "react-router-dom";
import { motion } from "motion/react";
import { Brand, LangSwitch, Spinner } from "../components/ui";
import { useI18n } from "../lib/i18n";
import { UserError, useAuth } from "../lib/auth";

function AuthLayout({ children }: { children: React.ReactNode }) {
  const { t } = useI18n();
  return (
    <div className="auth">
      <div className="auth-side" aria-hidden="true">
        <Brand light />
        <span className="auth-glyph">꿈</span>
        <p className="auth-meaning">{t("dream_meaning")}</p>
      </div>
      <main className="auth-main" id="main">
        <div className="auth-top"><span className="auth-brand-sm"><Brand /></span><LangSwitch compact /></div>
        <motion.div className="auth-box" initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }}>
          {children}
        </motion.div>
      </main>
    </div>
  );
}

const nextPath = (q: URLSearchParams) => {
  const n = q.get("next") || "/app";
  return n.startsWith("/app") ? n : "/app";
};

export function Login() {
  const { t } = useI18n();
  const { signIn, session } = useAuth();
  const [q] = useSearchParams();
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<UserError | null>(null);
  const [busy, setBusy] = useState(false);
  if (session) return <Navigate to={nextPath(q)} replace />;

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!email.trim() || !password) return setError(new UserError("err_fill_login"));
    setBusy(true); setError(null);
    try { await signIn(email.trim(), password); nav(nextPath(q), { replace: true }); }
    catch (err) { setError(err as UserError); }
    finally { setBusy(false); }
  }

  return (
    <AuthLayout>
      <h1 className="h2">{t("auth_login_title")}</h1>
      <p className="muted">{t("auth_login_hint")}</p>
      <form onSubmit={submit} className="form" noValidate>
        <label className="field"><span>{t("field_email")}</span>
          <input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="field"><span>{t("field_password")}</span>
          <input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {error && <p className="form-error" role="alert">{t(error.key, ...error.args)}</p>}
        <button className="btn btn-primary btn-lg btn-block" disabled={busy}>{busy ? <Spinner /> : t("auth_login_button")}</button>
      </form>
      <p className="auth-switch"><Link to={`/register${q.get("next") ? `?next=${encodeURIComponent(q.get("next")!)}` : ""}`}>{t("auth_to_register")}</Link></p>
      <p className="small muted">{t("web_and_app")}</p>
    </AuthLayout>
  );
}

function strength(p: string): 0 | 1 | 2 | 3 {
  if (!p) return 0;
  if (p.length < 6) return 1;
  const kinds = [/[a-z]/, /[A-Z]/, /\d/, /[^A-Za-z0-9]/].filter((r) => r.test(p)).length;
  return p.length >= 10 && kinds >= 3 ? 3 : 2;
}

export function Register() {
  const { t } = useI18n();
  const { signUp, session } = useAuth();
  const [q] = useSearchParams();
  const nav = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<UserError | null>(null);
  const [info, setInfo] = useState("");
  const [busy, setBusy] = useState(false);
  if (session) return <Navigate to={nextPath(q)} replace />;
  const s = strength(password);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null); setInfo("");
    if (!name.trim()) return setError(new UserError("err_name"));
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim())) return setError(new UserError("err_email"));
    if (password.length < 6) return setError(new UserError("err_password_short"));
    setBusy(true);
    try {
      const r = await signUp(name.trim(), email.trim(), password);
      if (r === "in") nav(nextPath(q), { replace: true }); else setInfo(t("auth_confirm_email"));
    } catch (err) { setError(err as UserError); }
    finally { setBusy(false); }
  }

  return (
    <AuthLayout>
      <h1 className="h2">{t("auth_register_title")}</h1>
      <p className="muted">{t("auth_register_hint")}</p>
      <form onSubmit={submit} className="form" noValidate>
        <label className="field"><span>{t("field_name")}</span>
          <input autoComplete="name" value={name} onChange={(e) => setName(e.target.value)} maxLength={80} required />
        </label>
        <label className="field"><span>{t("field_email")}</span>
          <input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label className="field"><span>{t("field_password_new")}</span>
          <input type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </label>
        {password && (
          <div className={`meter meter-${s}`} aria-live="polite">
            <span className="meter-bar"><i /><i /><i /></span>
            <span>{t(s === 1 ? "password_weak" : s === 2 ? "password_ok" : "password_strong")}</span>
          </div>
        )}
        {error && <p className="form-error" role="alert">{t(error.key, ...error.args)}</p>}
        {info && <p className="form-info" role="status">{info}</p>}
        <button className="btn btn-primary btn-lg btn-block" disabled={busy}>{busy ? <Spinner /> : t("auth_register_button")}</button>
      </form>
      <p className="auth-switch"><Link to="/login">{t("auth_to_login")}</Link></p>
      <p className="small muted">{t("web_and_app")}</p>
    </AuthLayout>
  );
}
