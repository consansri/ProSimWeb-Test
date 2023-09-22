package emulator.kit.common

class IConsole(val name: String) {

    private val messageArray: MutableList<Message> = mutableListOf()

    fun info(message: String) {
        messageArray.add(Message(StyleAttr.MESSAGE_TYPE_INFO, message))
    }

    fun log(message: String) {
        messageArray.add(Message(StyleAttr.MESSAGE_TYPE_LOG, message))
    }

    fun warn(message: String) {
        messageArray.add(Message(StyleAttr.MESSAGE_TYPE_WARN, message))
    }

    fun error(message: String) {
        messageArray.add(Message(StyleAttr.MESSAGE_TYPE_ERROR, message))
    }

    fun clear() {
        messageArray.clear()
    }

    fun getMessages(): List<Message> {
        return messageArray
    }

    class Message(val type: Int, val message: String) {

    }

}