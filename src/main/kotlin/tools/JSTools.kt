package tools

import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLTextAreaElement

object JSTools {

    fun getLineHeight(element: HTMLTextAreaElement): Int {

        console.log("getLineHeight(${element?.let { "[not null]" }})")
        val temp = document.createElement(element.nodeName) as HTMLTextAreaElement
        temp.setAttribute(
            "style",
            "margin:0; padding:0; "+
            "font-family:${element.style.fontFamily ?: "inherit"};" +
            "font-size:${element.style.fontSize ?: "inherit"};"
        )
        temp.innerHTML ="A"

        element.parentNode?.appendChild(temp)
        val ret = temp.clientHeight
        temp.parentNode?.removeChild(temp)

        console.log("getLineHeight(): $ret")

        return ret
    }

}