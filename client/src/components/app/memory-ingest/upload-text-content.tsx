import {Textarea} from "@/components/ui/textarea.tsx";
import {useState} from "react";
import {Input} from "@/components/ui/input.tsx";
import {Button} from "@/components/ui/button.tsx";

export function UploadTextContent() {
    const [body, setBody] = useState("");
    const [title, setTitle] = useState("");

    const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();

        if (!body.trim()) {
            alert("Please paste some text before submitting.");
            return;
        }

        // todo()
    }

    return (
        <form onSubmit={handleSubmit} className={"flex flex-col gap-4 h-[80svh] pb-10"}>
            <Input value={title} onChange={(e) => setTitle(e.target.value)} placeholder={"Add a title (optional)"}/>
            <Textarea value={body}
                      onChange={(e) => setBody(e.target.value)}
                      placeholder="Paste your text here"
                      className={"flex-1"}/>
            <Button type="submit" className="self-end">
                Submit
            </Button>
        </form>
    )
}