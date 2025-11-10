const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function ingestText(text: string): Promise<string> {
    const response = await fetch(`${BASE_URL}/api/memory/text`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({text}),
    });

    if (!response.ok) {
        throw new Error(`Failed to ingest text: ${response.statusText}`);
    }

    const data = await response.json();
    return data.id;
}

export async function ingestFile(file: File): Promise<string> {
    const formData = new FormData();
    formData.append("document", file);

    const response = await fetch(`${BASE_URL}/api/memory/file`, {
        method: "POST",
        body: formData,
    });

    if (!response.ok) {
        throw new Error(`Failed to ingest file ${file.name}: ${response.statusText}`);
    }

    const data = await response.json();
    return data.id;
}

export interface GraphNode extends Record<string, unknown> {
    id: string;
    label: string;
    type: string;
    fullContent: string;
    metadata: string;
    createdAt: string;
}

export interface GraphEdge {
    id: string;
    source: string;
    target: string;
    label: string;
}

export interface GraphData {
    nodes: GraphNode[];
    edges: GraphEdge[];
}

export async function fetchGraph(): Promise<GraphData> {
    const response = await fetch(`${BASE_URL}/api/graph`);
    if (!response.ok) {
        throw new Error(`Failed to fetch graph: ${response.statusText}`);
    }
    return await response.json();
}

export interface SearchResult {
    id: string;
    content: string;
    score: number;
}

export async function searchMemory(query: string): Promise<SearchResult[]> {
    const params = new URLSearchParams({q: query});
    const response = await fetch(`${BASE_URL}/api/memory/search?${params.toString()}`);

    if (!response.ok) {
        throw new Error(`Search failed: ${response.statusText}`);
    }

    return await response.json();
}

export async function createEdge(sourceId: string, targetId: string, type: 'EXTEND' | 'UPDATE' | 'DERIVE'): Promise<void> {
    const response = await fetch(`${BASE_URL}/api/graph/edges`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({sourceId, targetId, type}),
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Failed to create edge: ${errorText || response.statusText}`);
    }
}