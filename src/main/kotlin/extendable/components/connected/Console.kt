package extendable.components.connected

class Console(val name: String) {

    private val messageArray: MutableList<Message> = mutableListOf()

    init {
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a warning!")
        error("this is an error!")
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a warning!")
        error("this is an error!")
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a warning!")
        error("this is an error!")
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a warning!")
        error("this is an error!")
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a warning!")
        error("this is an error!")
        info("this is the Console")
        log("you can log some Information here")
        log("you can even print errors and stuff")
        warn("this is a \n multi \n line \n warning!")
        error("this is an error!")
    }


    fun info(message: String) {
        messageArray.add(Message(StyleConst.MESSAGE_TYPE_INFO, message))
    }

    fun log(message: String) {
        messageArray.add(Message(StyleConst.MESSAGE_TYPE_LOG, message))
    }

    fun warn(message: String) {
        messageArray.add(Message(StyleConst.MESSAGE_TYPE_WARN, message))
    }

    fun error(message: String) {
        messageArray.add(Message(StyleConst.MESSAGE_TYPE_ERROR, message))
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