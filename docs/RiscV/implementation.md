# RISC-V Implementation #

## 1 Assembler Process ##

## 2 Grammar Tree for Assembly ##

<p>
    <img src="RiscV - Grammar.drawio.svg"/>
</p>

### 2.1 Sections ###

#### 2.1.1 text ####

Possible Line Syntax

#### 2.1.2 data ####

#### 2.1.3 rodata ####

#### 2.1.4 bss ####

### 2.2 Labels ###

### 2.3 Directives ###

#### 2.3.1 DE (data emitting) ####

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

#### 2.3.2 ATTR (attribute) ####

#### 2.3.3 OPT (option) ####

#### 2.3.4 MACRO (macro) ####

### 2.4 Bss Section ###

## 3 Syntax List ##

### pre ###

| syntax name | format | example | execution usage | usage |
|-------------|--------|---------|-----------------|-------|

### tier 1 (elements) ###

| syntax name | row | example | execution usage | usage |
|-------------|-----|---------|-----------------|-------|

### tier 2 (rows) ###

| type            | element sequence     | example  | usage |
|-----------------|----------------------|----------|-------|
| `r_jlbl_common` | `[labelname]:`       | `main:`  |
| `r_jlbl_sub`    | `.[sublabelname]:`   | `.loop:` |
| `r_instr`       | `[name] [paramcoll]` |          |


### tier 3 (sections) ###

| section name | row sequence | example | usage |
|--------------|--------------|---------|-------|
| `s_text`     |              |         |
| `s_data`     |              |         |
| `s_rodata`   |              |         |
| `s_bss`      |              |         |
