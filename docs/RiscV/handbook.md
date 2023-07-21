# RISC-V Handbook #

## Available Syntax ##

### Directives ###
*(starting with \ means is recognized but not executed)*

#### INCLUDED ####

- **data emmitting**

  *comma seperated words [UNALIGNED or ALIGNED]*

  - **unaligned**
    ```
    .2byte          value
    .4byte          value
    .8byte          value
    ```
  - **aligned**
    ```
    .byte           value 
    .half           value
    .word           value
    .dword          value
    .asciz          value
    .string         value
    
    \.dtprelword    value
    \.dtpreldword   value
    
    \.uleb128       value
    \.sleb128       value
    ```

- **attributes**
  ```
  .attribute tag, value

  tag = {
  Tag_RISCV_arch;
  Tag_RISCV_stack_align;
  Tag_RISCV_unaligned_access;
  Tag_RISCV_priv_spec;
  Tag_RISCV_priv_spec_minor;
  Tag_RISCV_priv_spec_revision;
  }
  ```

- **options**
  ```
  .option argument
  
  argument = { 
      push; 
      pop; 
      \rvc; 
      \norvc; 
      \pic; 
      \nopic; 
      \relax; 
      \norelax; 
      \csr-check; 
      \no-csr-check; 
      \arch, +extension[version] [,...,+extension_n[version_n]];
      \arch, -extension [,...,-extension_n];
      \arch, =ISA;
  }
  ```
  
- **macros**
  ```
  .macro arg1 [,...,argn]
    [instructions which can take argn as parameters]
    ...  
  .endm
  
  ```
  

- **sections**

  On Every Position in any section a **label** can stand which **holds the address** of the **next element**

  Sections are written chronological to the memory

  - **text**
    
    ```
    .text
        # Read Only Section containing executable code
    ```
      
  - **data**
    
    ```
    .data
        # Initialized Read Write Static Variables
    ```
    
  - **rodata**
    
    ```
    .rodata
        # Initialized Read Only Const Variables      
    ```
        
    - **bss**
    ```
    .bss
        # Uninitialized Read Write Data      
    ```


#### NOT INCLUDED ####

```
.insn type, operand [,...,operand_n]
.insn insn_length, value
.insn value
```

```
\.align size-log-2
```

### Global Syntax ###

- Text Sections (standard if no section start is defined)

```
.text
    ...EQU Constant Definitions (const)...
    ...Jump Label Definitions (jlabel)...
    ...Instruction Definitions...

```

- Data Sections

```
.data
    ...Initialized Address Labels (alabel)...
        
```

- EQU Constant Definition (const) ```[clabel]: .equ [constant]```

```
.text
    constantname: .equ 0xCAFEAFFE
    
```

- Jump Label Definition (jlabel)```[jlabel]:```

```
.text
main:
    ...
    jal     loop
    ...

.loop:
    ...
    beqz    t0, end
    j       main.loop 
    
end:    
    
```

- Instruction Definition ```[instr] [parameters]```

```
.text
     lui    t0, 0xCAFEA
     addi   t0, t0, 0b111111111110
     li     t1, -4000
     sltz   t2, t1
     ...
    
```

- Initialized Address Labels ```[alabel] [type directive] [constant]```

```
.data
    lbl1: .word     "jklm"
    lbl2: .half     -34
    lbl3: .byte     0b11111011
    lbl4: .asciz    '['
    lbl5: .string   "hello world"
        
```

## Implemented Instructions ##

|     name     |                                pseudo usage                                | original params                              | pseudo params                      |
|:------------:|:--------------------------------------------------------------------------:|:---------------------------------------------|------------------------------------|
|  ```lui```   |                                     -                                      | ```rd, imm20```, ```rd, const20```           |                                    |
| ```auipc```  |                                     -                                      | ```rd, imm20```, ```rd, const20```           |                                    |
|  ```jal```   |                           ```jal [ra], jlabel```                           | ```rd, imm20```, ```rd, const20```           | ```rd, jlabel```, ```jlabel```     |
|  ```jalr```  |                         ```jalr [ra], [0](rs1)```                          | ```rd, imm12(rs1)```                         | ```rs1```                          |
| ```ecall```  |                                     -                                      |                                              |                                    |
| ```ebreak``` |                                     -                                      |                                              |                                    |
|  ```beq```   |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```bne```   |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```blt```   |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```bge```   |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```bltu```  |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```bgeu```  |                                     -                                      | ```rs1, rs2, imm12```                        | ```rs1, rs2, jlabel```             |
|  ```beqc```  |                                     -                                      | ```rs1, rs2, const12```                      |
|  ```bnec```  |                                     -                                      | ```rs1, rs2, const12```                      |
|  ```bltc```  |                                     -                                      | ```rs1, rs2, const12```                      |
|  ```bgec```  |                                     -                                      | ```rs1, rs2, const12```                      |
| ```bltuc```  |                                     -                                      | ```rs1, rs2, const12```                      |
| ```bgeuc```  |                                     -                                      | ```rs1, rs2, const12```                      |
|   ```lb```   |                                     -                                      | ```rd, imm12(rs)```                          |                                    |
|   ```lh```   |                                     -                                      | ```rd, imm12(rs)```                          |                                    |
|   ```lw```   |                                     -                                      | ```rd, imm12(rs)```                          |                                    |
|  ```lbu```   |                                     -                                      | ```rd, imm12(rs)```                          |                                    |
|  ```lhu```   |                                     -                                      | ```rd, imm12(rs)```                          |                                    |
|   ```sb```   |                                     -                                      | ```rs2, imm5(rs1)```                         |                                    |
|   ```sh```   |                                     -                                      | ```rs2, imm5(rs1)```                         |                                    |
|   ```sw```   |                                     -                                      | ```rs2, imm5(rs1)```                         |                                    |
|  ```addi```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
|  ```slti```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
| ```sltiu```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
|  ```xori```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
|  ```ori```   |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
|  ```andi```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12``` |                                    |
|  ```slli```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5``` |                                    |
|  ```srli```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5``` |                                    |
|  ```srai```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5``` |                                    |
|  ```add```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```sub```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```sll```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```slt```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```sltu```  |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```xor```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```srl```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```sra```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|   ```or```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```and```   |                                     -                                      | ```rd, rs1, rs2```                           |                                    |
|  ```nop```   |                      ```add [zero], [zero], [zero]```                      |                                              |                                    |
|   ```mv```   |                          ```addi rd, rs1, [0]```                           |                                              | ```rd, rs1```                      |
|   ```li```   | ```lui rd, %hi20(imm32/const32)```<br>```addi rd, %low12(imm32/const32)``` |                                              | ```rd, imm32```, ```rd, const32``` |
|   ```la```   |        ```lui rd, %hi20(alabel)```<br>```addi rd, %low12(alabel)```        |                                              | ```rd, alabel```                   |
|  ```not```   |                   ```xori rd, rs1, [0b111111111111] ```                    |                                              | ```rd, rs1```                      |
|  ```neg```   |                         ```sub rd, [zero], rs1```                          |                                              | ```rd, rs1```                      |
|  ```seqz```  |                          ```sltiu rd, rs1, [1]```                          |                                              | ```rd, rs1```                      |
|  ```snez```  |                         ```sltu rd, [zero], rs1```                         |                                              | ```rd, rs1```                      |
|  ```sltz```  |                         ```slt rd, rs1, [zero]```                          |                                              | ```rd, rs1```                      |
|  ```sgtz```  |                         ```slt rd, [zero], rs1```                          |                                              | ```rd, rs1```                      |
|  ```beqz```  |                       ```beq rs1, [zero], jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```bnez```  |                       ```bne rs1, [zero], jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```blez```  |                       ```bge [zero], rs1, jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```bgez```  |                       ```bge rs1, [zero], jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```bltz```  |                       ```blt rs1, [zero], jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```bgtz```  |                       ```blt [zero], rs1, jlabel```                        |                                              | ```rs1, jlabel```                  |
|  ```bgt```   |                         ```blt rs2, rs1, jlabel```                         |                                              | ```rs1, rs2, jlabel```             |
|  ```ble```   |                         ```bge rs2, rs1, jlabel```                         |                                              | ```rs1, rs2, jlabel```             |
|  ```bgtu```  |                        ```bltu rs2, rs1, jlabel```                         |                                              | ```rs1, rs2, jlabel```             |
|  ```bleu```  |                        ```bgeu rs2, rs1, jlabel```                         |                                              | ```rs1, rs2, jlabel```             |
|   ```j```    |                          ```jal [zero], jlabel```                          |                                              | ```jlabel```                       |
|   ```jr```   |                        ```jalr [zero], [0](rs1)```                         |                                              | ```rs1```                          |
|  ```ret```   |                        ```jalr [zero], [0]([ra])```                        |                                              |                                    |



## Value Input Sizes and Types ##

- Values will **automatically resize to 32 Bit, if value fits in less than 32 Bit** (32Bit, 64Bit, 128Bit).
- On Compilation every value will **first** be converted **to binary**.
- If the input value was a **decimal number** it will be upsized **signed** in **other cases** it will be resized *
  *unsigned**.

| type             |      examples       |
|:-----------------|:-------------------:|
| binary           |  ```0b10011101```   |
| hex              |     ```0x9D```      |
| decimal          |      ```-99```      |
| unsigned decimal |     ```u157```      |
| ascii            |      ```'a'```      |
| string           | ```"hello world"``` |

**Tips**

- The Value of 64 Bit and 128 Bit can only be stored as an array to fulfill that you can use the .data directive and
  initialize an array like this ```lbl1: .string 0x0123456789ABCDEF```

### Assembly Resize Behaviour ###

When will my coded value be resized if the size of expected and found values aren't matching?

- **(text-section)** on writing binary into opcodes (```32 Bit```, ```20 Bit```, ```12 Bit```, ```5 Bit```) see
  implemented instructions for further details


- **(data-section)** on writing initiated values to memory
  [```.byte -> 8 Bit```, ```.half -> 16 Bit```, ```.word -> 32 Bit```, ```.asciz -> 8 Bit```, ```.string -> 8 Bit Array (unlimited)```]

### Notes ###

- writing a ```imm32``` value **into** a smaller immediate size place (e.g. ```imm20```, ```imm12```)
  leads to **resizing** which can result in a **loss of information**.
  The written value 

