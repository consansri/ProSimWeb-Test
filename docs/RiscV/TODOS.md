# RISC-V TODOS #

- **(done)** Exchange StartAtLine Identification and pre code-execution with a memory highlighter of the instruction marked at the line
- **(done)** Switch Editable Memory from Binary Value Format to Hexadecimal Value Format
- **(done)** fix referenced constants so that asciis are still readable after written to memory
- **(done)** check binary mapper bit map syntax
- **(done)** rename Instruction Types (R Type, I Type, ...)
- **(done)** reinvent Grammar and Assembler for advanced assembly syntax
- **(done)** export MIF, HEX DUMP, ...
- **(done)** change alignment of RiscV memory allocation variables
- **(done)** Program Counter import bug/custom start .globl start
- **(done)** Transcript Pre/Post View, Parameter order like in instruction syntax (label offset...)
- **(done)** RegView Registername RegisterAlias
- **(done)** check import behaviour of data, rodata and bss sections
- **(done)** not only allow .global directive also allow .globl
- **(done)** transcript not binary representation instead use hex representation
- allow negative hex and resolve them after constant and macro replacements
- reanalyze line after constant insertion to resolve negations

 
## PERFORMANCE ##
  - replace DecTools because they are taking pretty long every calculation should happen in binary!
  - 