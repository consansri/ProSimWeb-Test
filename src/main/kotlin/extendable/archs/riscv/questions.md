# Questions #

## Risc V Disassembler ##

### Risc V Instruction Implementation ###

- JAL: Wird das jump offset (imm20) vorzeichenbehaftet  mit 2 oder 4 multipliziert? 
- BEQ: selbe Frage 
- BNE: selbe Frage
- ...


## Risc V Compiler ##

### Data Sections ###

- Wie werden die alloziierten Speicheradressen in den Binären OpCodes referenziert? Hier sind ja nur maximal imm20 Werte möglich. Werden hierzu im tatsächlichen AssemblerCode Ladebefehle hinzugefügt? Wenn ja welche wären das?