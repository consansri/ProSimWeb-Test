package ui.uilib.emulator

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import cengine.util.integer.Value
import cengine.util.integer.Value.Companion.toValue
import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CInput
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane

@Composable
fun RegView(arch: Architecture) {

    val tabs = remember { arch.regContainer.getRegFileList().map { TabItem(it, title = it.name) } }

    TabbedPane(tabs, false, content = { tabIndex ->

        val tab = tabs[tabIndex]

        key(tab.value.name) {
            RegTable(tab.value)
        }

    }, baseStyle = UIState.BaseStyle.current)
}

@Composable
fun RegTable(regFile: RegContainer.RegisterFile) {
    val scale = UIState.Scale.value
    val theme = UIState.Theme.value

    val regs = remember { mutableStateListOf(*regFile.unsortedRegisters) }

    val vScrollState = rememberScrollState()
    var numberFormat by remember { mutableStateOf<Value.Types>(Value.Types.Hex) }
    var sortByAddress by remember { mutableStateOf<Boolean>(false) }
    var showDescription by remember { mutableStateOf(false) }

    val valueHScroll = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {

            Box(
                modifier = Modifier.weight(0.4f),
                contentAlignment = Alignment.Center
            ) {
                CToggle(
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = UIState.BaseStyle.current,
                    text = if (sortByAddress) {
                        "Reg (by addr)"
                    } else {
                        "Reg"
                    }, onClick = {
                        sortByAddress = it
                    },
                    softWrap = false,
                    value = sortByAddress
                )
            }

            Box(
                modifier = Modifier.weight(0.5f),
                contentAlignment = Alignment.Center
            ) {
                CButton(text = numberFormat.visibleName, modifier = Modifier.fillMaxWidth(), softWrap = false, onClick = {
                    numberFormat = numberFormat.next()
                })
            }

            Box(
                modifier = Modifier.weight(0.1f),
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "CC", textStyle = UIState.BaseStyle.current, softWrap = false)
            }

            Box(
                modifier = if (showDescription) {
                    Modifier.weight(0.30f)
                } else Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                CToggle(text = if (showDescription) "Description" else "+", value = showDescription, onClick = {
                    showDescription = it
                })
            }
        }

        Column(
            Modifier.fillMaxSize()
                .verticalScroll(vScrollState)
        ) {
            regs.forEach { reg ->
                key("reg:${reg.names + reg.aliases}:$numberFormat") {
                    RegRow(reg, numberFormat, valueHScroll, showDescription)
                }
            }
        }
    }

    LaunchedEffect(sortByAddress) {
        regs.clear()
        regs.addAll(if (sortByAddress) {
            regFile.unsortedRegisters.sortedBy { it.address.toValue().toULong() }
        } else {
            regFile.unsortedRegisters.toList()
        })
    }
}

@Composable
fun RegRow(reg: RegContainer.Register, numberFormat: Value.Types, valueHScroll: ScrollState, showDescription: Boolean) {
    val regState by reg.variable.state

    fun getRegString(): String {
        return when (numberFormat) {
            Value.Types.Bin -> regState.toBin().rawInput
            Value.Types.Hex -> regState.toHex().rawInput
            Value.Types.Dec -> regState.toDec().rawInput
            Value.Types.UDec -> regState.toUDec().rawInput
        }
    }

    var regValue by remember { mutableStateOf(TextFieldValue(getRegString())) }
    val regNames = remember { (reg.names + reg.aliases).joinToString(" ") { it } }

    LaunchedEffect(regState) {
        regValue = regValue.copy(text = getRegString())
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.weight(0.4f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(regNames, Modifier.fillMaxWidth(), softWrap = false, fontFamily = UIState.CodeStyle.current.fontFamily, color = UIState.Theme.value.COLOR_FG_0, fontSize = UIState.CodeStyle.current.fontSize, textAlign = TextAlign.Left)
        }

        Box(
            modifier = Modifier.weight(0.5f)
                .horizontalScroll(valueHScroll),
            contentAlignment = Alignment.Center
        ) {
            CInput(
                value = regValue,
                onValueChange = { newVal ->
                    regValue = newVal
                },
                onFocusLost = { newVal ->
                    when (numberFormat) {
                        Value.Types.Bin -> reg.variable.setBin(newVal.text)
                        Value.Types.Hex -> reg.variable.setHex(newVal.text)
                        Value.Types.Dec -> reg.variable.setDec(newVal.text)
                        Value.Types.UDec -> reg.variable.setUDec(newVal.text)
                    }
                },
                numberFormat = numberFormat,
            )
        }

        Box(
            modifier = Modifier.weight(0.1f),
            contentAlignment = Alignment.Center
        ) {
            Text(reg.callingConvention.displayName, Modifier.fillMaxWidth(), softWrap = false, fontFamily = UIState.BaseStyle.current.fontFamily, color = UIState.Theme.value.COLOR_FG_0, fontSize = UIState.BaseStyle.current.fontSize, textAlign = TextAlign.Center)
        }

        if (showDescription) {
            Box(
                modifier = Modifier.weight(0.30f),
                contentAlignment = Alignment.Center
            ) {
                Text(reg.description, Modifier.fillMaxWidth(), softWrap = false, fontFamily = UIState.BaseSmallStyle.current.fontFamily, color = UIState.Theme.value.COLOR_FG_0, fontSize = UIState.BaseSmallStyle.current.fontSize, textAlign = TextAlign.Left)
            }
        } else {
            Spacer(Modifier.weight(0.05f))
        }

    }
}


