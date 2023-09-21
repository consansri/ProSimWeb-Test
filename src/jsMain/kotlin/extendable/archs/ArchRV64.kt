package extendable.cisc

import extendable.Architecture
import extendable.archs.riscv64.RV64
import extendable.components.types.Variable
import kotlin.time.measureTime

class ArchRV64 : Architecture {

    constructor() : super(RV64.config, RV64.asmConfig) {

    }

    override fun exeContinuous() {
        val a = 12567890
        val b = 23

        val adec = Variable.Value.Dec(a.toString())
        val bdec = Variable.Value.Dec(b.toString())

        val abin = adec.toBin()
        val bbin = bdec.toBin()

        var cbin: Variable.Value.Bin
        val binTime = measureTime {
            cbin = (abin - bbin).toBin()
        }
        console.log("Bin: ${abin.getBinaryStr()} - ${bbin.getBinaryStr()} = ${cbin.getBinaryStr()} took ${binTime.inWholeNanoseconds}")

        var cdec: Variable.Value.Dec
        val decTime = measureTime {
            cdec = (adec - bdec).toDec()
        }
        console.log("Dec: ${adec.getDecStr()} - ${bdec.getDecStr()} = ${cdec.getDecStr()} took ${decTime.inWholeNanoseconds}")

        var c: Int
        val nativeTime = measureTime {
            c = a - b
        }
        console.log("Native: $a - $b = $c took ${nativeTime.inWholeNanoseconds}")

    }

}