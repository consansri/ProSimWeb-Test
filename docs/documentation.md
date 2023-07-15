# Documentation #

## Performance Improvement ##

- **Example Program**

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

**Mode**

**Before**

| execution mode | time elapsed | elapsed time with performance measurement | executed instructions |
|:--------------:|:------------:|:-----------------------------------------:|----------------------:|
|   continuous   |   2577 ms    |                  3302 ms                  |                   247 |

**After**