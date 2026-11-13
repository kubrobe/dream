import { NavLink, Route, Routes, useLocation } from "react-router-dom";
import { AnimatePresence, motion } from "motion/react";
import { Brand, LangSwitch } from "../../components/ui";
import { useI18n } from "../../lib/i18n";
import { useAuth } from "../../lib/auth";
import { PathPage, GuidePage, CardModal } from "./InfoPages";
import { CoursesPage, CoursePage, LessonPage } from "./CoursePages";
import { RoomsPage, RoomPage } from "./RoomPages";
import { UnisPage } from "./UnisPage";
import { ProfilePage } from "./ProfilePage";
import NotFound from "../NotFound";

const TABS = [
  { to: "/app", end: true, glyph: "길", key: "tab_path" },
  { to: "/app/guide", glyph: "첫", key: "tab_guide" },
  { to: "/app/courses", glyph: "한", key: "tab_courses" },
  { to: "/app/rooms", glyph: "방", key: "tab_rooms" },
  { to: "/app/unis", glyph: "학", key: "tab_universities" },
  { to: "/app/profile", glyph: "나", key: "tab_profile" },
];

export default function AppShell() {
  const { t } = useI18n();
  const { profile, email } = useAuth();
  const loc = useLocation();
  const top = loc.pathname.split("/").slice(0, 3).join("/");
  return (
    <div className="shell">
      <a href="#main" className="skip">{t("skip_to_content")}</a>
      <aside className="rail">
        <Brand to="/" />
        <nav className="rail-nav" aria-label="Dream">
          {TABS.map((tab) => (
            <NavLink key={tab.to} to={tab.to} end={tab.end} className="rail-link">
              <span className="rail-glyph" aria-hidden="true">{tab.glyph}</span>
              <span>{t(tab.key)}</span>
            </NavLink>
          ))}
        </nav>
        <div className="rail-foot">
          <div className="rail-user">
            <span className="avatar" aria-hidden="true">{(profile.full_name || email || "?").slice(0, 1).toUpperCase()}</span>
            <span className="rail-user-text"><b>{profile.full_name || t("student")}</b><small>{email}</small></span>
          </div>
          <LangSwitch compact />
        </div>
      </aside>

      <main className="stage" id="main">
        <AnimatePresence mode="wait" initial={false}>
          <motion.div key={top} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }}
            transition={{ duration: 0.22, ease: "easeOut" }}>
            <Routes location={loc}>
              <Route index element={<PathPage />} />
              <Route path="guide" element={<GuidePage />} />
              <Route path="courses" element={<CoursesPage />} />
              <Route path="courses/:id" element={<CoursePage />} />
              <Route path="courses/:id/lessons/:lessonId" element={<LessonPage />} />
              <Route path="rooms" element={<RoomsPage />} />
              <Route path="rooms/:roomId" element={<RoomPage />} />
              <Route path="unis" element={<UnisPage />} />
              <Route path="profile" element={<ProfilePage />} />
              <Route path="*" element={<NotFound />} />
            </Routes>
          </motion.div>
        </AnimatePresence>
        <CardModal />
      </main>

      <nav className="tabbar" aria-label="Dream">
        {TABS.map((tab) => (
          <NavLink key={tab.to} to={tab.to} end={tab.end} className="tab-link">
            <span className="rail-glyph" aria-hidden="true">{tab.glyph}</span>
            <span>{t(tab.key)}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
