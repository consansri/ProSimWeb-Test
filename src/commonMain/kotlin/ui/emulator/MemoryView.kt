package ui.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import cengine.util.newint.IntNumber
import emulator.kit.Architecture
import emulator.kit.MicroSetup
import emulator.kit.memory.*
import ui.uilib.UIState
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import kotlin.math.log2
import kotlin.math.roundToInt

@Composable
fun MemView(arch: Architecture<*, *>) {
    val memoryList = remember { MicroSetup.memories }
    var memoryTabs by remember { mutableStateOf(memoryList.map { TabItem(it, title = it.name) }) }

    TabbedPane(memoryTabs, content = {
        val mem = memoryTabs[it]

        key(mem.value.name) {
            MemoryView(mem.value, arch.pcState.value, UIState.BaseStyle.current, UIState.CodeStyle.current)
        }
    }, baseStyle = UIState.BaseStyle.current)

    LaunchedEffect(memoryList) {
        memoryTabs = memoryList.map { TabItem(it, title = it.name) }
    }

}

@Composable
fun MemoryView(memory: Memory<*, *>, pc: IntNumber<*>, baseStyle: TextStyle, codeStyle: TextStyle) {
    when (memory) {
        is FACache -> {
            FACacheView(memory, pc, baseStyle, codeStyle)
        }

        is DMCache -> {
            DMCacheView(memory, pc, baseStyle, codeStyle)
        }

        is SACache -> {
            SACacheView(memory, pc, baseStyle, codeStyle)
        }

        is MainMemory -> {
            MainMemoryView(memory, pc, baseStyle, codeStyle)
        }
    }
}

@Composable
fun MainMemoryView(memory: MainMemory<*, *>, pc: IntNumber<*>, baseStyle: TextStyle, codeStyle: TextStyle) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val fillValue = remember { memory.init }

    val entrysInRow = 32 / fillValue.byteCount
    val offsetBits = log2(entrysInRow.toFloat()).roundToInt()
    val offsetMask = IntNumber.bitMask(offsetBits)

    Column {
        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("ADDR", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0)
            }

            Row(
                Modifier.weight(0.5f)
            ) {
                for (offset in 0..<entrysInRow) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(offset.toString(16), fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0)
                    }
                }
            }

            Box(
                Modifier.weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Text("ASCII", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0)
            }
        }

        LazyColumn {
            val grouped = memory.memList.toList().sortedBy { it.first.toBigInt().value }.groupBy { it.first / entrysInRow }.toList()

            items(grouped.size) { index ->
                val (rowAddr, instances) = grouped[index]

                val ascii = remember {
                    (0..<entrysInRow).joinToString("") { offset ->
                        val char = instances.firstOrNull {
                            (it.first and offsetMask).toInt() == offset
                        }?.second?.toInt()

                        when (char) {
                            null -> "·"
                            in 0x20..0x7E -> char.toChar().toString() // Printable Range
                            else -> "·" // Non-printable characters are replaced with a dot
                        }
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier.weight(0.2f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(rowAddr.toString(16), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0)
                    }

                    Row(Modifier.weight(0.5f)) {
                        for (i in 0..<entrysInRow) {
                            val instance = instances.firstOrNull {
                                val instanceIndex = (it.first and offsetMask).toInt()
                                instanceIndex == i
                            }

                            if (instance != null) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(instance.second.zeroPaddedHex(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (instance.first == pc) theme.COLOR_GREEN else theme.COLOR_FG_0)
                                }
                            } else {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(fillValue.zeroPaddedHex(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_1)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.weight(0.3f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ascii, fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_1)
                    }
                }
            }
        }
    }
}


@Composable
fun DMCacheView(memory: DMCache<*, *>, pc: IntNumber<*>, baseStyle: TextStyle, codeStyle: TextStyle) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val pcColor = theme.COLOR_GREEN

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("valid", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("dirty", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(16), fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(memory.model.rows.size, key = { row ->
                row
            }) { rowIndex ->
                val row = memory.model.rows[rowIndex]

                Row {
                    Box(Modifier.weight(0.05f)) {
                        Text(row.rowIndex.zeroPaddedHex(), Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                    }

                    Column(Modifier.weight(0.95f)) {
                        row.blocks.forEachIndexed { index, block ->
                            val isInvalid = block.tag == null

                            Row(Modifier.fillMaxWidth()) {
                                Box(Modifier.weight(0.05f)) {
                                    Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    if (block.valid) {
                                        Icon(UIState.Icon.value.statusFine, "1", Modifier.size(UIState.Scale.value.SIZE_CONTROL_SMALL), theme.COLOR_GREEN)
                                    } else {
                                        Icon(UIState.Icon.value.statusError, "0", Modifier.size(UIState.Scale.value.SIZE_CONTROL_SMALL), theme.COLOR_FG_1)
                                    }
                                }

                                Box(Modifier.weight(0.2f)) {
                                    Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    if (block.dirty) {
                                        Icon(UIState.Icon.value.statusFine, "1", Modifier.size(UIState.Scale.value.SIZE_CONTROL_SMALL), theme.COLOR_GREEN)
                                    } else {
                                        Icon(UIState.Icon.value.statusError, "0", Modifier.size(UIState.Scale.value.SIZE_CONTROL_SMALL), theme.COLOR_FG_1)
                                    }
                                }

                                Row(Modifier.weight(0.7f)) {
                                    block.data.forEachIndexed { index, cacheInstance ->
                                        val addr = memory.addrFor(row.rowIndex, block.tag, index)
                                        Box(Modifier.weight(1f)) {
                                            Text(
                                                cacheInstance.zeroPaddedHex(),
                                                Modifier.fillMaxWidth(),
                                                fontFamily = codeStyle.fontFamily,
                                                fontSize = codeStyle.fontSize,
                                                color = if (isInvalid) theme.COLOR_FG_1 else if (addr == pc) pcColor else theme.COLOR_FG_0,
                                                textAlign = TextAlign.Center
                                            )
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
fun SACacheView(memory: SACache<*, *>, pc: IntNumber<*>, baseStyle: TextStyle, codeStyle: TextStyle) {

    val theme = UIState.Theme.value
    val pcColor = theme.COLOR_GREEN

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(16), fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        LazyColumn(Modifier.fillMaxSize()) {
            items(memory.model.rows.size, key = { rowIndex ->
                rowIndex
            }) { rowIndex ->
                val row = memory.model.rows[rowIndex]

                Row {
                    Box(Modifier.weight(0.05f)) {
                        Text(row.rowIndex.toString(16), Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Right)
                    }

                    Column(Modifier.weight(0.95f)) {
                        row.blocks.forEachIndexed { index, block ->
                            val isInvalid = block.tag == null

                            Row(Modifier.fillMaxWidth()) {
                                Box(Modifier.weight(0.05f)) {
                                    Text(index.toString(16), Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Box(Modifier.weight(0.2f)) {
                                    Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                                }

                                Row(Modifier.weight(0.7f)) {
                                    block.data.forEachIndexed { index, cacheInstance ->
                                        val addr = memory.addrFor(row.rowIndex, block.tag, index)
                                        Box(Modifier.weight(1f)) {
                                            Text(
                                                cacheInstance.zeroPaddedHex(),
                                                Modifier.fillMaxWidth(),
                                                fontFamily = codeStyle.fontFamily,
                                                fontSize = codeStyle.fontSize,
                                                color = if (isInvalid) theme.COLOR_FG_1 else if (addr == pc) pcColor else theme.COLOR_FG_0,
                                                textAlign = TextAlign.Center
                                            )
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
fun FACacheView(memory: FACache<*, *>, pc: IntNumber<*>, baseStyle: TextStyle, codeStyle: TextStyle) {
    val theme = UIState.Theme.value
    val pcColor = theme.COLOR_GREEN

    Column(Modifier.fillMaxSize()) {

        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("k", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.05f),
                contentAlignment = Alignment.Center
            ) {
                Text("m", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                Text("tag", fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
            }

            Row(Modifier.weight(0.7f)) {
                for (s in 0..<memory.model.offsetCount) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s.toString(16), fontFamily = baseStyle.fontFamily, fontSize = baseStyle.fontSize, color = theme.COLOR_FG_0, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Row(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.05f))

            val row = memory.model.rows.first()
            LazyColumn(Modifier.weight(0.95f)) {
                items(row.blocks.size, key = { blockIndex ->
                    blockIndex
                }) { blockIndex ->
                    val block = row.blocks[blockIndex]
                    val isInvalid = block.tag == null
                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(0.05f)) {
                            Text(blockIndex.toString(16), Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                        }

                        Box(Modifier.weight(0.2f)) {
                            Text(block.tag?.toULong()?.toString(16) ?: "invalid", Modifier.fillMaxWidth(), fontFamily = codeStyle.fontFamily, fontSize = codeStyle.fontSize, color = if (isInvalid) theme.COLOR_FG_1 else theme.COLOR_FG_0, textAlign = TextAlign.Right)
                        }

                        Row(Modifier.weight(0.7f)) {
                            block.data.forEachIndexed { index, cacheInstance ->
                                val addr = memory.addrFor(row.rowIndex, block.tag, index)
                                Box(Modifier.weight(1f)) {
                                    Text(
                                        cacheInstance.zeroPaddedHex(),
                                        Modifier.fillMaxWidth(),
                                        fontFamily = codeStyle.fontFamily,
                                        fontSize = codeStyle.fontSize,
                                        color = if (isInvalid) theme.COLOR_FG_1 else if (addr == pc) pcColor else theme.COLOR_FG_0,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
