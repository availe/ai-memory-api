import {AppNavigationMenu} from "@/components/app/navigation-menu/app-navigation-menu.tsx";
import {MemoryIngestPanel} from "@/components/app/memory-ingest/memory-ingest-panel.tsx";
import {useState} from "react";
import {KnowledgeGraphView} from "@/components/app/knowledge-graph/knowledge-graph-view.tsx";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs.tsx";

function App() {
    const [activeTab, setActiveTab] = useState("ingest")

    return (
        <div className="min-h-screen w-full flex flex-col">
            <header
                className="sticky top-0 z-50 w-full border-b bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <AppNavigationMenu/>
            </header>

            <main className="flex-1 flex flex-col items-center p-4">
                <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full max-w-6xl flex flex-col flex-1">
                    <TabsList className="grid w-full max-w-md grid-cols-2 mx-auto mb-8">
                        <TabsTrigger value="ingest">Ingest Data</TabsTrigger>
                        <TabsTrigger value="graph">Knowledge Graph</TabsTrigger>
                    </TabsList>
                    <TabsContent value="ingest" className="flex-1 flex justify-center">
                        <section className="w-2/3 min-w-[300px]">
                            <MemoryIngestPanel/>
                        </section>
                    </TabsContent>
                    <TabsContent value="graph" className="flex-1 h-full">
                        <KnowledgeGraphView/>
                    </TabsContent>
                </Tabs>
            </main>
        </div>
    )
}

export default App