const BASE_URL = import.meta.env.VITE_API_BASE_URL;

export async function ingestText(text: string): Promise<string> {
    const response = await fetch(`${BASE_URL}/api/memory/text`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
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
    formData.append('document', file);

    const response = await fetch(`${BASE_URL}/api/memory/file`, {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        throw new Error(`Failed to ingest file ${file.name}: ${response.statusText}`);
    }

    const data = await response.json();
    return data.id;
}

export interface GraphNode {
    id: string;
    label: string;
    type: string;
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