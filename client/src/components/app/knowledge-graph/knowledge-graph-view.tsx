import {useCallback, useEffect, useState} from 'react';
import {
    addEdge,
    Background,
    type Connection,
    Controls,
    type Edge,
    MiniMap,
    type Node,
    Position,
    ReactFlow,
    useEdgesState,
    useNodesState,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import dagre from 'dagre';
import {fetchGraph, type GraphNode} from "@/lib/api.ts";
import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle,} from "@/components/ui/sheet";
import {Badge} from "@/components/ui/badge.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import {Separator} from "@/components/ui/separator.tsx";

const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 180;
const nodeHeight = 50;

const getLayoutedElements = <D extends Record<string, unknown>>(nodes: Node<D>[], edges: Edge[], direction: 'TB' | 'LR' = 'TB') => {
    const isHorizontal = direction === 'LR';
    dagreGraph.setGraph({rankdir: direction});

    nodes.forEach((node) => {
        dagreGraph.setNode(node.id, {width: nodeWidth, height: nodeHeight});
    });

    edges.forEach((edge) => {
        dagreGraph.setEdge(edge.source, edge.target);
    });

    dagre.layout(dagreGraph);

    const newNodes = nodes.map<Node<D>>((node) => {
        const nodeWithPosition = dagreGraph.node(node.id);
        return {
            ...node,
            targetPosition: isHorizontal ? Position.Left : Position.Top,
            sourcePosition: isHorizontal ? Position.Right : Position.Bottom,
            position: {
                x: nodeWithPosition.x - nodeWidth / 2,
                y: nodeWithPosition.y - nodeHeight / 2,
            },
        };
    });

    return {nodes: newNodes, edges};
};

type MemoryNode = Node<GraphNode>;

export function KnowledgeGraphView() {
    const [nodes, setNodes, onNodesChange] = useNodesState<MemoryNode>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
    const [selectedNode, setSelectedNode] = useState<MemoryNode | null>(null);

    const onConnect = useCallback(
        (params: Connection) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    const onNodeClick = useCallback((_: React.MouseEvent, node: MemoryNode) => {
        setSelectedNode(node);
    }, []);

    useEffect(() => {
        fetchGraph().then(data => {
            const initialNodes: MemoryNode[] = data.nodes.map(n => ({
                id: n.id,
                data: {...n},
                position: {x: 0, y: 0},
                style: {
                    background: n.type === 'FILE' ? '#eef2ff' : '#fff',
                    border: '1px solid #777',
                    borderRadius: '8px',
                    padding: '10px',
                    fontSize: '12px',
                    width: nodeWidth,
                    cursor: 'pointer',
                }
            }));

            const initialEdges: Edge[] = data.edges.map(e => ({
                id: e.id,
                source: e.source,
                target: e.target,
                label: e.label,
                animated: true,
                style: {stroke: '#555'}
            }));

            const {
                nodes: layoutedNodes,
                edges: layoutedEdges
            } = getLayoutedElements<GraphNode>(initialNodes, initialEdges);

            setNodes(layoutedNodes);
            setEdges(layoutedEdges);
        }).catch(err => console.error("Failed to load graph", err));
    }, [setNodes, setEdges]);

    return (
        <div className="w-full h-full min-h-[500px] border rounded-lg bg-slate-50" style={{height: '600px'}}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                onNodeClick={onNodeClick}
                fitView
            >
                <Controls/>
                <MiniMap/>
                <Background gap={12} size={1}/>
            </ReactFlow>

            <Sheet open={!!selectedNode} onOpenChange={(open) => !open && setSelectedNode(null)}>
                <SheetContent className="w-[400px] sm:w-[540px]">
                    <SheetHeader className="mb-4">
                        <div className="flex items-center gap-2">
                            <Badge variant={selectedNode?.data.type === 'FILE' ? 'secondary' : 'default'}>
                                {selectedNode?.data.type}
                            </Badge>
                            <SheetTitle className="truncate">Memory Details</SheetTitle>
                        </div>
                        <SheetDescription>
                            Created {selectedNode?.data.createdAt && new Date(selectedNode.data.createdAt).toLocaleString()}
                        </SheetDescription>
                    </SheetHeader>

                    {selectedNode && (
                        <div className="flex flex-col gap-4 h-full max-h-[calc(100vh-10rem)]">
                            <div>
                                <h4 className="text-sm font-medium mb-2">Content</h4>
                                <ScrollArea className="h-[200px] w-full rounded-md border p-4 bg-muted/50">
                                    <pre className="text-sm whitespace-pre-wrap font-sans">
                                        {selectedNode.data.fullContent}
                                    </pre>
                                </ScrollArea>
                            </div>

                            {selectedNode.data.metadata && selectedNode.data.metadata !== "{}" && (
                                <>
                                    <Separator/>
                                    <div>
                                        <h4 className="text-sm font-medium mb-2">Metadata</h4>
                                        <ScrollArea className="h-[100px] w-full rounded-md border p-4 bg-muted/50">
                                            <pre className="text-xs">
                                                {JSON.stringify(JSON.parse(selectedNode.data.metadata as string), null, 2)}
                                            </pre>
                                        </ScrollArea>
                                    </div>
                                </>
                            )}

                            <Separator/>
                            <div className="text-xs text-muted-foreground">
                                ID: {selectedNode.id}
                            </div>
                        </div>
                    )}
                </SheetContent>
            </Sheet>
        </div>
    );
}
