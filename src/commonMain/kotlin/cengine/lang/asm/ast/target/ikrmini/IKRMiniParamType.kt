package cengine.lang.asm.ast.target.ikrmini

import cengine.lang.asm.ast.Component
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.impl.ASNodeType

enum class IKRMiniParamType(val rule: Rule?, val wordAmount: Int, val exampleString: String) {
    IND(Rule { Component.Seq(Component.Specific("("), Component.Specific("("), Component.SpecNode(ASNodeType.INT_EXPR), Component.Specific(")"), Component.Specific(")")) }, 2, "(([16 Bit]))"),
    IND_OFF(Rule { Component.Seq(Component.Specific("("), Component.SpecNode(ASNodeType.INT_EXPR), Component.Specific(","), Component.Specific("("), Component.SpecNode(ASNodeType.INT_EXPR), Component.Specific(")"), Component.Specific(")")) }, 3, "([16 Bit],([16 Bit]))"),
    DIR(Rule { Component.Seq(Component.Specific("("), Component.SpecNode(ASNodeType.INT_EXPR), Component.Specific(")"), print = true) }, 2, "([16 Bit])"),
    IMM(Rule { Component.Seq(Component.Specific("#"), Component.SpecNode(ASNodeType.INT_EXPR)) }, 2, "#[16 Bit]"),
    DEST(Rule { Component.Seq(Component.SpecNode(ASNodeType.INT_EXPR)) }, 2, "[label]"),
    IMPL(null, 1, "");
}