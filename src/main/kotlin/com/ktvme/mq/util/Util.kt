package com.ktvme.mq.util

import java.text.SimpleDateFormat
import java.util.Date

const val DATE_FORMAT = "yyyy-MM-dd"
object Util {
    /**
     * 修改基于SDF的实现避免 2024060433312731332234323c32343e3939 这样的字符串
     */
    fun String.parseDate(pattern: String = "yyyy-MM-dd HH:mm:ss"): Date {
        val sdf = SimpleDateFormat(pattern)
        sdf.isLenient = false
        return try {
            sdf.parse(this)
        } catch (ex: Exception) {
            val msg = "日期格式转换失败，日期：$this，格式：$pattern, 出现异常: ${ex.localizedMessage}"
            throw IllegalArgumentException(msg)
        }

    }

    /**
     * 元分转换的 Long 版本
     */
    fun String.yuan2Fen(): Long {
        if ("0" == this) {
            return 0L
        }

        val index = this.indexOf('.')

        if (index < 0) {
            return this.toLong() * 100
        }
        //两位小数
        if (index == this.length - 3 && index > 0) {
            val fen = StringBuilder(this).deleteCharAt(index).toString()
            return fen.toLong()
        }
        //1位小数
        if (index == this.length - 2 && index > 0) {
            val fen = StringBuilder(this).deleteCharAt(index).toString()
            return fen.toLong() * 10
        }

        //3 位或更多小数，去尾，递归
        val newString = this.substring(0, this.length - 1)
        return newString.yuan2Fen()
    }

    fun Date.formatString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String = SimpleDateFormat(pattern).format(this)
}