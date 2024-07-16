package cengine.lang.cown.psi

import cengine.editor.completion.Completion
import cengine.editor.completion.CompletionProvider
import cengine.editor.text.TextModel
import cengine.psi.core.PsiFile

class CownCompleter : CompletionProvider {

    private val keywords = listOf(
        "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return",
        "super", "this", "throw", "true", "try", "typealias", "val", "var", "when", "while",
        "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally",
        "get", "import", "init", "param", "property", "receiver", "set", "setparam",
        "where", "actual", "abstract", "annotation", "companion", "const", "crossinline",
        "data", "enum", "expect", "external", "final", "infix", "inline", "inner", "internal",
        "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected",
        "public", "reified", "sealed", "suspend", "tailrec", "vararg"
    )

    override fun getCompletions(textModel: TextModel, offset: Int, prefix: String, psiFile: PsiFile?): List<Completion> {
        return keywords
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { keyword ->
                Completion(
                    keyword,
                    keyword.removePrefix(prefix),
                )
            }
    }
}