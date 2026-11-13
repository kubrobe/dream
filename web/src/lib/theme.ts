import { useEffect, useState } from "react";

export type ThemeMode = "system" | "light" | "dark";

function read(): ThemeMode {
  try {
    const v = localStorage.getItem("dream.theme");
    if (v === "light" || v === "dark" || v === "system") return v;
  } catch { /* ignore */ }
  return "system";
}

function apply(mode: ThemeMode) {
  const dark = mode === "dark" || (mode === "system" && matchMedia("(prefers-color-scheme: dark)").matches);
  document.documentElement.dataset.theme = dark ? "dark" : "light";
}

apply(read());

export function useTheme() {
  const [mode, setMode] = useState<ThemeMode>(read);
  useEffect(() => {
    apply(mode);
    try { localStorage.setItem("dream.theme", mode); } catch { /* ignore */ }
    if (mode !== "system") return;
    const mq = matchMedia("(prefers-color-scheme: dark)");
    const on = () => apply("system");
    mq.addEventListener("change", on);
    return () => mq.removeEventListener("change", on);
  }, [mode]);
  return [mode, setMode] as const;
}
