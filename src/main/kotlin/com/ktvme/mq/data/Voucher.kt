@file:Suppress("MemberVisibilityCanBePrivate")

package com.ktvme.mq.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.ktvme.mq.util.DATE_FORMAT
import com.ktvme.mq.util.Util.parseDate
import com.ktvme.mq.util.Util.yuan2Fen
import java.math.BigDecimal
import java.util.*

/**
 *  @author fan wu 编写于 2018/2/2.
 */

data class Voucher(
    val checkout_voucher: String, //K150528810
    val voucher: String, //R15052800040
    val voucher_type: Int, //1
    val voucher_info: String, //包厢单
    val business_date: String, //营业日期 yyyy-MM-dd
    val open_datetime: String, //2018-01-08 12:00:00
    val close_datetime: String, //2018-01-08 14:00:00
    val account_start_datetime: String, //2018-01-08 12:04:21
    val account_end_datetime: String, //2018-01-08 13:53:00
    val checkout_datetime: String, //2018-01-08 13:52:00
    val checkout_charge: String, //39800
    val fact_charge: Long, //39800
    val room_info: RoomInfo,
    val guest_info: GuestInfo?,
    val charges: List<Charge>,
    val payments: List<Payment>,
    val date: Date = business_date.parseDate(DATE_FORMAT),
    val companyCode: String?,
    val updateAt: Date = Date(),
    /**
         * 新增版本号字段
         */
        val version: Int?,
        //作废标记 0 正常 1作废 2 预买转后结 3 并房挂起
    @JsonProperty(value = "revoke", defaultValue = "0") val revoke: Int = 0,
        //反结账标记 0 正常 1 反结账 2 账单还原
    @JsonProperty(value = "modify", defaultValue = "0") val modify: Int = 0,
        //账单新增人员标识
    @JsonProperty(value = "saleman_staff_code", defaultValue = "") val saleMainStaffCode: String = "",
    @JsonProperty(value = "box_waiter_staff_code", defaultValue = "") val boxWaiterStaffCode: String = "",
    /**
         * 赢娱新增字段，用于标识上面 saleman_staff_code 对应的人员账单业绩金额字段，单位为分
         */
        @JsonProperty(value = "saleman_performance", defaultValue = "0") val saleManPerformance: Long = 0L,
    /**
         * 订位人 staff code
         */
        @JsonProperty(value = "reserve_staff_code", defaultValue = "") val reserveStaffCode: String? = "",
    /**
         * 第二订位人 staff Code
         */
        @JsonProperty(value = "sec_reserve_staff_code", defaultValue = "") val secReserveStaffCode: String? = "",
    /**
         * 新增开台单 ID，可空，默认值 -1 
         */
        @JsonProperty(value= "open_checkout_id", defaultValue = "-1") val checkoutId: Int? = -1
)

data class GuestInfo(
        val customer_id: String?, //3123131231441421321
        val name: String?, //7888
        val phone: String?, //15080456603
        val grade_id: Int?, //1
        val grade_name: String? //男女卡
)

data class Charge(
        val name: String, //包厢费
        val charge: Long, //20000
        val type: Int, //1
        val details: List<Material>?
) {
    object Helper {
        /**
         * 生成随机支付项
         * @param factCharge 账单实收金额
         * @param offsetMoney 折扣金额
         */
        fun randomCharge(factCharge: Long, offsetMoney: Long): List<Charge> {
            val material = Material(
                    material_name = "人头马路易13",
                    material_id = "1",
                    type = 1,
                    type_name = "洋酒",
                    num = BigDecimal.ONE,
                    price = factCharge.toInt(),
                    spec = "瓶",
                    money = factCharge,
                    sale_type = 0,
                    present_status = 0,
                    wine_list_id = 123,
                    ad_money = null,
                    presentman = null,
                    present_staffcode = null,
                    wine_datetime = "",
            )
            val charge = Charge("酒水费用", factCharge, 2, listOf(material))
            val charge2 = Charge(name = "折扣", charge = offsetMoney, type = 6, details = null)
            return listOf(charge, charge2)
        }
    }
}

data class Payment(
        val payment_type_id: Int, //1
        val payment_type_name: String, //现金
        val value: Long, //39800
        val by_member_card: Int = 0,//0
        /**
         * 是否为挂账
         */
        val by_debt: Int? = 0,
        val rate: String,
        val translatemoney: Long,
        val coupon_id: String?,
        /**
         * 会员卡支付本金金额，考虑旧版本数据，因此为可空字段
         */
        val member_cash: Long? = 0L,
        /**
         * 会员卡支付赠送金额，考虑旧版本数据，因此为可空字段
         */
        val member_present: Long? = 0L
) {
    object ModelMapper {
        fun from(map: Map<String, Any>): Payment = Payment(
                payment_type_id = map["payment_type_id"] as Int,
                payment_type_name = map["payment_type_name"].toString(),
                value = map["value"]?.toString()?.toLong() ?: 0L,
                rate = map["rate"].toString(),
                translatemoney = map["translatemoney"]?.toString()?.toLong() ?: 0L,
                by_member_card = map["by_member_card"] as? Int ?: 0,
                by_debt = map["by_debt"] as? Int ?: 0,
                coupon_id = map["coupon_id"] as? String,
                member_cash = map["member_cash"]?.toString()?.toLong() ?: 0L,
                member_present = map["member_present"]?.toString()?.toLong() ?: 0L
        )

        /**
         *  为叶斌的会员卡 Kafka 数据使用
         */
        fun fromMember(map: Map<String, Any>): Payment = Payment(
                payment_type_id = map["paymenttypeid"].toString().toInt(),
                payment_type_name = map["paymenttypename"].toString(),
                value = (map["value"]?.toString() ?: "0").yuan2Fen(),
                translatemoney = (map["translatemoney"]?.toString() ?: map["value"]?.toString() ?: "0").yuan2Fen(),
                coupon_id = null,
                by_member_card = 0,
                rate = map["rate"]?.toString() ?: "1",
                member_cash = map["member_cash"]?.toString()?.toLong() ?: 0L,
                member_present = map["member_present"]?.toString()?.toLong() ?: 0L
        )

        /**
         * 根据支付类型ID对支付数据进行合并
         */
        fun mergePaymentsByID(payments: List<Payment>): List<Payment> {
            return payments.groupBy { it.payment_type_id }
                    .entries.map { map ->
                        val paymentListWithSameID: List<Payment> = map.value
                        val money: Long = paymentListWithSameID.sumOf { it.translatemoney }
                        val memberCash: Long = paymentListWithSameID.sumOf { it.member_cash ?: 0L }
                        val memberPresent: Long = paymentListWithSameID.sumOf { it.member_present ?: 0L }
                        val paymentTypeId = map.key
                        val name = payments.first { it.payment_type_id == paymentTypeId }.payment_type_name
                        val byMemberCard = payments.first { it.payment_type_id == paymentTypeId }.by_member_card
                        val rate = payments.first { it.payment_type_id == paymentTypeId }.rate
                        Payment(
                                payment_type_id = paymentTypeId,
                                payment_type_name = name,
                                translatemoney = money,
                                value = money,
                                coupon_id = null,
                                by_member_card = byMemberCard,
                                rate = rate,
                                member_present = memberPresent,
                                member_cash = memberCash
                        )
                    }
        }

        /**
         * 生成随机支付项，仅用于测试
         */
        fun randomPayments(money: Long): List<Payment> {
            val pay = Payment(
                    payment_type_id = 1,
                    payment_type_name = "现金",
                    translatemoney = money,
                    value = money,
                    coupon_id = null,
                    by_member_card = 0,
                    rate = "1",
                    member_cash = money,
                    member_present = 0
            )
            return listOf(pay)
        }
    }
}

data class RoomInfo(
        val room_id: String?, //1
        val room_name: String?, //R001
        val room_type_id: Int?, //1
        val room_type_name: String? //小包
)

data class Material(
        val type: Int,
        val type_name: String,
        val material_id: String,
        val material_name: String,
        val num: BigDecimal,
        val spec: String,
        val price: Int,
        val money: Long,
        /**
         * 商家折后金额 ad = after discount
         */
        val ad_money: Long?,
        /**
         * 0 销售
         * 1 赠送
         * 2 配送
         * 3 例送
         * 4 套餐
         */
        val sale_type: Int,
        /**
         * 酒水类型：0 普通酒水 1 套餐主酒水 2 套餐子酒水
         */
        val present_status: Int,
        /**
         * 套餐子商品数量，仅用于酒水报表中的子商品统计
         */
        val presentNum: BigDecimal? = if (2 == present_status) {
            num
        } else {
            BigDecimal.ZERO
        },
        /**
         * 点单 ID，可以通过此 ID 来识别子物品属于哪个套餐点单
         */
        val wine_list_id: Int,
        /**
         * 操作人员，当sale_type=1时，此字段代表赠送人
         */
        val presentman: String?,
        /**
         *
         */
        val present_staffcode: String?,

        /**
         * 酒水单的下单时间
         */
        val wine_datetime: String?,
)
