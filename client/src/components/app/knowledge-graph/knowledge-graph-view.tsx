import {useCallback, useEffect} from 'react';
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
import {fetchGraph} from "@/lib/api.ts";

const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 180;
const nodeHeight = 50;

const getLayoutedElements = (nodes: Node[], edges: Edge[], direction = 'TB') => {
    const isHorizontal = direction === 'LR';
    dagreGraph.setGraph({rankdir: direction});

    nodes.forEach((node) => {
        dagreGraph.setNode(node.id, {width: nodeWidth, height: nodeHeight});
    });

    edges.forEach((edge) => {
        dagreGraph.setEdge(edge.source, edge.target);
    });

    dagre.layout(dagreGraph);

    const newNodes = nodes.map((node) => {
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

export function KnowledgeGraphView() {
    const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);

    const onConnect = useCallback(
        (params: Connection) => setEdges((eds) => addEdge(params, eds)),
        [setEdges],
    );

    useEffect(() => {
        fetchGraph().then(data => {
            const initialNodes: Node[] = data.nodes.map(n => ({
                id: n.id,
                data: {label: n.label},
                position: {x: 0, y: 0},
                style: {
                    background: n.type === 'FILE' ? '#eef2ff' : '#fff',
                    border: '1px solid #777',
                    borderRadius: '8px',
                    padding: '10px',
                    fontSize: '12px',
                    width: nodeWidth,
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

            const {nodes: layoutedNodes, edges: layoutedEdges} = getLayoutedElements(initialNodes, initialEdges);

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
                fitView
            >
                <Controls/>
                <MiniMap/>
                <Background gap={12} size={1}/>
            </ReactFlow>
        </div>
    );
}