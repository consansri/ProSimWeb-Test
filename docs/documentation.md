# Documentation #

## Design ##

### Rules ###

- > **CSSRule:** Apply Variable Elements such as (Colors) in kotlin emotion and every other css style in a css file!

### z-Axis ###

| INDEX | ITEM                    |
|:-----:|:------------------------|
|  100  | **Scroll Bars**         |
|  21   | **Menu Nav Dropdown**   |
|  20   | **Menu Mobile Nav**     |
|  10   | **Memory Window**       |
|   2   | **Editor Area**         |
|   1   | **Editor Highlighting** |
|   0   | **normal**              |

## Performance Improvement ##

### RiscV Program Execution ###

**Example**

```riscv
li t0, 0xCAFEAFFE
main:        
    li a1, 0x100
    li a2, 8
    jal init
    li a3, 4
    jal lru
    li a3, 7
    jal lru
    li a3, 2
    jal lru  
    j .end_prog

init:        
    mv t0, a2
.loop:   
    beqz t0, .end
    addi t0, t0, -1
    add t1, a1, t0
    sb t0, 0(t1)
    j .loop
.end:    
    ret

# a0 ret_val
# a1 *A
# a2 N
# a3 element
# t0 i
# t1 j
lru:           
    li a0, 0   
    addi t0, a2, -1    
    
.oloop:     
    bltz  t0, .end_oloop
    add  t2, a1, t0   
    lb   t2, 0(t2)     
    bne  t2, a3, .end_if    
    addi t1, t0, -1   
    
.iloop:     
    bltz  t1, .end_iloop
    add  t2, a1, t1   
    lb   t3, 0(t2)    
    addi t4, t2, 1  
    sb   t3, 0(t4)
    addi t1, t1, -1  
    j  .iloop
    
.end_iloop:
    sb   a3, 0(a1)
    addi t4, a2, -1  
    add  t4, a1, t4
    lb   a0, 0(t4)    
    j  .end_oloop
    
.end_if:    
    addi t0, t0, -1
    j .oloop
    
.end_oloop:
    ret
    
.end_prog:
    nop
```

**Before**

| execution mode | time elapsed | elapsed time with performance measurement | executed instructions |
|:--------------:|:------------:|:-----------------------------------------:|----------------------:|
|   continuous   |   2577 ms    |                  3302 ms                  |                   247 |
|  single step   |   2 - 5 ms   |                     -                     |                   lui |
|  single step   |  9 - 22 ms   |                     -                     |                  addi |
|  single step   |   0 - 1 ms   |                     -                     |                   jal |
|  single step   |    13 ms     |                     -                     |                   beq |
|  single step   |  16 - 32 ms  |                     -                     |                   add |
|  single step   |  5 - 14 ms   |                     -                     |                    lb |
|  single step   |  4 - 20 ms   |                     -                     |                    sb |
|  single step   |    13 ms     |                     -                     |                   beq |
|  single step   |     1 ms     |                     -                     |                  jalr |
|  single step   |   5 - 8 ms   |                     -                     |                   blt |
|  single step   |  18 - 19 ms  |                     -                     |                   bne |

Measurement continuous
<p align="center">
    <img width="90%" alt="[measurement-before]" src="Performance Traces/measurement20230715-continuousexample-before.png"/>
</p>

Measurement Single Instruction add
> **Test Program**
> ```riscv
> main:
>         jal prereginit
>         add a2, a1, a0	
>         j end
>
> prereginit:
>         lui a0, 0xCAFEAFFE
>         lui a1, 0xAffECAFE
>         ret
> end:
> ```

<p align="center">
    <img width="90%" alt="[measurement-before]" src="Performance Traces/measurement20230715-addexample-before.png"/>
</p>

**Issues and Solutions**

> **Issue:** Short Site Blocking while executing anything\
> **Solution:** Async Execution

> **Issue:** Execution to slow\
> **Solutions:**
>
> | id | improvement    | solution                                                                               | continuous speed |
> |----|----------------|----------------------------------------------------------------------------------------|------------------|
> | 0  | 3 times faster | Exchanged Binary Weight calculation `pow(2, index))` with look up table!               | 1015 ms          |
> | 1  | 3 times faster | Removed Regex inititalizations in all MutVal functions defined them somewhere globally | 360 ms           |
>


**After**

| execution mode | time elapsed | elapsed time with performance measurement | executed instructions |
|:--------------:|:------------:|:-----------------------------------------:|----------------------:|
|   continuous   |   1015 ms    |                  1302 ms                  |                   247 |
|  single step   |   2 - 5 ms   |                     -                     |                   lui |
|  single step   |  9 - 22 ms   |                     -                     |                  addi |
|  single step   |   0 - 1 ms   |                     -                     |                   jal |
|  single step   |    13 ms     |                     -                     |                   beq |
|  single step   |  16 - 32 ms  |                     -                     |                   add |
|  single step   |  5 - 14 ms   |                     -                     |                    lb |
|  single step   |  4 - 20 ms   |                     -                     |                    sb |
|  single step   |    13 ms     |                     -                     |                   beq |
|  single step   |     1 ms     |                     -                     |                  jalr |
|  single step   |   5 - 8 ms   |                     -                     |                   blt |
|  single step   |  18 - 19 ms  |                     -                     |                   bne |

### RegisterEdit ###

**Example**

- **32 Bit**
    - Hex: 0x00000000 to 0xFFFFFFFF
    - Bin: 0b00000000000000000000000000000000 to 0b11111111111111111111111111111111
    - UDec: 0 to 4294967295
    - Dec: -2147483648 to 2147483647

**Before**

| Type | elapsed time |
|------|-------------:|
| Hex  |         3 ms |
| Bin  |         3 ms |
| UDec |        12 ms |
| Dec  |        15 ms |

**Issues and Solutions**

> **Issue:** Short Site Blocking while onBlur is calculating\
> **Solution:** ASync onBlur Event

> **Issue:** Decimal Calculations to long!\
> **Solution:** TODO


**After**

### Register Type Switch ###

**Example**

- **Initial:** Switching Type of 32 Registers filled with zeros

**Before**

| From | To   | elapsed time |
|------|------|-------------:|
| Hex  | UDec |       449 ms |
| UDec | Dec  |       425 ms |
| Dec  | Bin  |       344 ms |
| Bin  | Hex  |       339 ms |

**Issues and Solutions**

> **Issue:** Short Site Blocking while RegType is changed\
> **Solution:** ASync calculation of new types

> **Issue:** Decimal Calculations to long\
> **Solution:** TODO

**After**
