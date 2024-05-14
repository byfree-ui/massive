package top.byfree.massive.batch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import top.byfree.massive.dsl.*
import top.byfree.massive.orm.RowSet
import top.byfree.massive.prh.*
import javax.sql.DataSource

/**
 * @author jbyan
 * @since 2024/5/12
 */
@Component("demo-batch")
class DemoBatch(
    @Autowired
    private val dataSource: DataSource
) : Batch {


    override fun apply() = massive(globalConfig = Demo01Config()) {
        val t1 = prhUnit(RowSet::class.java) {
            id = "T1"
            name = "批量导出classify数据"

            producer {
                DBProducer(dataSource.connection, "select * from ac_classify")
            }

            handler {
                FileWriteHandler(
                    it.filePath,
                    it.fileName,
                    ",",
                    post = "\n",
                    order = mutableListOf("id", "name", "gmt_create"),
                )
            }

        }

        val t2 = prhUnit(Classify::class.java) {
            id = "T2"
            name = "批量导入classify数据"

            producer {
                FileProducer(
                    Classify::class.java,
                    it.filePath,
                    it.fileName,
                    ",",
                )
            }

            handler {
                CatHandler(Classify::class.java)
            }

        }

        route(START, success = t1)
        route(t1, success = t2)
        route(t2, success = END, fail = END)
    }
}

data class Demo01Config(
    val filePath: String = "C:\\Users\\jbyan\\IdeaProjects\\massive\\demo",
    val fileName: String = "alice.csv"
)

class Classify {
    @field:Index(0)
    lateinit var id: String

    @field:Index(1)
    lateinit var name: String

    @field:Index(2)
    lateinit var gmt_create: String
    override fun toString(): String {
        return "Classify(id='$id', name='$name', gmt_create='$gmt_create')"
    }


}