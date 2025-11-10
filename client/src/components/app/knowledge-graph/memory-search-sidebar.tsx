import {useState} from "react";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import {Loader2, Search} from "lucide-react";
import {searchMemory, type SearchResult} from "@/lib/api.ts";

interface MemorySearchSidebarProps {
    onResultClick: (nodeId: string) => void;
}

export function MemorySearchSidebar({onResultClick}: MemorySearchSidebarProps) {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<SearchResult[]>([]);
    const [isSearching, setIsSearching] = useState(false);

    const handleSearch = async (e?: React.FormEvent) => {
        e?.preventDefault();
        if (!query.trim()) return;

        setIsSearching(true);
        try {
            const data = await searchMemory(query);
            setResults(data);
        } catch (err) {
            console.error(err);
        } finally {
            setIsSearching(false);
        }
    };

    return (
        <div className="flex flex-col h-full">
            <div className="p-4 border-b">
                <h3 className="font-semibold mb-2 flex items-center gap-2">
                    <Search className="w-4 h-4"/>
                    Semantic Search
                </h3>
                <form onSubmit={handleSearch} className="flex gap-2">
                    <Input
                        value={query}
                        onChange={e => setQuery(e.target.value)}
                        placeholder="Search memories..."
                        className="h-8 text-sm"
                    />
                    <Button type="submit" size="sm" variant="secondary" disabled={isSearching}>
                        {isSearching ? <Loader2 className="w-3 h-3 animate-spin"/> : "Go"}
                    </Button>
                </form>
            </div>
            <ScrollArea className="flex-1">
                <div className="p-4 flex flex-col gap-2">
                    {results.length === 0 && !isSearching && query && (
                        <p className="text-xs text-muted-foreground text-center py-4">
                            No relevant memories found.
                        </p>
                    )}
                    {results.map(result => (
                        <button
                            key={result.id}
                            onClick={() => onResultClick(result.id)}
                            className="text-left p-3 rounded-lg border bg-card hover:bg-accent/50 transition-colors text-sm flex flex-col gap-1"
                        >
                            <div className="font-medium line-clamp-2">
                                {result.content}
                            </div>
                            <div className="text-xs text-muted-foreground flex justify-between">
                                <span>Similarity match</span>
                                <span className="font-mono">{(result.score * 100).toFixed(0)}%</span>
                            </div>
                        </button>
                    ))}
                </div>
            </ScrollArea>
        </div>
    );
}