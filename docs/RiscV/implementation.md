# RISC-V Implementation #

## 1 Assembler Process ##

## 2 Grammar Tree for Assembly ##

<p>
    <img src="RiscV - Grammar.drawio.svg"/>
</p>

### 2.1 Sections ###

#### 2.1.1 text ####

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

**Type**

#### 2.3.3 OPT (option) ####

#### 2.3.4 MACRO (macro) ####

### 2.4 Bss Section ###

## 3 Syntax List ##

<style>
.general {
  background: #F7F9F9;
  border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}
.pre {
  background: #F5CBA7;
border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}
.e {
  background: #ABEBC6;
  border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}
.r {
  background: #AED6F1;
  border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}
.s {
  background: #D7BDE2;
border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}
.c {
  background: #E6B0AA;
border-radius: 0.2rem;
  padding: 0.1rem;
  border: 1px solid white;
}

</style>

### general tokens ###


| general type | contains                     | 
|--------------|------------------------------|
| `constant`   | minimum 32 Bit binary values |


### pre ###


| pre type           | format                                                                                                                   | example                                                              | execution usage                                                                    |
|--------------------|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------------------------------|
| `pre_import`       | `#import "[filename]"`                                                                                                   | `#import "math.s"`                                                   | imports content of another file                                       |
| `pre_comment`      | `#[.]+`                                                                                                                  |                                                                      | nothing                                                                            |
| `pre_option`       | `.option [argument]`                                                                                                     |                                                                      | nothing                                                                            |
| `pre_attr_def`     | `.attribute [tag], [constant]`                                                                                           |                                                                      | nothing                                                                            |
| `pre_global_start` | `.global [label]`                                                                                                        | `.global main`                                                       | pc will be set to jump label address                                               |
| `pre_macro_def`    | `.macro [name] [attr1, ..., attrn]` <br/>`[instr] [parameters attributes supplied with \attr]` <br/> `...` <br/> `.endm` | `.macro jump address` <br/> `li ra, \address` <br/> `jr ra` <br/> `.endm` | defines a macro which then can be inserted                                         |
| `pre_macro_insert` | `[macroname] [attributes]`                                                                                               | `jump 0x10000000`                                                    | inserts macro with certain attributes                                              |
| `pre_equ_def`      | `.equ [equname], [constant]`                                                                                             | `.equ const1, u142`                                                  | should replace all places where the equ name is referenced with the given constant |   


### tier 1 (elements) ###


| element type  | format            | conditions                                                | examples                          | types                                                        | val and fun                       | usage |
|---------------|-------------------|-----------------------------------------------------------|-----------------------------------|--------------------------------------------------------------|-----------------------------------|-------|
| `e_instr`     | `[instrtypename]` |                                                           | `li`, `lui`, ...                  | `[InstrTypes]`     `[ParamTypes]`                            | `ParamType`, `check(e_paramcoll)` |       |
| `e_paramcoll` |                   |                                                           | `[e_param], [e_param], [e_param]` |                                                              | `getValues()`, `getLabels()`      |       |
| `e_param`     |                   |                                                           |                                   | `Offset`, `Constant`, `Register`, `SplitSymbol`, `LabelLink` |                                   |       |
| `e_label`     | `[.]+: `          |                                                           | `main:`, `.loop:`, `var1:`        |                                                              |                                   |       |
| `e_directive` | `.[.]+`           | must have type **DE** (Data Emitting) or **SectionStart** |                                   | `[DirTypes]`                                                 |                                   |       |


### tier 2 (rows) ###

1. link all labels
2. check instruction param_colls and linked labels


| row type         | element sequence                                               | conditions                                                                                           | example  | usage |
|------------------|----------------------------------------------------------------|------------------------------------------------------------------------------------------------------|----------|-------|
| `r_sectionstart` | <span class="e">`[e_directive]`</span>                         | **1.** Type of `e_directive` is `SecStart` (Section Start)                                           |          |       |
|                  |                                                                |                                                                                                      |          |       |
| *text section*   |                                                                |                                                                                                      |          |       |
| `r_jlbl_common`  | <span class="e">`[e_label]` </span>                            | **1.** labelname doesn't start with `.`                                                              | `main:`  |       |
| `r_jlbl_sub`     | <span class="e">`[e_label]` </span>                            | **1.** labelname starts with `.`                                                                     | `.loop:` |       |
| `r_instr`        | <span class="e">`[e_instr] [e_paramcoll]`</span>               | **1.** `e_paramcoll` must contain the expected values for `e_instr` use `e_instr.check(e_paramcoll)` |          |       |
|                  |                                                                |                                                                                                      |          |       |
| *data section*   |                                                                |                                                                                                      |          |       |
| `r_ilbl_rw`      | <span class="e">`[e_label] [e_directive] [e_paramcoll]`</span> | **1.** Type of `e_directive` is `DE` (Data Emitting)<br>**2.** `e_paramcoll` contains 1 constant     |          |       |
|                  |                                                                |                                                                                                      |          |       |
| *rodata section* |                                                                |                                                                                                      |          |       |
| `r_ilbl_r`       | <span class="e">`[e_label] [e_directive] [e_paramcoll]`</span> | **1.** Type of `e_directive` is `DE` (Data Emitting)<br>**2.** `e_paramcoll` contains 1 constant     |          |       |
|                  |                                                                |                                                                                                      |          |       |
| *bss section*    |                                                                |                                                                                                      |          |       |
| `r_ulbl_rw`      | <span class="e">`[e_label] [e_directive]`</span>               | **1.** Type of `e_directive` is `DE` (Data Emitting)                                                 |          |       |


### tier 3 (sections) ###


| section type | allowed rows                                                            | conditions                                           | usage |
|--------------|-------------------------------------------------------------------------|------------------------------------------------------|-------|
| `s_text`     | <span class="r">`r_sectionstart` `r_jlbl_common` `r_jlbl_sub` `r_instr` | **1.** `r_sectionstart` must be text section start   |
| `s_data`     | <span class="r">`r_sectionstart` `r_ilbl_rw`                            | **1.** `r_sectionstart` must be data section start   |
| `s_rodata`   | <span class="r">`r_sectionstart` `r_ilbl_r`                             | **1.** `r_sectionstart` must be rodata section start |
| `s_bss`      | <span class="r">`r_sectionstart` `r_ulbl_rw`                            | **1.** `r_sectionstart` must be bss section start    |


### tier 4 (container) ###


| container name | containing content of type   | usage |
|----------------|------------------------------|-------|
| `c_sections`   | <span class="s">`[sections]` |       |
| `c_pres`       | <span class="pre">`[pres]`   |       |
| `c_errors`     |                              |       |
