package ui.uilib.emulator

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import cengine.util.integer.Value
import cengine.util.integer.toULong
import emulator.kit.Architecture
import emulator.kit.common.RegContainer
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.interactable.CInput
import ui.uilib.interactable.CToggle
import ui.uilib.label.CLabel
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import ui.uilib.params.FontType

@Composable
fun RegView(arch: Architecture) {

    val tabs = remember { arch.regContainer.getRegFileList().map { TabItem(it, title = it.name) } }

    TabbedPane(tabs, false, content = { tabIndex ->

        val tab = tabs[tabIndex]

        key(tab.value.name) {
            RegTable(tab.value)
        }

    })
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
                modifier = Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                CToggle(
                    modifier = Modifier.fillMaxWidth(),
                    fontType = FontType.MEDIUM,
                    text = if (sortByAddress) {
                        "Reg (by addr)"
                    } else {
                        "Reg"
                    }, onClick = {
                        sortByAddress = it
                    },
                    softWrap = false,
                    initialToggle = sortByAddress
                )
            }

            Box(
                modifier = Modifier.weight(0.6f),
                contentAlignment = Alignment.Center
            ) {
                CButton(text = numberFormat.visibleName, modifier = Modifier.fillMaxWidth(), softWrap = false, fontType = FontType.MEDIUM, onClick = {
                    numberFormat = numberFormat.next()
                })
            }

            Box(
                modifier = Modifier.weight(0.1f),
                contentAlignment = Alignment.Center
            ) {
                CLabel(fontType = FontType.MEDIUM, softWrap = false, text = "CC")
            }

            Box(
                modifier = if (showDescription) {
                    Modifier.weight(0.3f)
                } else Modifier,
                contentAlignment = Alignment.Center
            ) {
                CToggle(text = if (showDescription) "Description" else "+", initialToggle = showDescription, onClick = {
                    showDescription = it
                })
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(regs){reg ->
                key("reg:${reg.names + reg.aliases}:$numberFormat") {
                    RegRow(reg, numberFormat, valueHScroll, showDescription)
                }
            }
        }
    }

    LaunchedEffect(sortByAddress) {
        regs.clear()
        regs.addAll(if (sortByAddress) {
            regFile.unsortedRegisters.sortedBy { it.address.toHex().toULong() ?: 0U }
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
            Value.Types.Bin -> regState.toBin().toRawString()
            Value.Types.Hex -> regState.toHex().toRawString()
            Value.Types.Dec -> regState.toDec().toRawString()
            Value.Types.UDec -> regState.toUDec().toRawString()
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
            modifier = Modifier.weight(0.2f),
            contentAlignment = Alignment.CenterStart
        ) {
            CLabel(modifier = Modifier.fillMaxWidth(), softWrap = false, fontType = FontType.CODE, textAlign = TextAlign.Left, text = regNames)
        }

        Box(
            modifier = Modifier.weight(0.6f)
                .horizontalScroll(valueHScroll),
            contentAlignment = Alignment.Center
        ) {
            // TODO Replace CLabel with a CInput which accepts a specific numberformat which only allows certain input chars
            CInput(
                value = regValue,
                onValueChange = { newVal ->
                    regValue = newVal
                },
                onFocusLost = {newVal ->
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
            CLabel(fontType = FontType.MEDIUM, softWrap = false, text = reg.callingConvention.displayName)
        }

        if (showDescription) {
            Box(
                modifier = Modifier.weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                CLabel(fontType = FontType.SMALL, softWrap = false, text = reg.description)
            }
        }

    }
}


