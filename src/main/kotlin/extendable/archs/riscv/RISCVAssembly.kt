package extendable.archs.riscv

import extendable.ArchConst
import extendable.Architecture
import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar
import extendable.components.connected.Transcript
import extendable.components.types.ByteValue
import tools.DebugTools

class RISCVAssembly(val binaryMapper: RISCVBinMapper) : Assembly() {


    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val rootNode = grammarTree.rootNode
        val transcript = architecture.getTranscript()
        rootNode?.let {

            for (section in rootNode.sections) {
                if (section.name == RISCVGrammar.Syntax.NODE3_SECTION_TEXT) {
                    for (collection in section.collNodes) {
                        if (collection.name == RISCVGrammar.Syntax.NODE2_INSTRDEF) {
                            for (header in ArchConst.TranscriptHeaders.values()) {


                            }
                        }
                    }
                }
            }
        }
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree) {

        val rootNode = grammarTree.rootNode
        rootNode?.let {
            val instructionMapList = mutableMapOf<Long, RISCVGrammar.T2InstrDef>()
            val binarys = mutableListOf<ByteValue.Type.Binary>()
            val labelBinAddrMap = mutableMapOf<RISCVGrammar.T1Label, String>()
            val transcriptEntrys = mutableListOf<Transcript.TranscriptEntry>()

            var instrID: Long = 0

            for (section in rootNode.sections) {
                // Resolve data sections
                when (section) {
                    is RISCVGrammar.T3TextSection -> {
                        for (entry in section.collNodes) {
                            when (entry) {
                                is RISCVGrammar.T2InstrDef -> {
                                    instructionMapList.set(instrID, entry)
                                    instrID += entry.type.memWords
                                }

                                is RISCVGrammar.T2LabelDef -> {
                                    labelBinAddrMap.set(entry.t1Label, (instrID * 4).toString(2))
                                }

                            }


                        }
                    }

                    is RISCVGrammar.T3DataSection -> {

                    }
                }

                // Resolve text sections


            }

            binaryMapper.setLabelLinks(labelBinAddrMap)

            for (instr in instructionMapList) {
                val binary = binaryMapper.getBinaryFromInstrDef(instr.value)
                if(DebugTools.RISCV_showAsmInfo){
                    console.log("Assembly.generateByteCode(): ASM-MAP [LINE ${instr.value.t1Instr.insToken.lineLoc.lineID + 1} ID ${instr.key}, ${instr.value.type.id},  ${instr.value.t1ParamColl?.paramsWithOutSplitSymbols?.joinToString(",") { it.getAllTokens().joinToString("") { it.content } }} to ${binary.joinToString { it.getRawBinaryStr() }}]")
                }
                binarys.addAll(binary)
            }

            val memory = architecture.getMemory()

            for (binaryID in binarys.indices) {
                val binary = binarys[binaryID]

                memory.save(ByteValue.Type.Hex((binaryID * 4).toString(16)), binary)
            }

        }
    }
}