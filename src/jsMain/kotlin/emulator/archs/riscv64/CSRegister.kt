package emulator.archs.riscv64

import emulator.kit.common.RegContainer
import emulator.kit.types.Variable

class CSRegister(
    address: Variable.Value,
    val privilege: Privilege,
    names: List<String>,
    aliases: List<String>,
    variable: Variable,
    description: String,
    hardwire: Boolean = false
): RegContainer.Register(address,names, aliases,variable, description =  description, privilegeID = privilege.name, hardwire = hardwire) {

    override fun set(value: Variable.Value) {
        super.set(value)
    }

    enum class Privilege{
        URW,
        URO,
        SRW,
        HRW,
        HRO,
        MRO,
        MRW,
        DRW
    }
}

