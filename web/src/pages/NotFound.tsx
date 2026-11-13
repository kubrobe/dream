import { Link } from "react-router-dom";
import { Brand } from "../components/ui";
import { useI18n } from "../lib/i18n";

export default function NotFound() {
  const { t } = useI18n();
  return (
    <main className="page-center notfound" id="main">
      <Brand />
      <span className="notfound-glyph" aria-hidden="true">길?</span>
      <h1 className="h2">{t("not_found_title")}</h1>
      <p className="muted">{t("not_found_body")}</p>
      <Link className="btn btn-primary" to="/">{t("home")}</Link>
    </main>
  );
}
