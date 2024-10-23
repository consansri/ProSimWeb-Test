package ui.uilib.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import cengine.util.integer.Value
import cengine.util.integer.toULong
import emulator.kit.Architecture
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import ui.uilib.UIState
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import ui.uilib.params.FontType

@Composable
fun MemView(arch: Architecture) {

    val pc = arch.regContainer.pc.variable.state
    val memoryList = remember { MicroSetup.memory }
    var memoryTabs by remember { mutableStateOf<List<TabItem<Memory>>>(memoryList.map { TabItem(it, title = it.name) }) }

    TabbedPane(memoryTabs, content = {
        val mem = memoryTabs[it]

        key(mem.value.name) {
            MemoryView(mem.value, pc)
        }
    })

    LaunchedEffect(memoryList) {
        memoryTabs = memoryList.map { TabItem(it, title = it.name) }
    }

}

@Composable
fun MemoryView(memory: Memory, pc: MutableState<Value>) {
    when (memory) {
        is FACache -> {
            FACacheView(memory, pc)
        }

        is DMCache -> {
            DMCacheView(memory, pc)
        }

        is SACache -> {
            SACacheView(memory, pc)
        }

        is MainMemory -> {
            MainMemoryView(memory, pc)
        }
    }
}

@Composable
fun MainMemoryView(memory: MainMemory, pc: MutableState<Value>) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val baseFont = FontType.MEDIUM.getFamily()
    val codeFont = FontType.CODE.getFamily()

    val fillValue = remember { memory.getInitialBinary().value.toHex().toRawString() }

    Column {
        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("ADDR", fontFamily = baseFont, color = theme.COLOR_FG_0)
            }

            Row(
                Modifier.weight(0.5f)
            ) {
                for (offset in 0..<memory.entrysInRow) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(offset.toString(), fontFamily = baseFont, color = theme.COLOR_FG_0)
                    }
                }
            }

            Box(
                Modifier.weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Text("ASCII", fontFamily = baseFont, color = theme.COLOR_FG_0)
            }
        }

        LazyColumn {
            items(memory.memList.groupBy { it.row }.toList()) { (rowAddress, instances) ->
                MemoryRow(rowAddress.toRawString(), instances, memory.entrysInRow, fillValue, codeFont, theme.COLOR_FG_0, theme.COLOR_GREEN, pc)
            }
        }
    }
}

@Composable
fun MemoryRow(rowAddress: String, instances: List<MainMemory.MemInstance>, entrysInRow: Int, fillValue: String, codeFont: FontFamily, fgColor: Color, pcColor: Color, pc: MutableState<Value>) {

    val ascii = remember {
        (0..<entrysInRow).joinToString("") { offset ->
            instances.firstOrNull { it.offset == offset }?.variable?.state?.value?.toASCII() ?: "·"
        }
    }

    Row(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.weight(0.2f),
            contentAlignment = Alignment.Center
        ) {
            Text(rowAddress, fontFamily = codeFont, color = fgColor)
        }

        Row(Modifier.weight(0.5f)) {
            for (i in 0..<entrysInRow) {
                val instance = instances.firstOrNull { it.offset == i }

                if (instance != null) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(instance.variable.state.value.toHex().toRawString(), fontFamily = codeFont, color = if (instance.address == pc.value.toHex()) pcColor else fgColor)
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(fillValue, fontFamily = codeFont, color = fgColor)
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Text(ascii, fontFamily = codeFont, color = fgColor)
        }
    }
}


@Composable
fun DMCacheView(memory: DMCache, pc: MutableState<Value>) {
    val theme = UIState.Theme.value
    val baseFont = FontType.MEDIUM.getFamily()
    val codeFont = FontType.CODE.getFamily()
    val pcColor = theme.COLOR_GREEN
    val pcValue = pc.value.toHex()

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(), fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(memory.model.rows, key = { row ->
                row.rowIndexBinStr
            }) { row ->
                val rowAddr = remember {
                    if (row.rowIndexBinStr.isNotEmpty()) {
                        row.rowIndexBinStr.toULong(2).toString(16)
                    } else {
                        ""
                    }
                }
                Row {
                    Box(Modifier.weight(0.05f)) {
                        Text(rowAddr, Modifier.fillMaxWidth(), fontFamily = codeFont, textAlign = TextAlign.Right)
                    }

                    Column(Modifier.weight(0.95f)) {
                        row.blocks.forEachIndexed { index, block ->
                            Row(Modifier.fillMaxWidth()) {
                                Box(Modifier.weight(0.05f)) {
                                    Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Row(Modifier.weight(0.7f)) {
                                    block.data.forEachIndexed { index, cacheInstance ->
                                        Box(Modifier.weight(1f)) {
                                            Text(cacheInstance.value.toRawString(), Modifier.fillMaxWidth(), fontFamily = codeFont, color = if (cacheInstance.address == pcValue) pcColor else theme.COLOR_FG_0, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SACacheView(memory: SACache, pc: MutableState<Value>) {

    val theme = UIState.Theme.value
    val baseFont = FontType.MEDIUM.getFamily()
    val codeFont = FontType.CODE.getFamily()
    val pcColor = theme.COLOR_GREEN
    val pcValue = pc.value.toHex()

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(), fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(memory.model.rows, key = { row ->
                row.rowIndexBinStr
            }) { row ->
                val rowAddr = remember {
                    if (row.rowIndexBinStr.isNotEmpty()) {
                        row.rowIndexBinStr.toULong(2).toString(16)
                    } else {
                        ""
                    }
                }
                Row {
                    Box(Modifier.weight(0.05f)) {
                        Text(rowAddr, Modifier.fillMaxWidth(), fontFamily = codeFont, textAlign = TextAlign.Right)
                    }

                    Column(Modifier.weight(0.95f)) {
                        row.blocks.forEachIndexed { index, block ->
                            Row(Modifier.fillMaxWidth()) {
                                Box(Modifier.weight(0.05f)) {
                                    Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Row(Modifier.weight(0.7f)) {
                                    block.data.forEachIndexed { index, cacheInstance ->
                                        Box(Modifier.weight(1f)) {
                                            Text(cacheInstance.value.toRawString(), Modifier.fillMaxWidth(), fontFamily = codeFont, color = if (cacheInstance.address == pcValue) pcColor else theme.COLOR_FG_0, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FACacheView(memory: FACache, pc: MutableState<Value>) {
    val theme = UIState.Theme.value
    val baseFont = FontType.MEDIUM.getFamily()
    val codeFont = FontType.CODE.getFamily()
    val pcColor = theme.COLOR_GREEN
    val pcValue = pc.value.toHex()

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(), fontFamily = baseFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Row(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.05f))

            val row = memory.model.rows.first()
            LazyColumn(Modifier.weight(0.95f)) {
                items(row.blocks, key = { block ->
                    row.blocks.indexOf(block)
                }) { block ->
                    val index = row.blocks.indexOf(block)
                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(0.05f)) {
                            Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                        }

                        Box(Modifier.weight(0.2f)) {
                            Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                        }

                        Row(Modifier.weight(0.7f)) {
                            block.data.forEachIndexed { index, cacheInstance ->
                                Box(Modifier.weight(1f)) {
                                    Text(cacheInstance.value.toRawString(), Modifier.fillMaxWidth(), fontFamily = codeFont, color = if (cacheInstance.address == pcValue) pcColor else theme.COLOR_FG_0, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
