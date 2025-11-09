import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs.tsx";
import {useEffect, useState} from "react";
import {UploadFilesContent} from "@/components/app/memory-ingest/upload-files-content.tsx";
import {UploadTextContent} from "@/components/app/memory-ingest/upload-text-content.tsx";

export function MemoryIngestPanel() {
    const [selectedTab, setSelectedTab] = useState<Tab>(TAB_VALUES.text)

    useEffect(() => {
        const abortController = new AbortController()

        const handleGlobalKeydownForTabs = (event: KeyboardEvent) => {
            const currentlyFocusedElement = document.activeElement
            if (isFocusInEditableContext(currentlyFocusedElement)) return
            if (isHotkeyCode(event.code)) {
                setSelectedTab(HOTKEY_CODE_TO_TAB[event.code])
            }
        }

        window.addEventListener("keydown", handleGlobalKeydownForTabs, {signal: abortController.signal})
        return () => abortController.abort()
    }, [])

    return (
        <Tabs
            value={selectedTab} onValueChange={(nextValue) => setSelectedTab(nextValue as Tab)}
            className={"w-full h-full"}
        >
            <TabsList>
                <TabsTrigger value={TAB_VALUES.text}>Text</TabsTrigger>
                <TabsTrigger value={TAB_VALUES.files}>Files</TabsTrigger>
            </TabsList>
            <TabsContent value={TAB_VALUES.text}>
                <UploadTextContent/>
            </TabsContent>
            <TabsContent value={TAB_VALUES.files}>
                <UploadFilesContent/>
            </TabsContent>
        </Tabs>
    )
}

// helpers

const TAB_VALUES = {
    files: "files",
    text: "text",
} as const

type Tab = typeof TAB_VALUES[keyof typeof TAB_VALUES]

function isFocusInEditableContext(element: Element | null): boolean {
    if (!element) return false
    if (element instanceof HTMLInputElement) return true
    if (element instanceof HTMLTextAreaElement) return true
    if (element instanceof HTMLSelectElement) return true
    return element instanceof HTMLElement && element.isContentEditable
}

const HOTKEY_CODE_TO_TAB = {
    Digit1: TAB_VALUES.text,
    Numpad1: TAB_VALUES.text,
    Digit2: TAB_VALUES.files,
    Numpad2: TAB_VALUES.files,
} as const

type HotkeyCode = keyof typeof HOTKEY_CODE_TO_TAB

function isHotkeyCode(code: string): code is HotkeyCode {
    return Object.prototype.hasOwnProperty.call(HOTKEY_CODE_TO_TAB, code)
}