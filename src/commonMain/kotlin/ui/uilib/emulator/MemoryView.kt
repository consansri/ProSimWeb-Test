package ui.uilib.emulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import emulator.kit.MicroSetup
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import emulator.kit.nativeLog
import ui.uilib.UIState
import ui.uilib.label.CLabel
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

    val memList = remember { memory.memList }

    val fillValue = remember { memory.getInitialBinary().value.toHex().toRawString() }
    var instances by remember { mutableStateOf(memList.groupBy { it.row }.map { it.key to it.value }) }

    LaunchedEffect(memList) {
        nativeLog("MemList Changed")
        instances = memList.groupBy { it.row }.map { it.key to it.value }
    }

    Column {
        Row(
            Modifier.fillMaxWidth()
                .background(theme.COLOR_BG_1)
        ) {
            Box(
                Modifier.weight(0.2f),
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "ADDR")
            }

            Row(
                Modifier.weight(0.5f)
            ) {
                for (offset in 0..<memory.entrysInRow) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CLabel(text = offset.toString())
                    }
                }
            }

            Box(
                Modifier.weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                CLabel(text = "ASCII")
            }
        }

        LazyColumn {
            items(memory.memList.groupBy { it.row }.toList()) { (rowAddress, instances) ->
                MemoryRow(rowAddress.toRawString(), instances, memory.entrysInRow, fillValue)
            }
        }
    }
}

@Composable
fun MemoryRow(rowAddress: String, instances: List<MainMemory.MemInstance>, entrysInRow: Int, fillValue: String) {

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
            CLabel(text = rowAddress)
        }

        Row(Modifier.weight(0.5f)) {
            for (i in 0..<entrysInRow) {
                val instance = instances.firstOrNull { it.offset == i }

                if (instance != null) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CLabel(text = instance.variable.state.value.toHex().toRawString(), fontType = FontType.CODE)
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CLabel(text = fillValue, fontType = FontType.CODE)
                    }
                }
            }
        }

        Box(
            modifier = Modifier.weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            CLabel(text = ascii, fontType = FontType.CODE)
        }
    }
}


@Composable
fun CacheView(memory: Cache) {

}
