// Copies the Android app's texts (English, Russian, Uzbek) into the website,
// so the app and the site say the same thing. Run: npm run strings
import { readFileSync, writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const here = dirname(fileURLToPath(import.meta.url));
const res = join(here, "../../app/src/main/res");
const files = { en: "values", ru: "values-ru", uz: "values-uz" };

const unescape = (s) =>
  s.replace(/\\'/g, "'").replace(/\\"/g, '"').replace(/\\n/g, "\n")
   .replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&apos;/g, "'").replace(/&amp;/g, "&");

const out = {};
for (const [lang, dir] of Object.entries(files)) {
  const xml = readFileSync(join(res, dir, "strings.xml"), "utf8");
  out[lang] = {};
  for (const m of xml.matchAll(/<string name="([^"]+)"[^>]*>([\s\S]*?)<\/string>/g)) out[lang][m[1]] = unescape(m[2]);
}
const missing = Object.keys(out.en).filter((k) => !(k in out.ru) || !(k in out.uz));
if (missing.length) throw new Error("missing translations: " + missing.join(", "));
writeFileSync(join(here, "../src/i18n/app-strings.json"), JSON.stringify(out, null, 1) + "\n");
console.log(`app-strings.json: ${Object.keys(out.en).length} keys × 3 languages`);
