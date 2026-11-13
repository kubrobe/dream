import { StrictMode, lazy, Suspense, useEffect } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { MotionConfig } from "motion/react";
import "@fontsource/unbounded/600.css";
import "@fontsource/unbounded/700.css";
import "@fontsource/unbounded/800.css";
import "@fontsource/onest/400.css";
import "@fontsource/onest/500.css";
import "@fontsource/onest/600.css";
import "@fontsource/onest/700.css";
import "./styles.css";
import "./lib/theme";
import { LangProvider } from "./lib/i18n";
import { AuthProvider, useAuth } from "./lib/auth";
import { UserDataProvider } from "./lib/data";
import { ToastProvider } from "./lib/toast";
import Landing from "./pages/Landing";
import { Login, Register } from "./pages/Auth";
import NotFound from "./pages/NotFound";
import { Spinner } from "./components/ui";

const AppShell = lazy(() => import("./pages/app/AppShell"));

function RequireAuth({ children }: { children: React.ReactNode }) {
  const { session, ready } = useAuth();
  const loc = useLocation();
  if (!ready) return <div className="page-center"><Spinner /></div>;
  if (!session) return <Navigate to={`/login?next=${encodeURIComponent(loc.pathname)}`} replace />;
  return <>{children}</>;
}

function ScrollToTop() {
  const { pathname } = useLocation();
  // a new page starts at the top (hash links on the landing page handle themselves)
  useEffect(() => { if (!window.location.hash) window.scrollTo(0, 0); }, [pathname]);
  return null;
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <MotionConfig reducedMotion="user">
      <LangProvider>
        <AuthProvider>
          <UserDataProvider>
            <ToastProvider>
              <BrowserRouter>
                <ScrollToTop />
                <Routes>
                  <Route path="/" element={<Landing />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />
                  <Route path="/app/*" element={
                    <RequireAuth>
                      <Suspense fallback={<div className="page-center"><Spinner /></div>}><AppShell /></Suspense>
                    </RequireAuth>
                  } />
                  <Route path="*" element={<NotFound />} />
                </Routes>
              </BrowserRouter>
            </ToastProvider>
          </UserDataProvider>
        </AuthProvider>
      </LangProvider>
    </MotionConfig>
  </StrictMode>,
);
