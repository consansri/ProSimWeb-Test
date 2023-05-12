package tools

object HTMLTools {
    fun encodeBeforeHTML(value: String): String {
        val builder = StringBuilder()

        for (c in value) {
            when (c) {
                '<' -> builder.append("&lt;")
                '>' -> builder.append("&gt;")
                else -> builder.append(c)
            }
        }

        return builder.toString()
    }

    fun encodeAfterHTML(value: String): String {
        val builder = StringBuilder()

        for (c in value) {
            when (c) {
                '&' -> builder.append("&amp;")
                '"' -> builder.append("&quot;")
                else -> builder.append(c)
            }
        }

        return builder.toString()
    }




}