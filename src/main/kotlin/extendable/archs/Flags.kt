package extendable.archs

open class Flags {
    private val errorFlag = "red"
    private val greenFlag = "green"
    private val blueFlag = "blue"
    private val cyanFlag = "cyan"
    private val orangeFlag = "orange"
    private val hexFlag = "hex"
    private val decFlag = "decimal"

    protected fun getErrorFlag(): String {
        return errorFlag
    }

    protected fun getGreenFlag(): String {
        return greenFlag
    }

    protected fun getBlueFlag(): String {
        return blueFlag
    }

    protected fun getCyanFlag(): String {
        return cyanFlag
    }

    protected fun getOrangeFlag(): String {
        return orangeFlag
    }

    protected fun getHexFlag(): String {
        return hexFlag
    }

    protected fun getDecFlag(): String {
        return decFlag
    }

}