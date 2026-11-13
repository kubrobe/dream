import { useState } from "react";
import { motion } from "motion/react";
import { Modal, Spinner } from "../../components/ui";
import { pick, useI18n } from "../../lib/i18n";
import { useUserData } from "../../lib/data";
import { useToast } from "../../lib/toast";
import { useContent, type University } from "../../lib/content";
import type { UserError } from "../../lib/auth";

export function UnisPage() {
  const { t, lang } = useI18n();
  const { universities } = useContent();
  const { interests, setInterest } = useUserData();
  const toast = useToast();
  const [asking, setAsking] = useState<University | null>(null);
  const [agree, setAgree] = useState(false);
  const [busy, setBusy] = useState<string | null>(null);

  async function change(u: University, on: boolean) {
    setBusy(u.id);
    try { await setInterest(u.id, on); setAsking(null); toast(on ? t("in_interest_list") : t("saved")); }
    catch (e) { const err = e as UserError; toast(t(err.key, ...(err.args ?? [])), "error"); }
    finally { setBusy(null); }
  }

  return (
    <div className="page">
      <header className="page-head">
        <h1 className="h1">{t("unis_title")}</h1>
        <p className="lead">{t("unis_subtitle")}</p>
        <p className="pill pill-open">{t("unis_count", interests.size)}</p>
      </header>
      <p className="demo-note">{t("demo_unis")}</p>
      <ul className="uni-rows">
        {universities.map((u) => {
          const on = interests.has(u.id);
          return (
            <motion.li key={u.id} layout className={`uni-row${on ? " on" : ""}`}>
              <div className="uni-row-main">
                <span className="uni-name">{u.name}</span>
                <span className="muted small">{pick(u, "city", lang)} · {pick(u, "type", lang)}</span>
              </div>
              {u.url && <a className="btn btn-quiet btn-sm" href={u.url} target="_blank" rel="noreferrer">{t("website")}</a>}
              {on
                ? <button className="btn btn-quiet btn-sm" disabled={busy === u.id} onClick={() => change(u, false)}>{t("withdraw_interest")}</button>
                : <button className="btn btn-primary btn-sm" onClick={() => { setAgree(false); setAsking(u); }}>{t("interested")}</button>}
            </motion.li>
          );
        })}
      </ul>

      <Modal open={!!asking} onClose={() => setAsking(null)} label={asking?.name ?? ""}>
        {asking && (
          <div className="consent">
            <h2 className="h2">{asking.name}</h2>
            <p>{t("consent_text")}</p>
            <label className="consent-demo">
              <input type="checkbox" checked={agree} onChange={(e) => setAgree(e.target.checked)} />
              <span>{t("consent_checkbox")}</span>
            </label>
            <p className="small muted">{t("consent_revoke")}</p>
            <div className="row">
              <button className="btn btn-quiet" onClick={() => setAsking(null)}>{t("cancel")}</button>
              <button className="btn btn-primary" disabled={!agree || busy === asking.id} onClick={() => change(asking, true)}>
                {busy === asking.id ? <Spinner /> : t("confirm")}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
