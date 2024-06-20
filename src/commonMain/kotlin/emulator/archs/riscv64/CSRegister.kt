package emulator.archs.riscv64

import emulator.kit.common.RegContainer
import emulator.core.*

class CSRegister(
    address: Value,
    val privilege: Privilege,
    names: List<String>,
    aliases: List<String>,
    variable: Variable,
    description: String,
    neededByFeatureID: List<Int>? = null,
    hardwire: Boolean = false
) : RegContainer.Register(address, names, aliases, variable, needsFeatureID = neededByFeatureID, description = description, privilegeID = privilege.name, hardwire = hardwire) {

    enum class Privilege {
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

