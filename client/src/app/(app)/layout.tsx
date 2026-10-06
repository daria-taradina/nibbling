import { UserProvider } from "@/components/auth/UserProvider";
import { Navbar } from "@/components/layout/Navbar";

// Shell for every signed-in page: auth gate + navbar. Pages render below it.
export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <UserProvider>
      <div className="min-h-screen bg-cream-bg dark:bg-ink-bg">
        <Navbar />
        <main className="mx-auto max-w-7xl px-4 py-4 sm:px-6">{children}</main>
      </div>
    </UserProvider>
  );
}
