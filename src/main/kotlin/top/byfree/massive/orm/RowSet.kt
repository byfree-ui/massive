package top.byfree.massive.orm

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * 数据库结果行抽象
 *
 * @author jbyan
 * @since 26/1/2024
 */
class RowSet : HashMap<String, Any>() {
    fun getString(key: String?): String {
        val value = get(key)
        return if (Objects.isNull(value)) DEFAULT_STRING else value.toString()
    }

    fun getInt(key: String?): Int {
        val value = get(key) as Int?
        return if (Objects.isNull(value)) DEFAULT_INT else value!!
    }

    fun getDouble(key: String?): Double {
        val value = get(key) as Double?
        return if (Objects.isNull(value)) DEFAULT_DOUBLE else value!!
    }

    fun getDate(key: String?): LocalDate {
        val value = get(key) as LocalDate?
        return if (Objects.isNull(value)) DEFAULT_DATE else value!!
    }

    fun getDateTime(key: String?): LocalDateTime {
        val value = get(key) as LocalDateTime?
        return if (Objects.isNull(value)) DEFAULT_DATE_TIME else value!!
    }


    fun getBigDecimal(key: String?): BigDecimal {
        val value = get(key) as BigDecimal?
        return if (Objects.isNull(value)) DEFAULT_DECIMAL else value!!
    }

    companion object {
        private const val DEFAULT_STRING = ""
        private const val DEFAULT_INT = 0
        private const val DEFAULT_DOUBLE = 0.0
        private val DEFAULT_DECIMAL: BigDecimal = BigDecimal.ZERO
        private val DEFAULT_DATE: LocalDate = LocalDate.now()
        private val DEFAULT_DATE_TIME: LocalDateTime = LocalDateTime.now()
    }
}
