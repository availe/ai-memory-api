import {type ChangeEvent, type FormEvent, useState} from "react";
import {Button} from "@/components/ui/button.tsx";
import {Input} from "@/components/ui/input.tsx";
import {ingestFile} from "@/lib/api.ts";

export function UploadFilesContent() {
    const [files, setFiles] = useState<File[]>([])
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
        const fileList = event.currentTarget.files ?? []
        const incomingFiles = Array.from(fileList)
        setFiles((previousFiles) => mergeUniqueFiles(previousFiles, incomingFiles))
    }

    const handleRemoveFile = (fileToRemove: File) => {
        const removeKey = computeFileKey(fileToRemove)
        setFiles(previousFiles =>
            previousFiles.filter(file => computeFileKey(file) !== removeKey)
        )
    }

    const handleRemoveAllFiles = () => setFiles([])

    const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        if (files.length === 0) {
            alert("Please select at least one file to upload.")
            return
        }

        try {
            setIsSubmitting(true);
            for (const file of files) {
                await ingestFile(file);
            }
            alert(`Successfully ingested ${files.length} file(s).`);
            setFiles([]);
        } catch (error) {
            console.error("File ingestion failed:", error);
            alert("Failed to ingest some files. Check console for details.");
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4 h-[80svh] pb-10">
            <Input
                type="file"
                multiple
                accept={ACCEPTED_FILE_TYPES.join(",")}
                onChange={handleFileChange}
                className="cursor-pointer"
                disabled={isSubmitting}
            />

            {files.length > 0 && (
                <ul className="list-disc list-inside text-sm text-muted-foreground space-y-1">
                    {files.map((file) => (
                        <li key={computeFileKey(file)} className="flex justify-between items-center">
                            <span className="truncate">{file.name}</span>
                            <Button
                                type="button"
                                variant="ghost"
                                size="sm"
                                onClick={() => handleRemoveFile(file)}
                                className="text-red-500 hover:text-red-700"
                                disabled={isSubmitting}
                            >
                                ✕
                            </Button>
                        </li>
                    ))}
                </ul>
            )}

            <div className="mt-auto flex gap-2 self-end">
                <Button type="button" variant="outline" onClick={handleRemoveAllFiles}
                        disabled={isSubmitting || files.length === 0}>
                    Clear
                </Button>
                <Button type="submit" disabled={isSubmitting || files.length === 0}>
                    {isSubmitting ? "Uploading..." : "Upload"}
                </Button>
            </div>
        </form>
    )
}

// helpers

const ACCEPTED_FILE_TYPES = [
    "text/plain",
    "application/pdf",
    "application/json",
    "text/csv",
    "image/png",
    "image/jpeg",
] as const

export type AcceptedFileType = (typeof ACCEPTED_FILE_TYPES)[number]

type FileKey = `${string}:${number}:${number}`

const computeFileKey = (file: File): FileKey =>
    `${file.name}:${file.size}:${file.lastModified}`

function mergeUniqueFiles(
    existingFiles: readonly File[],
    incomingFiles: readonly File[]
): File[] {
    const filesByCompositeKeyMap = new Map<FileKey, File>(
        existingFiles.map((existingFile) => [computeFileKey(existingFile), existingFile] as const)
    )
    for (const incomingFile of incomingFiles) {
        filesByCompositeKeyMap.set(computeFileKey(incomingFile), incomingFile)
    }
    return [...filesByCompositeKeyMap.values()]
}