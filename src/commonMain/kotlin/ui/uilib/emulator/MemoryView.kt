package ui.uilib.emulator

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import emulator.kit.memory.Cache
import emulator.kit.memory.MainMemory
import emulator.kit.memory.Memory
import ui.uilib.UIState
import ui.uilib.params.FontType

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

    val fontFamily = FontType.CODE.getFamily()

    val instances = memory.getAllInstances().groupBy { it.row }.map { it.key to it.value }

    LazyColumn {
        items(instances.size, key = {keyIndex ->
            "row:${instances[keyIndex].first.toRawString()}"
        }) { keyIndex ->
            val (rowAddr, rowInstances) = instances[keyIndex]

            LazyRow {
                item("addr:${rowAddr.toRawString()}") {
                    Text(rowAddr.toRawString(), fontFamily = fontFamily, color = theme.COLOR_FG_1)
                }

                items(rowInstances.size, key = { offset ->
                    "value:${rowAddr.toRawString()}:$offset"
                }) { offset ->
                    val instance = rowInstances[offset]

                    Text(instance.variable.value.toHex().toRawString(), fontFamily = fontFamily, color = theme.COLOR_FG_0)
                }
            }
        }
    }
}

@Composable
fun CacheView(memory: Cache) {

}
