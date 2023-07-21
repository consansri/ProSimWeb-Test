# RISC-V Implementation #

## 1 Grammar Tree ##


<p>
    <img src="RiscV - Grammar.drawio.svg"/>
</p>

### 1.1 Sections ###


### 1.2 Labels ###


### 1.3 Directives ###

#### 1.3.1 DE (data emitting) ####

Each is aligned or unaligned and is containing a certain save/load syntax or a fixed size (1 - 8 Bytes)

**Type**

- aligned

  ```
  .byte
  .half
  .word
  .dword
  .asciz
  .string
  .dtprelword
  .dtpreldword
  .uleb128
  .sleb128 
  ```

- unaligned

  ```
  .2byte
  .4byte
  .8byte
  ```

#### 1.3.2 ATTR (attribute) ####


#### 1.3.3 OPT (option) ####

#### 1.3.4 MACRO (macro) ####




### 1.4 Bss Section ###
