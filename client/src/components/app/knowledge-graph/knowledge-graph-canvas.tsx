import {useEffect} from 'react';
import {
    Background,
    type Connection,
    Controls,
    type Edge,
    type EdgeChange,
    MiniMap,
    type NodeChange,
    ReactFlow,
    useReactFlow,
} from '@xyflow/react';
import dagre from 'dagre';
import type {MemoryNode} from "@/components/app/knowledge-graph/knowledge-graph-view.tsx";

interface KnowledgeGraphCanvasProps {
    nodes: MemoryNode[];
    edges: Edge[];
    onNodesChange: (changes: NodeChange<MemoryNode>[]) => void;
    onEdgesChange: (changes: EdgeChange[]) => void;
    onConnect: (connection: Connection) => void;
    onNodeClick: (node: MemoryNode) => void;
}

const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 180;
const nodeHeight = 50;

const getLayoutedElements = (nodes: MemoryNode[], edges: Edge[]) => {
    dagreGraph.setGraph({rankdir: 'TB', ranksep: 80, nodesep: 30});

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
            // Removed targetPosition and sourcePosition to allow handles on all sides
            position: {
                x: nodeWithPosition.x - nodeWidth / 2,
                y: nodeWithPosition.y - nodeHeight / 2,
            },
            style: {
                background: node.data.type === 'FILE' ? '#eef2ff' : '#fff',
                border: '1px solid #777',
                borderRadius: '8px',
                padding: '10px',
                fontSize: '12px',
                width: nodeWidth,
                cursor: 'pointer',
            }
        };
    });

    return {nodes: newNodes, edges};
};

export function KnowledgeGraphCanvas({
                                         nodes,
                                         edges,
                                         onNodesChange,
                                         onEdgesChange,
                                         onConnect,
                                         onNodeClick
                                     }: KnowledgeGraphCanvasProps) {
    const {setNodes, setEdges, fitView} = useReactFlow();

    useEffect(() => {
        if (nodes.length > 0) {
            const {nodes: layoutedNodes, edges: layoutedEdges} = getLayoutedElements(nodes, edges);
            setNodes(layoutedNodes);
            setEdges(layoutedEdges);
            window.requestAnimationFrame(() => fitView());
        }
    }, [nodes.length, edges.length, setNodes, setEdges, fitView]);

    return (
        <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={(_, node) => onNodeClick(node)}
            fitView
            minZoom={0.1}
        >
            <Controls/>
            <MiniMap zoomable pannable/>
            <Background gap={12} size={1}/>
        </ReactFlow>
    );
}