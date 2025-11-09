import {AppNavigationMenu} from "@/components/app/navigation-menu/app-navigation-menu.tsx";
import {FileUploadMultiFileDropzone} from "@/components/app/navigation-menu/file-upload-multi-file-dropzone.tsx";

function App() {

    return (
        <div className="min-h-screen w-full flex flex-col">
            <header
                className="sticky top-0 z-50 w-full border-b bg-background/80 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <AppNavigationMenu/>
            </header>

            <main className="flex-1">
                <FileUploadMultiFileDropzone/>
            </main>
        </div>
    )
}

export default App
