import {AppNavigationMenu} from "@/components/app/navigation-menu/app-navigation-menu.tsx";
import {MemoryIngestPanel} from "@/components/app/memory-ingest/memory-ingest-panel.tsx";

function App() {
    return (
        <div className="min-h-screen w-full flex flex-col">
            <header
                className="sticky top-0 z-50 w-full border-b bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <AppNavigationMenu/>
            </header>

            <main className="flex-1 flex justify-center items-stretch">
                <section
                    aria-labelledby="memory-ingest-heading"
                    className="w-2/3 min-w-[300px]"
                >
                    <h2 id="memory-ingest-heading" className="sr-only">
                        Memory Ingest Panel
                    </h2>
                    <MemoryIngestPanel/>
                </section>
            </main>
        </div>
    )
}

export default App
