package emulator.kit.register

data class RegDescr(
    val id: Int,
    val names: List<String>,
    val cc: Calle? = null,
    val descr: String? = null
) {

    enum class Calle {
        R,
        E
    }

}