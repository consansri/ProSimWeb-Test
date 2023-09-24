import emulator.kit.types.Variable
import react.create
import react.dom.client.createRoot
import web.dom.*
import tools.DebugTools

fun main() {
    if (DebugTools.REACT_showUpdateInfo) {
        console.log("Init Client")
    }
    val root = document.getElementById("root") ?: error("Couldn't find root container!")

    val app = App.create()

    document.title = "${Constants.name} ${Constants.version}"
    createRoot(root).render(app)

    /*val bin7 = Variable.Value.Bin("00000111", Variable.Size.Bit8())
    val decNeg5 = Variable.Value.Dec("-5", Variable.Size.Bit8()) // = 0b11111011

    // Binäre Operationen
    val binsum = bin7 + decNeg5   // 0b00000010, 2
    val binsub = bin7 - decNeg5   // 0b00001100, 12
    val binprod = bin7 * decNeg5  // 0b11011101, -35
    val bindiv = bin7 / decNeg5   // 0b00000000, 0

    val binNeg5 = Variable.Value.Bin("0b11111011", Variable.Size.Bit8())
    val dec7 = Variable.Value.Dec("7", Variable.Size.Bit8()) //  = 0b00000111

    // Dezimale Operationen
    val decsum = dec7 + binNeg5   // 0b00000010, 2
    val decsub = dec7 - binNeg5   // 0b00001100, 12
    val decprod = dec7 * binNeg5  // 0b11011101, -35
    val decdiv = dec7 / binNeg5   // 0b00000000, -1


    console.log("binsum: ${binsum.toBin()},      decsum: ${decsum.toDec()}")
    console.log("binsub: ${binsub.toBin()},      decsub: ${decsub.toDec()}")
    console.log("binprod: ${binprod.toBin()},    decprod: ${decprod.toDec()}")
    console.log("bindiv: ${bindiv.toBin()},      decdiv: ${decdiv.toDec()}")*/

}


