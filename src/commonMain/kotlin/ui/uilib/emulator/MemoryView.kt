package ui.uilib.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import cengine.util.integer.toULong
import emulator.kit.MicroSetup
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import ui.uilib.UIState
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import ui.uilib.params.FontType

@Composable
fun MemView() {

    val memoryList = remember { MicroSetup.memory }
    var memoryTabs by remember { mutableStateOf<List<TabItem<Memory>>>(memoryList.map { TabItem(it, title = it.name) }) }

    TabbedPane(memoryTabs, content = {
        val mem = memoryTabs[it]

        key(mem.value.name) {
            MemoryView(mem.value)
        }
    })

    LaunchedEffect(memoryList) {
        memoryTabs = memoryList.map { TabItem(it, title = it.name) }
    }

}

@Composable
fun MemoryView(memory: Memory) {
    when (memory) {
        is Cache -> {
            CacheView(memory)
        }

        is MainMemory -> {
            MainMemoryView(memory)
        }
    }
}

@Composable
fun MainMemoryView(memory: MainMemory) {

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
                MemoryRow(rowAddress.toRawString(), instances, memory.entrysInRow, fillValue, codeFont, theme.COLOR_FG_0)
            }
        }
    }
}

@Composable
fun MemoryRow(rowAddress: String, instances: List<MainMemory.MemInstance>, entrysInRow: Int, fillValue: String, codeFont: FontFamily, fgColor: Color) {

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
                        Text(instance.variable.state.value.toHex().toRawString(), fontFamily = codeFont, color = fgColor)
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
fun CacheView(memory: Cache) {

    val vScrollState = rememberScrollState()
    val theme = UIState.Theme.value
    val baseFont = FontType.MEDIUM.getFamily()
    val codeFont = FontType.CODE.getFamily()

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

        /*Column(Modifier.fillMaxSize().verticalScroll(vScrollState)) {

            memory.model.rows.forEach { row ->
                key(row.rowIndexBinStr) {
                    Row {
                        Box(Modifier.weight(0.05f)) {
                            CLabel(Modifier.fillMaxWidth(), text = row.rowIndexBinStr.toULong(2).toString(16), textAlign = TextAlign.Right)
                        }

                        Column(Modifier.weight(0.95f)) {
                            row.blocks.forEachIndexed { index, block ->
                                Box(Modifier.weight(0.05f)) {
                                    Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Row(Modifier.weight(0.7f)) {
                                    block.data.forEachIndexed { index, cacheInstance ->
                                        Box(Modifier.weight(1f)) {
                                            Text(cacheInstance.value.toRawString(), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }*/

        LazyColumn(Modifier.fillMaxSize()) {
            items(memory.model.rows, key = { row ->
                row.rowIndexBinStr
            }) { row ->
                val rowAddr = remember { row.rowIndexBinStr.toULong(2).toString(16) }
                Row {
                    Box(Modifier.weight(0.05f)) {
                        Text(rowAddr, Modifier.fillMaxWidth(), fontFamily = codeFont, textAlign = TextAlign.Right)
                    }

                    Column(Modifier.weight(0.95f)) {
                        row.blocks.forEachIndexed { index, block ->
                            Box(Modifier.weight(0.05f)) {
                                Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                            }

                            Box(Modifier.weight(0.2f)) {
                                Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                            }

                            Row(Modifier.weight(0.7f)) {
                                block.data.forEachIndexed { index, cacheInstance ->
                                    Box(Modifier.weight(1f)) {
                                        Text(cacheInstance.value.toRawString(), Modifier.fillMaxWidth(), fontFamily = codeFont, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
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
