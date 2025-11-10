import {useEffect, useState} from 'react';
import {
    addEdge,
    type Connection,
    type Edge,
    type Node,
    ReactFlowProvider,
    useEdgesState,
    useNodesState,
    useReactFlow
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import {createEdge, fetchGraph, type GraphNode} from "@/lib/api.ts";
import {NodeDetailsSheet} from "@/components/app/knowledge-graph/node-details-sheet.tsx";
import {KnowledgeGraphCanvas} from "@/components/app/knowledge-graph/knowledge-graph-canvas.tsx";
import {MemorySearchSidebar} from "@/components/app/knowledge-graph/memory-search-sidebar.tsx";
import {CreateEdgeDialog} from "@/components/app/knowledge-graph/create-edge-dialog.tsx";

export type MemoryNode = Node<GraphNode>;

function KnowledgeGraphViewContent() {
    const [nodes, setNodes, onNodesChange] = useNodesState<MemoryNode>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
    const [selectedNode, setSelectedNode] = useState<MemoryNode | null>(null);
    const [pendingConnection, setPendingConnection] = useState<Connection | null>(null);
    const {setCenter} = useReactFlow();

    const refreshGraph = () => {
        fetchGraph().then(data => {
            const initialNodes: MemoryNode[] = data.nodes.map(n => ({
                id: n.id,
                data: {...n},
                position: {x: 0, y: 0},
                type: 'default'
            }));

            const initialEdges: Edge[] = data.edges.map(e => ({
                id: e.id,
                source: e.source,
                target: e.target,
                label: e.label,
                animated: e.label === 'EXTEND',
                style: {
                    stroke: e.label === 'UPDATE' ? '#ef4444' :
                        e.label === 'DERIVE' ? '#a855f7' : '#555'
                }
            }));

            setNodes(initialNodes);
            setEdges(initialEdges);
        }).catch(err => console.error("Failed to load graph", err));
    };

    useEffect(() => {
        refreshGraph();
    }, []);

    const handleConnect = (connection: Connection) => {
        if (connection.source !== connection.target) {
            setPendingConnection(connection);
        }
    };

    const handleCreateEdgeConfirm = async (type: 'EXTEND' | 'UPDATE' | 'DERIVE') => {
        if (!pendingConnection?.source || !pendingConnection?.target) return;

        try {
            await createEdge(pendingConnection.source, pendingConnection.target, type);
            setEdges((eds) => addEdge({
                ...pendingConnection,
                label: type,
                animated: type === 'EXTEND',
                style: {
                    stroke: type === 'UPDATE' ? '#ef4444' :
                        type === 'DERIVE' ? '#a855f7' : '#555'
                }
            }, eds));
        } catch (e) {
            console.error(e);
            alert("Failed to create edge. See console.");
        } finally {
            setPendingConnection(null);
        }
    };

    const handleSearchResultClick = (nodeId: string) => {
        const node = nodes.find(n => n.id === nodeId);
        if (node) {
            setCenter(node.position.x, node.position.y, {zoom: 1.5, duration: 800});
            setSelectedNode(node);
        }
    };

    return (
        <div className="w-full h-full flex border rounded-lg overflow-hidden bg-slate-50"
             style={{height: 'calc(100vh - 8rem)'}}>
            <div className="w-80 border-r bg-white">
                <MemorySearchSidebar onResultClick={handleSearchResultClick}/>
            </div>
            <div className="flex-1 h-full relative">
                <KnowledgeGraphCanvas
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={handleConnect}
                    onNodeClick={setSelectedNode}
                />
            </div>

            <NodeDetailsSheet
                node={selectedNode}
                onClose={() => setSelectedNode(null)}
            />

            <CreateEdgeDialog
                isOpen={!!pendingConnection}
                onClose={() => setPendingConnection(null)}
                onConfirm={handleCreateEdgeConfirm}
            />
        </div>
    );
}

export function KnowledgeGraphView() {
    return (
        <ReactFlowProvider>
            <KnowledgeGraphViewContent/>
        </ReactFlowProvider>
    );
}