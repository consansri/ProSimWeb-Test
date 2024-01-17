package emulator.kit.common


/**
 * The usage of [IConsole] is mainly to hold messages from architecture components on runtime. It's used to resolve assembler errors and warnings.
 */
class IConsole(val name: String) {

    private val messageArray: MutableList<Message> = mutableListOf()

    fun info(message: String) {
        messageArray.add(Message(MSGType.INFO, message))
    }

    fun log(message: String) {
        messageArray.add(Message(MSGType.LOG, message))
    }

    fun warn(message: String) {
        messageArray.add(Message(MSGType.WARNING, message))
    }

    fun error(message: String) {
        messageArray.add(Message(MSGType.ERROR, message))
    }

    fun compilerInfo(message: String) {
        messageArray.add(Message(MSGType.INFO, "o.O $message"))
    }

    fun exeInfo(message: String){
        messageArray.add(Message(MSGType.INFO, "> $message"))
    }

    fun missingFeature(message: String){
        messageArray.add(Message(MSGType.WARNING, "feature missing: $message"))
    }

    fun clear() {
        messageArray.clear()
    }

    fun getMessages(): List<Message> {
        return messageArray
    }

    data class Message(val type: MSGType, val message: String)

    enum class MSGType {
        LOG,
        INFO,
        WARNING,
        ERROR
    }

}