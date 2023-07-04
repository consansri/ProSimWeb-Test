package extendable.archs.riscv

import extendable.ArchConst
import extendable.Architecture
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.Transcript

class RISCVCompiler : Compiler() {


    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        val rootNode = grammarTree.rootNode
        val transcript = architecture.getTranscript()
        rootNode?.let {

            for (section in rootNode.sections) {
                if (section.name == RISCVGrammar.Syntax.NODE3_SECTION_TEXT) {
                    for(collection in section.collNodes){
                        if(collection.name == RISCVGrammar.Syntax.NODE2_INSTRDEF){


                            for(header in ArchConst.TranscriptHeaders.values()){




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


        }
    }
}