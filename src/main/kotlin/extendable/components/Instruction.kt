package extendable.components

class Instruction {

    val name: String
    val extensionCount: Int

    constructor(name: String, extensionCount: Int) {
        this.name = name
        this.extensionCount = extensionCount

    }

}