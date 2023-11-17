# TODOS #

- Add standard calculator in editor (resolved by the global compiler at tokenization)
- Make RV32 and RV64 dependent on XLEN parameter of each Object
- RV64 change li pseudo instruction to smart li with different transcriptions (make li flexible)
- RV64 ADD Control and Status Register

# SUGAR #

- Automtatic Code Screenshots
- Make UART Memory Mapped IO accessible through console on runtime

# DONE #

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
- **(done)** allow negative hex and resolve them after constant and macro replacements
- **(done)** reanalyze line after constant insertion to resolve negations