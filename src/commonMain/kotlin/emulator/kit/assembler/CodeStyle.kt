package emulator.kit.assembler

enum class CodeStyle(val lightHexColor: Int, val darkHexColor: Int? = null) {

    RED(0xc94922),
    ORANGE(0xc76b29),
    YELLOW(0xc08b30),
    GREEN(0x4E9155),
    ALTGREEN(0xac9739),
    GREENPC(0x008b19),
    CYAN(0x22a2c9),
    BLUE(0x3d8fd1),
    VIOLET(0x6679cc),
    MAGENTA(0x9c637a),

    BASE0(0x202746, 0xf5f7ff),
    BASE1(0x293256, 0xdfe2f1),
    BASE2(0x5e6687, 0x979db4),
    BASE3(0x6b7394, 0x898ea4),
    BASE4(0x898ea4, 0x6b7394),
    BASE5(0x979db4, 0x5e6687),
    BASE6(0xdfe2f1, 0x293256),
    BASE7(0xf5f7ff, 0x202746),
    // Testing Colors
    CADETBLUE(0x5F9EA0), // is fine

    ALTBLUE(0x5B8FCB)
    ;

    companion object {

        val baseColor = BASE1

        val comment = BASE5

        val keyWordDir = ORANGE
        val keyWordReg = MAGENTA
        val keyWordInstr = YELLOW

        val symbol = ALTBLUE
        val label = CADETBLUE // REPLACE
        val altInt = VIOLET
        val integer = baseColor

        val string = GREEN // REPLACE
        val escape = ALTBLUE // REPLACE

        val char = altInt
        val argument = ALTGREEN
        val operator = baseColor
        val punctuation = baseColor

        val error = RED
    }

    fun getDarkElseLight(): Int = if (darkHexColor != null) darkHexColor else lightHexColor
}