import {Textarea} from "@/components/ui/textarea.tsx";
import {useState} from "react";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";
import {ingestText} from "@/lib/api.ts";

export function UploadTextContent() {
    const [body, setBody] = useState("");
    const [title, setTitle] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!body.trim()) {
            alert("Please paste some text before submitting.");
            return;
        }

        try {
            setIsSubmitting(true);
            const textToIngest = title ? `# ${title}\n\n${body}` : body;
            await ingestText(textToIngest);
            alert("Memory ingested successfully!");
            setBody("");
            setTitle("");
        } catch (error) {
            console.error("Ingestion failed:", error);
            alert("Failed to ingest memory. Check console for details.");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className={"flex flex-col gap-4 h-[80svh] pb-10"}>
            <Input
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder={"Add a title (optional)"}
                disabled={isSubmitting}
            />
            <Textarea
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Paste your text here"
                className={"flex-1"}
                disabled={isSubmitting}
            />
            <Button type="submit" className="self-end" disabled={isSubmitting}>
                {isSubmitting ? "Ingesting..." : "Submit"}
            </Button>
        </form>
    )
}