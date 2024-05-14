package top.byfree.massive

import top.byfree.massive.dsl.AbstractCell
import top.byfree.massive.dsl.CellStatus
import top.byfree.massive.dsl.massive
import top.byfree.massive.dsl.unit
import top.byfree.massive.dsl.route
import top.byfree.massive.dsl.parallelRoute
import top.byfree.massive.dsl.START
import top.byfree.massive.dsl.END



massive(globalConfig = MyParams()) {
    val t1 = unit {
        id = "T1"
        cell = CellImpl1()
        name = "测试批量1"
        inner {
            put("你好", "世界")
        }
    }
    val t2 = unit {
        id = "T2"
        cell = CellImpl2()
        name = "测试批量2"
    }

    parallelRoute(current = START, success = END, fail = t1) {
        route(t1)
        route(t2)
    }
    route(t1, success = END, fail = END)
}
data class MyParams(
    var param1: String = "p1",
    var param2: String = "p2"
)

class CellImpl1: AbstractCell<MyParams, MutableMap<String, Any>>() {
    override fun apply(): CellStatus {
        println("你好 CellImpl1" + inner!!["你好"])
        return CellStatus.FAIL
    }
}

class CellImpl2 : AbstractCell<MyParams, MutableMap<String, Any>>() {

    override fun apply(): CellStatus {
        println("你好 CellImpl12")

        return CellStatus.SUCCESS
    }
}