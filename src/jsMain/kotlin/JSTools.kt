
import org.w3c.dom.events.EventListener
import org.w3c.dom.get
import web.dom.document
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.html.HTMLAnchorElement
import web.html.HTMLElement

object JSTools {

    val cancelWheel = { event: Event ->
        event.preventDefault()
        event.stopImmediatePropagation()
        event.stopPropagation()
    }

    fun pauseScroll(element: HTMLElement) {
        element.addEventListener(EventType("scroll"), cancelWheel)
        element.addEventListener(EventType("wheel"), cancelWheel)
    }

    fun unpauseScroll(element: HTMLElement) {
        element.removeEventListener(EventType("scroll"), cancelWheel)
        element.removeEventListener(EventType("wheel"), cancelWheel)
    }

    fun setupEventLogging() {
        val events = listOf(
            "click", "dblclick",
            "keydown", "keyup", "keypress", "focus", "blur",
            "scroll", "wheel"
        )

        val eventListener = EventListener { event: org.w3c.dom.events.Event ->
            val target = event.target as? org.w3c.dom.Element
            val targetDescription = if (target != null) {
                "Element: <${target.tagName.lowercase()} id='${target.id}' class='${target.className}'>"
            } else {
                "Unknown Element"
            }
            console.log("Event: ${event.type}, $targetDescription")
        }

        val elements = kotlinx.browser.document.body?.querySelectorAll("*") ?: return

        events.forEach { eventType ->
            for (i in 0..<elements.length) {
                elements[i]?.addEventListener(eventType, eventListener)
            }
        }

        console.log("Listening to events: ${events.joinToString(", ")}")
    }

    fun downloadFile(path: String, fileName: String) {
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = "$path$fileName"
        anchor.download = fileName
        document.body.appendChild(anchor)
        anchor.click()
        document.body.removeChild(anchor)
    }


}