package prosim.vfs

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

class FileWatcher(private val vfs: VFileSystem) {
    private val watchService = FileSystems.getDefault().newWatchService()

    fun watchDirectory(dir: Path){
        dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_MODIFY)
    }

    fun startWatching(){
        Thread{
            while(true){
                val key = watchService.take()
                for(event in key.pollEvents()){
                    when(event.kind()){
                        StandardWatchEventKinds.ENTRY_CREATE -> {
                            val path = (event.context() as Path).toString()
                            vfs.createFile(path)
                        }
                        StandardWatchEventKinds.ENTRY_DELETE -> {
                            val path = (event.context() as Path).toString()
                            vfs.deleteFile(path)
                        }
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            val path = (event.context() as Path).toString()
                            vfs.findFile(path)?.let { vfs.notifyFileChanged(it) }
                        }
                    }
                }
                key.reset()
            }
        }.start()
    }


}