import {Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle} from "@/components/ui/sheet.tsx";
import {Badge} from "@/components/ui/badge.tsx";
import {ScrollArea} from "@/components/ui/scroll-area.tsx";
import {Separator} from "@/components/ui/separator.tsx";
import type {MemoryNode} from "@/components/app/knowledge-graph/knowledge-graph-view.tsx";

interface NodeDetailsSheetProps {
    node: MemoryNode | null;
    onClose: () => void;
}

export function NodeDetailsSheet({node, onClose}: NodeDetailsSheetProps) {
    return (
        <Sheet open={!!node} onOpenChange={(open) => !open && onClose()}>
            <SheetContent className="w-[400px] sm:w-[540px]">
                <SheetHeader className="mb-4">
                    <div className="flex items-center gap-2">
                        <Badge variant={node?.data.type === 'FILE' ? 'secondary' : 'default'}>
                            {node?.data.type}
                        </Badge>
                        <SheetTitle className="truncate">Memory Details</SheetTitle>
                    </div>
                    <SheetDescription>
                        Created {node?.data.createdAt && new Date(node.data.createdAt).toLocaleString()}
                    </SheetDescription>
                </SheetHeader>

                {node && (
                    <div className="flex flex-col gap-4 h-full max-h-[calc(100vh-10rem)]">
                        <div>
                            <h4 className="text-sm font-medium mb-2">Content</h4>
                            <ScrollArea className="h-[200px] w-full rounded-md border p-4 bg-muted/50">
                                    <pre className="text-sm whitespace-pre-wrap font-sans">
                                        {node.data.fullContent}
                                    </pre>
                            </ScrollArea>
                        </div>

                        {node.data.metadata && node.data.metadata !== "{}" && (
                            <>
                                <Separator/>
                                <div>
                                    <h4 className="text-sm font-medium mb-2">Metadata</h4>
                                    <ScrollArea className="h-[100px] w-full rounded-md border p-4 bg-muted/50">
                                            <pre className="text-xs">
                                                {JSON.stringify(JSON.parse(node.data.metadata as string), null, 2)}
                                            </pre>
                                    </ScrollArea>
                                </div>
                            </>
                        )}

                        <Separator/>
                        <div className="text-xs text-muted-foreground">
                            ID: {node.id}
                        </div>
                    </div>
                )}
            </SheetContent>
        </Sheet>
    );
}