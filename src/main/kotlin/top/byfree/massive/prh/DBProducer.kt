package top.byfree.massive.prh

import top.byfree.massive.dsl.PrhUnit
import top.byfree.massive.dsl.Producer
import top.byfree.massive.orm.RowSet
import java.sql.Connection
import java.sql.ResultSetMetaData

/**
 * @author jbyan
 * @since 2024/5/3
 */
class DBProducer(private val conn: Connection, private val sql: String) :
    Producer<RowSet> {
    override fun exec(cb: (RowSet) -> Unit) {
        conn.prepareStatement(this.sql).use { stmt ->
            stmt.executeQuery().use { rs ->
                val metaData: ResultSetMetaData = rs.metaData
                val len = metaData.columnCount
                while (rs.next()) {
                    val rowSet = RowSet()
                    for (i in 1..len) {
                        rowSet[metaData.getColumnName(i)] = rs.getObject(i)
                    }
                    cb(rowSet)
                }
            }
        }
    }
}