package extendable.archs.riscv

import extendable.components.*
import extendable.components.connected.*
import extendable.components.types.*

object RISCV {

    // PROCESSOR
    val MEM_INIT: String = "0"

    val REG_INIT: String = "0"
    val REG_SIZE = MutVal.Size.Bit32()
    val REG_ADDRESS_SIZE = MutVal.Size.Bit8()

    enum class TS_COMPILED_HEADERS {
        Address,
        Label,
        Instruction,
        Parameters
    }

    enum class TS_DISASSEMBLED_HEADERS {
        Address,
        Label,
        Instruction,
        Parameters
    }

    // Assembler CONFIG
    val asmConfig = AsmConfig(
        RISCVGrammar(),
        RISCVAssembly(RISCVBinMapper(), MutVal.Value.Hex("00001000", MutVal.Size.Bit32()),MutVal.Value.Hex("00002000", MutVal.Size.Bit32()),MutVal.Value.Hex("00003000", MutVal.Size.Bit32()))
    )

    val riscVDocs = Docs(
        Docs.HtmlFile(
            "Risc-V Handbook",
            """
<h1 id="risc-v-handbook">RISC-V Handbook</h1>
<h2 id="available-syntax">Available Syntax</h2>
<h3 id="directives">Directives</h3>
<ul>
    <li>global</li>
    <p>define start of pc</p>
    <pre><code>.global [jlabelname]</code></pre>
    <li>data emitting unaligned</li>
        <pre><code><span class="hljs-number">.2</span><span class="hljs-keyword">byte</span>          <span
                class="hljs-keyword">value</span>
                <span class="hljs-number">.4</span><span class="hljs-keyword">byte</span>          <span
                    class="hljs-keyword">value</span>
                <span class="hljs-number">.8</span><span class="hljs-keyword">byte</span>          <span
                    class="hljs-keyword">value</span>
                </code></pre>

    <li>data emitting aligned</li>
        <pre><code><span class="hljs-selector-class">.byte</span>           value
                <span class="hljs-selector-class">.half</span>           value
                <span class="hljs-selector-class">.word</span>           value
                <span class="hljs-selector-class">.dword</span>          value
                <span class="hljs-selector-class">.asciz</span>          value
                <span class="hljs-selector-class">.string</span>         value

                \<span class="hljs-selector-class">.dtprelword</span>    value
                \<span class="hljs-selector-class">.dtpreldword</span>   value

                \<span class="hljs-selector-class">.uleb128</span>       value
                \<span class="hljs-selector-class">.sleb128</span>       value
                </code></pre>

    <li>attributes</li>
        <pre><code>.attribute tag, value

                tag = {
                Tag_RISCV_arch<span class="hljs-comment">;</span>
                Tag_RISCV_stack_align<span class="hljs-comment">;</span>
                Tag_RISCV_unaligned_access<span class="hljs-comment">;</span>
                Tag_RISCV_priv_spec<span class="hljs-comment">;</span>
                Tag_RISCV_priv_spec_minor<span class="hljs-comment">;</span>
                Tag_RISCV_priv_spec_revision<span class="hljs-comment">;</span>
                }
                </code></pre>
    <li>options</li>
        <pre><code>.option argument

                argument = {
                    push<span class="hljs-comment">; </span>
                    pop<span class="hljs-comment">; </span>
                    \rvc<span class="hljs-comment">; </span>
                    \<span class="hljs-keyword">norvc; </span>
                    \pic<span class="hljs-comment">; </span>
                    \<span class="hljs-keyword">nopic; </span>
                    \relax<span class="hljs-comment">; </span>
                    \<span class="hljs-keyword">norelax; </span>
                    \csr-check<span class="hljs-comment">; </span>
                    \no-csr-check<span class="hljs-comment">; </span>
                    \arch, +<span class="hljs-keyword">extension[version] </span>[,...,+<span class="hljs-keyword">extension_n[version_n]];
                </span>    \arch, -<span class="hljs-keyword">extension </span>[,...,-<span class="hljs-keyword">extension_n];
                </span>    \arch, =ISA<span class="hljs-comment">;</span>
                }
                </code></pre>
    <li>macros</li>
        <pre><code><span class="hljs-selector-class">.macro</span> <span class="hljs-selector-tag">arg1</span> <span
                class="hljs-selector-attr">[,...,argn]</span>
                  <span class="hljs-selector-attr">[instructions which can take argn as parameters]</span>
                  ...
                <span class="hljs-selector-class">.endm</span>
                </code></pre>

    <li>sections</li>
        <p>On Every Position in any section a <strong>label</strong> can stand which <strong>holds the address</strong> of the <strong>next element</strong><br>Sections are written chronological to the memory</p>
        <p><strong>text</strong></p>
        <pre><code>.<span class="hljs-built_in">text</span>
                    <span class="hljs-comment"># Read Only Section containing executable code</span>
                </code></pre>

        <p><strong>data</strong></p>
        <pre><code>.data
                    # Initialized <span class="hljs-keyword">Read</span> <span class="hljs-keyword">Write</span> <span
                    class="hljs-keyword">Static</span> Variables
                </code></pre>

        <p><strong>rodata</strong></p>
        <pre><code>.rodata
                    # Initialized <span class="hljs-keyword">Read</span> Only <span class="hljs-keyword">Const</span> Variables
                </code></pre>

        <strong>bss</strong>
        <pre><code>.bss
                # Uninitialized <span class="hljs-keyword">Read</span> <span class="hljs-keyword">Write</span> Data
                </code></pre>

</ul>
<h3 id="global-syntax">Global Syntax</h3>
<ul>
    <li>Text Sections (standard if no section start is defined)</li>

    <pre><code><span class="hljs-selector-class">.text</span>
                    ..<span class="hljs-selector-class">.EQU</span> Constant Definitions (const)...
                    ..<span class="hljs-selector-class">.Jump</span> Label Definitions (jlabel)...
                    ..<span class="hljs-selector-class">.Instruction</span> Definitions...
                </code></pre>

    <li>Data Sections</li>

    <pre><code><span class="hljs-selector-class">.data</span>
                    ..<span class="hljs-selector-class">.Initialized</span> Address Labels (alabel)...
                </code></pre>

    <li>RoData Sections</li>

    <pre><code><span class="hljs-selector-class">.rodata</span>
                    ..<span class="hljs-selector-class">.Initialized</span> Read-Only Address Labels (alabel)...
                </code></pre>

    <li>BSS Sections</li>

    <pre><code><span class="hljs-selector-class">.bss</span>
                    ..<span class="hljs-selector-class">.Uninitialized</span> Address Labels (alabel)...
                </code></pre>

    <li>Macro Definition</li>

    <pre><code># <span class="hljs-selector-tag">definition</span>
                <span class="hljs-selector-class">.macro</span> <span
                class="hljs-selector-attr">[macroname]</span> <span
                class="hljs-selector-attr">[attr1, ..., attrn]</span>
                    <span class="hljs-selector-attr">[instructions with \attrn links]</span>
                    ...
                <span class="hljs-selector-class">.endm</span>

                # <span class="hljs-selector-tag">replacement</span>
                <span class="hljs-selector-class">.text</span>
                    <span class="hljs-selector-attr">[macroname]</span> <span
                class="hljs-selector-attr">[attributes]</span>
                </code></pre>

    <li>EQU Constant Definition (constant)</li>

    <pre><code>    <span class="hljs-selector-class">.equ</span> [constantname], <span class="hljs-number">0</span>xCAFEAFFE

                <span class="hljs-selector-class">.text</span>
                    <span class="hljs-selector-tag">li</span> t0, [constantname]
                </code></pre>

    <li>Jump Label Definition (jlabel)<code>[jlabel]:</code></li>

    <pre><code>.<span class="hljs-built_in">text</span>
                main:
                    ...
                    jal     <span class="hljs-built_in">loop</span>
                    ...

                .<span class="hljs-built_in">loop</span>:
                    ...
                    beqz    t0, <span class="hljs-built_in">end</span>
                    j       main.<span class="hljs-built_in">loop</span>

                <span class="hljs-built_in">end</span>:
                </code></pre>

    <li>Instruction Definition <code>[instr] [parameters]</code></li>

    <pre><code>.text
                     lui    t0, <span class="hljs-number">0xCAFEA</span>
                     addi   t0, t0, <span class="hljs-number">0b111111111110</span>
                     <span class="hljs-built_in">li</span>     t1, -<span class="hljs-number">4000</span>
                     sltz   t2, t1
                     ...
                </code></pre>

    <li>Initialized Address Labels <code>[alabel] [type directive] [constant]</code></li>

    <pre><code><span class="hljs-selector-class">.data</span>
                    lbl1: <span class="hljs-selector-class">.word</span>     <span class="hljs-string">"jklm"</span>
                    lbl2: <span class="hljs-selector-class">.half</span>     -<span class="hljs-number">34</span>
                    lbl3: <span class="hljs-selector-class">.byte</span>     <span class="hljs-number">0</span>b11111011
                    lbl4: <span class="hljs-selector-class">.asciz</span>    <span class="hljs-string">'['</span>
                    lbl5: <span class="hljs-selector-class">.string</span>   <span
                class="hljs-string">"hello world"</span>
                </code></pre>
</ul>
<h2 id="implemented-instructions">Implemented Instructions</h2>
<table>
    <thead>
    <tr>
        <th style="text-align:center">name</th>
        <th style="text-align:center">pseudo usage</th>
        <th style="text-align:left">original params</th>
        <th>pseudo params</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td style="text-align:center"><code>lui</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm20</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>auipc</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm20</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>jal</code></td>
        <td style="text-align:center"><code>jal [ra], jlabel</code></td>
        <td style="text-align:left"><code>rd, imm20</code></td>
        <td><code>rd, jlabel</code>, <code>jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>jalr</code></td>
        <td style="text-align:center"><code>jalr [ra], [0](rs1)</code></td>
        <td style="text-align:left"><code>rd, imm12(rs1)</code></td>
        <td><code>rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>ecall</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>ebreak</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>beq</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bne</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>blt</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bge</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bltu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bgeu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs1, rs2, imm12</code></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>lb</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm12(rs)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>lh</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm12(rs)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>lw</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm12(rs)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>lbu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm12(rs)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>lhu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, imm12(rs)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sb</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs2, imm5(rs1)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sh</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs2, imm5(rs1)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sw</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rs2, imm5(rs1)</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>addi</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>slti</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sltiu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>xori</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>ori</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>andi</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, imm12</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>slli</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, shamt5</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>srli</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, shamt5</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>srai</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, shamt5</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>add</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sub</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sll</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>slt</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sltu</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>xor</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>srl</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sra</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>or</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>and</code></td>
        <td style="text-align:center">-</td>
        <td style="text-align:left"><code>rd, rs1, rs2</code></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>nop</code></td>
        <td style="text-align:center"><code>add [zero], [zero], [zero]</code></td>
        <td style="text-align:left"></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>mv</code></td>
        <td style="text-align:center"><code>addi rd, rs1, [0]</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>li</code></td>
        <td style="text-align:center"><code>lui rd, %hi20(imm32)</code><br><code>addi rd, %low12(imm32)</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, imm32</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>la</code></td>
        <td style="text-align:center"><code>lui rd, %hi20(alabel)</code><br><code>addi rd, %low12(alabel)</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, alabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>not</code></td>
        <td style="text-align:center"><code>xori rd, rs1, [0b111111111111]</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>neg</code></td>
        <td style="text-align:center"><code>sub rd, [zero], rs1</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>seqz</code></td>
        <td style="text-align:center"><code>sltiu rd, rs1, [1]</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>snez</code></td>
        <td style="text-align:center"><code>sltu rd, [zero], rs1</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sltz</code></td>
        <td style="text-align:center"><code>slt rd, rs1, [zero]</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>sgtz</code></td>
        <td style="text-align:center"><code>slt rd, [zero], rs1</code></td>
        <td style="text-align:left"></td>
        <td><code>rd, rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>beqz</code></td>
        <td style="text-align:center"><code>beq rs1, [zero], jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bnez</code></td>
        <td style="text-align:center"><code>bne rs1, [zero], jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>blez</code></td>
        <td style="text-align:center"><code>bge [zero], rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bgez</code></td>
        <td style="text-align:center"><code>bge rs1, [zero], jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bltz</code></td>
        <td style="text-align:center"><code>blt rs1, [zero], jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bgtz</code></td>
        <td style="text-align:center"><code>blt [zero], rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bgt</code></td>
        <td style="text-align:center"><code>blt rs2, rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>ble</code></td>
        <td style="text-align:center"><code>bge rs2, rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bgtu</code></td>
        <td style="text-align:center"><code>bltu rs2, rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>bleu</code></td>
        <td style="text-align:center"><code>bgeu rs2, rs1, jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1, rs2, jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>j</code></td>
        <td style="text-align:center"><code>jal [zero], jlabel</code></td>
        <td style="text-align:left"></td>
        <td><code>jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>jr</code></td>
        <td style="text-align:center"><code>jalr [zero], [0](rs1)</code></td>
        <td style="text-align:left"></td>
        <td><code>rs1</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>ret</code></td>
        <td style="text-align:center"><code>jalr [zero], [0]([ra])</code></td>
        <td style="text-align:left"></td>
        <td></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>call</code></td>
        <td style="text-align:center"><code>auipc x1, %hi20(jlabel)</code> <br/> <code>jalr x1,
            %low12(jlabel)(x1)</code></td>
        <td style="text-align:left"></td>
        <td><code>jlabel</code></td>
    </tr>
    <tr>
        <td style="text-align:center"><code>tail</code></td>
        <td style="text-align:center"><code>auipc x6, %hi20(jlabel)</code> <br/> <code>jalr x0,
            %low12(jlabel)(x6)</code></td>
        <td style="text-align:left"></td>
        <td><code>jlabel</code></td>
    </tr>
    </tbody>
</table>
<h2 id="value-input-sizes-and-types">Value Input Sizes and Types</h2>
<ul>
    <li>values will <strong>automatically resize to 32 Bit, if value fits in less than 32 Bit</strong> (32Bit, 64Bit, 128Bit)</li>
    <li>on compilation every value will <strong>first</strong> be converted <strong>to binary</strong></li>
    <li>If the input value was a <strong>decimal number</strong> it will be upsized <strong>signed</strong> in <strong>other cases</strong> it will be resized unsigned</li>
</ul>
<table>
    <thead>
    <tr>
        <th style="text-align:left">type</th>
        <th style="text-align:center">examples</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td style="text-align:left">binary</td>
        <td style="text-align:center"><code>0b10011101</code></td>
    </tr>
    <tr>
        <td style="text-align:left">hex</td>
        <td style="text-align:center"><code>0x9D</code></td>
    </tr>
    <tr>
        <td style="text-align:left">decimal</td>
        <td style="text-align:center"><code>-99</code></td>
    </tr>
    <tr>
        <td style="text-align:left">unsigned decimal</td>
        <td style="text-align:center"><code>u157</code></td>
    </tr>
    <tr>
        <td style="text-align:left">ascii</td>
        <td style="text-align:center"><code>&#39;a&#39;</code></td>
    </tr>
    <tr>
        <td style="text-align:left">string</td>
        <td style="text-align:center"><code>&quot;hello world&quot;</code></td>
    </tr>
    </tbody>
</table>
<p><strong>Tips</strong></p>
<ul>
    <li>The Value of 64 Bit and 128 Bit can only be stored as an array to fulfill that you can use the .data directive <br>and initialize an array like this lbl1: .string 0x0123456789ABCDEF</li>
</ul>
<h3 id="assembly-resize-behaviour">Assembly Resize Behaviour</h3>
<p>When will my coded value be resized if the size of expected and found values aren&#39;t matching?</p>
<ul>
    <li><strong>(text-section)</strong></li>
    <p> on writing binary into opcodes <br>(32 Bit, 20 Bit, 12 Bit, 5 Bit) <br>see implemented instructions for further details</p>
    <li><strong>(data-section)</strong></li>
    <p>on writing initiated values to memory <br>(.byte -&gt; 8 Bit, .half -&gt; 16 Bit, .word -&gt; 32 Bit, .asciz -&gt; 8 Bit, .string -&gt; 8 Bit Array (unlimited))</p>
</ul>
<h3 id="notes">Notes</h3>
<ul>
    <li>writing a imm32 value into a smaller immediate size place (e.g. imm20, imm12) <br>leads to resizing which can result in a loss of information</li>
</ul>
"""
        )
    )

    // PROCESSOR CONFIG
    val config = Config(
        """RISC-V""",
        riscVDocs,
        FileHandler("s"),
        RegisterContainer(
            listOf(
                RegisterContainer.RegisterFile(
                    "main", arrayOf(
                        RegisterContainer.Register(MutVal.Value.Dec("0", REG_ADDRESS_SIZE), listOf("x0"), listOf("zero"), MutVal(REG_INIT, REG_SIZE), "hardwired zero", hardwire = true),
                        RegisterContainer.Register(MutVal.Value.Dec("1", REG_ADDRESS_SIZE), listOf("x1"), listOf("ra"), MutVal(REG_INIT, REG_SIZE), "return address"),
                        RegisterContainer.Register(MutVal.Value.Dec("2", REG_ADDRESS_SIZE), listOf("x2"), listOf("sp"), MutVal(REG_INIT, REG_SIZE), "stack pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("3", REG_ADDRESS_SIZE), listOf("x3"), listOf("gp"), MutVal(REG_INIT, REG_SIZE), "global pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("4", REG_ADDRESS_SIZE), listOf("x4"), listOf("tp"), MutVal(REG_INIT, REG_SIZE), "thread pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("5", REG_ADDRESS_SIZE), listOf("x5"), listOf("t0"), MutVal(REG_INIT, REG_SIZE), "temporary register 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("6", REG_ADDRESS_SIZE), listOf("x6"), listOf("t1"), MutVal(REG_INIT, REG_SIZE), "temporary register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("7", REG_ADDRESS_SIZE), listOf("x7"), listOf("t2"), MutVal(REG_INIT, REG_SIZE), "temporary register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("8", REG_ADDRESS_SIZE), listOf("x8"), listOf("s0", "fp"), MutVal(REG_INIT, REG_SIZE), "saved register 0 / frame pointer"),
                        RegisterContainer.Register(MutVal.Value.Dec("9", REG_ADDRESS_SIZE), listOf("x9"), listOf("s1"), MutVal(REG_INIT, REG_SIZE), "saved register 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("10", REG_ADDRESS_SIZE), listOf("x10"), listOf("a0"), MutVal(REG_INIT, REG_SIZE), "function argument 0 / return value 0"),
                        RegisterContainer.Register(MutVal.Value.Dec("11", REG_ADDRESS_SIZE), listOf("x11"), listOf("a1"), MutVal(REG_INIT, REG_SIZE), "function argument 1 / return value 1"),
                        RegisterContainer.Register(MutVal.Value.Dec("12", REG_ADDRESS_SIZE), listOf("x12"), listOf("a2"), MutVal(REG_INIT, REG_SIZE), "function argument 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("13", REG_ADDRESS_SIZE), listOf("x13"), listOf("a3"), MutVal(REG_INIT, REG_SIZE), "function argument 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("14", REG_ADDRESS_SIZE), listOf("x14"), listOf("a4"), MutVal(REG_INIT, REG_SIZE), "function argument 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("15", REG_ADDRESS_SIZE), listOf("x15"), listOf("a5"), MutVal(REG_INIT, REG_SIZE), "function argument 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("16", REG_ADDRESS_SIZE), listOf("x16"), listOf("a6"), MutVal(REG_INIT, REG_SIZE), "function argument 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("17", REG_ADDRESS_SIZE), listOf("x17"), listOf("a7"), MutVal(REG_INIT, REG_SIZE), "function argument 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("18", REG_ADDRESS_SIZE), listOf("x18"), listOf("s2"), MutVal(REG_INIT, REG_SIZE), "saved register 2"),
                        RegisterContainer.Register(MutVal.Value.Dec("19", REG_ADDRESS_SIZE), listOf("x19"), listOf("s3"), MutVal(REG_INIT, REG_SIZE), "saved register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("20", REG_ADDRESS_SIZE), listOf("x20"), listOf("s4"), MutVal(REG_INIT, REG_SIZE), "saved register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("21", REG_ADDRESS_SIZE), listOf("x21"), listOf("s5"), MutVal(REG_INIT, REG_SIZE), "saved register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("22", REG_ADDRESS_SIZE), listOf("x22"), listOf("s6"), MutVal(REG_INIT, REG_SIZE), "saved register 6"),
                        RegisterContainer.Register(MutVal.Value.Dec("23", REG_ADDRESS_SIZE), listOf("x23"), listOf("s7"), MutVal(REG_INIT, REG_SIZE), "saved register 7"),
                        RegisterContainer.Register(MutVal.Value.Dec("24", REG_ADDRESS_SIZE), listOf("x24"), listOf("s8"), MutVal(REG_INIT, REG_SIZE), "saved register 8"),
                        RegisterContainer.Register(MutVal.Value.Dec("25", REG_ADDRESS_SIZE), listOf("x25"), listOf("s9"), MutVal(REG_INIT, REG_SIZE), "saved register 9"),
                        RegisterContainer.Register(MutVal.Value.Dec("26", REG_ADDRESS_SIZE), listOf("x26"), listOf("s10"), MutVal(REG_INIT, REG_SIZE), "saved register 10"),
                        RegisterContainer.Register(MutVal.Value.Dec("27", REG_ADDRESS_SIZE), listOf("x27"), listOf("s11"), MutVal(REG_INIT, REG_SIZE), "saved register 11"),
                        RegisterContainer.Register(MutVal.Value.Dec("28", REG_ADDRESS_SIZE), listOf("x28"), listOf("t3"), MutVal(REG_INIT, REG_SIZE), "temporary register 3"),
                        RegisterContainer.Register(MutVal.Value.Dec("29", REG_ADDRESS_SIZE), listOf("x29"), listOf("t4"), MutVal(REG_INIT, REG_SIZE), "temporary register 4"),
                        RegisterContainer.Register(MutVal.Value.Dec("30", REG_ADDRESS_SIZE), listOf("x30"), listOf("t5"), MutVal(REG_INIT, REG_SIZE), "temporary register 5"),
                        RegisterContainer.Register(MutVal.Value.Dec("31", REG_ADDRESS_SIZE), listOf("x31"), listOf("t6"), MutVal(REG_INIT, REG_SIZE), "temporary register 6")
                    )
                )
            ),
            pcSize = MutVal.Size.Bit32()
        ),
        Memory(MutVal.Size.Bit32(), MEM_INIT, MutVal.Size.Bit8(), Memory.Endianess.LittleEndian),
        Transcript(TS_COMPILED_HEADERS.entries.map { it.name }, TS_DISASSEMBLED_HEADERS.entries.map { it.name })
    )

    // EDITOR

    val FileExtension = "rvasm"
    val FileTypeDescription = "RISC-V Assembler"


}