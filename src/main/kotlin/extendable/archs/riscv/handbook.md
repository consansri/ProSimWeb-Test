# RiscV Handbook #

## Implemented Instructions ##

|     name     |                                pseudo usage                                | original params                                  | pseudo params                        |
|:------------:|:--------------------------------------------------------------------------:|:-------------------------------------------------|--------------------------------------|
|  ```lui```   |                                     -                                      | ```rd, imm20```, ```rd, const20```             |                                      |
| ```auipc```  |                                     -                                      | ```rd, imm20```, ```rd, const20```             |                                      |
|  ```jal```   |                           ```jal [ra], jlabel```                           | ```rd, imm20```, ```rd, const20```             | ```rd, jlabel```, ```jlabel```     |
|  ```jalr```  |                         ```jalr [ra], [0](rs1)```                          | ```rd, imm12(rs1)```                             | ```rs1```                            |
| ```ecall```  |                                     -                                      |                                                  |                                      |
| ```ebreak``` |                                     -                                      |                                                  |                                      |
|  ```beq```   |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|  ```bne```   |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|  ```blt```   |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|  ```bge```   |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|  ```bltu```  |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|  ```bgeu```  |                                     -                                      | ```rs1, rs2, imm12```, ```rs1, rs2, const12``` | ```rs1, rs2, jlabel```               |
|   ```lb```   |                                     -                                      | ```rd, imm12(rs)```                              |                                      |
|   ```lh```   |                                     -                                      | ```rd, imm12(rs)```                              |                                      |
|   ```lw```   |                                     -                                      | ```rd, imm12(rs)```                              |                                      |
|  ```lbu```   |                                     -                                      | ```rd, imm12(rs)```                              |                                      |
|  ```lhu```   |                                     -                                      | ```rd, imm12(rs)```                              |                                      |
|   ```sb```   |                                     -                                      | ```rs2, imm5(rs1)```                             |                                      |
|   ```sh```   |                                     -                                      | ```rs2, imm5(rs1)```                             |                                      |
|   ```sw```   |                                     -                                      | ```rs2, imm5(rs1)```                             |                                      |
|  ```addi```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
|  ```slti```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
| ```sltiu```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
|  ```xori```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
|  ```ori```   |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
|  ```andi```  |                                     -                                      | ```rd, rs1, imm12```, ```rd, rs1, const12```   |                                      |
|  ```slli```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5```   |                                      |
|  ```srli```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5```   |                                      |
|  ```srai```  |                                     -                                      | ```rd, rs1, shamt5```, ```rd, rs1, const5```   |                                      |
|  ```add```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```sub```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```sll```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```slt```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```sltu```  |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```xor```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```srl```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```sra```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|   ```or```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```and```   |                                     -                                      | ```rd, rs1, rs2```                               |                                      |
|  ```nop```   |                      ```add [zero], [zero], [zero]```                      |                                                  |                                      |
|   ```mv```   |                          ```addi rd, rs1, [0]```                           |                                                  | ```rd, rs1```                        |
|   ```li```   | ```lui rd, %hi20(imm32/const32)```<br>```addi rd, %low12(imm32/const32)``` |                                                  | ```rd, imm32```, ```rd, const32``` |
|   ```la```   |        ```lui rd, %hi20(alabel)```<br>```addi rd, %low12(alabel)```        |                                                  | ```rd, alabel```                     |
|  ```not```   |                   ```xori rd, rs1, [0b111111111111] ```                    |                                                  | ```rd, rs1```                        |
|  ```neg```   |                         ```sub rd, [zero], rs1```                          |                                                  | ```rd, rs1```                        |
|  ```seqz```  |                          ```sltiu rd, rs1, [1]```                          |                                                  | ```rd, rs1```                        |
|  ```snez```  |                         ```sltu rd, [zero], rs1```                         |                                                  | ```rd, rs1```                        |
|  ```sltz```  |                         ```slt rd, rs1, [zero]```                          |                                                  | ```rd, rs1```                        |
|  ```sgtz```  |                         ```slt rd, [zero], rs1```                          |                                                  | ```rd, rs1```                        |
|  ```beqz```  |                       ```beq rs1, [zero], jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```bnez```  |                       ```bne rs1, [zero], jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```blez```  |                       ```bge [zero], rs1, jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```bgez```  |                       ```bge rs1, [zero], jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```bltz```  |                       ```blt rs1, [zero], jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```bgtz```  |                       ```blt [zero], rs1, jlabel```                        |                                                  | ```rs1, jlabel```                    |
|  ```bgt```   |                         ```blt rs2, rs1, jlabel```                         |                                                  | ```rs1, rs2, jlabel```               |
|  ```ble```   |                         ```bge rs2, rs1, jlabel```                         |                                                  | ```rs1, rs2, jlabel```               |
|  ```bgtu```  |                        ```bltu rs2, rs1, jlabel```                         |                                                  | ```rs1, rs2, jlabel```               |
|  ```bleu```  |                        ```bgeu rs2, rs1, jlabel```                         |                                                  | ```rs1, rs2, jlabel```               |
|   ```j```    |                          ```jal [zero], jlabel```                          |                                                  | ```jlabel```                         |
|   ```jr```   |                        ```jalr [zero], [0](rs1)```                         |                                                  | ```rs1```                            |
|  ```ret```   |                        ```jalr [zero], [0]([ra])```                        |                                                  |                                      |
