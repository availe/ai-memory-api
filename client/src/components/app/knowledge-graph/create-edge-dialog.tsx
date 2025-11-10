import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle
} from "@/components/ui/dialog.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Label} from "@/components/ui/label.tsx";
import {RadioGroup, RadioGroupItem} from "@/components/ui/radio-group.tsx";
import {useState} from "react";

interface CreateEdgeDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: (type: 'EXTEND' | 'UPDATE' | 'DERIVE') => void;
}

export function CreateEdgeDialog({isOpen, onClose, onConfirm}: CreateEdgeDialogProps) {
    const [type, setType] = useState<'EXTEND' | 'UPDATE' | 'DERIVE'>('EXTEND');

    const handleConfirm = () => {
        onConfirm(type);
        setType('EXTEND'); // Reset for next time
    };

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Connect Memories</DialogTitle>
                    <DialogDescription>
                        Define the relationship between these two memories.
                    </DialogDescription>
                </DialogHeader>

                <div className="py-4">
                    <RadioGroup value={type} onValueChange={(v) => setType(v as any)} className="gap-4">
                        <div className="flex items-start space-x-2">
                            <RadioGroupItem value="EXTEND" id="r-extend" className="mt-1"/>
                            <div className="grid gap-1.5 leading-none">
                                <Label htmlFor="r-extend" className="font-medium">EXTEND (Default)</Label>
                                <p className="text-sm text-muted-foreground">
                                    Adds new information or context to the original memory.
                                </p>
                            </div>
                        </div>
                        <div className="flex items-start space-x-2">
                            <RadioGroupItem value="UPDATE" id="r-update" className="mt-1"/>
                            <div className="grid gap-1.5 leading-none">
                                <Label htmlFor="r-update" className="font-medium">UPDATE</Label>
                                <p className="text-sm text-muted-foreground">
                                    Supersedes the original memory because it is now outdated or incorrect.
                                </p>
                            </div>
                        </div>
                        <div className="flex items-start space-x-2">
                            <RadioGroupItem value="DERIVE" id="r-derive" className="mt-1"/>
                            <div className="grid gap-1.5 leading-none">
                                <Label htmlFor="r-derive" className="font-medium">DERIVE</Label>
                                <p className="text-sm text-muted-foreground">
                                    An insight or conclusion drawn from the original memory.
                                </p>
                            </div>
                        </div>
                    </RadioGroup>
                </div>

                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>Cancel</Button>
                    <Button onClick={handleConfirm}>Connect</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}