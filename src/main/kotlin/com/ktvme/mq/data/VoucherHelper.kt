package com.ktvme.mq.data

import com.ktvme.mq.util.Util.formatString
import java.util.*
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * 生成随机账单，仅用于测试
 */
object VoucherHelper {
    fun randomVoucher(
        companyCode: String, businessDate: Date, staffCode: String, boxId: String, boxName: String,
        sequence: Int = 1
    ): Voucher {
        val dateStr = businessDate.formatString("yyMMdd")
        val checkoutVoucher = "K${dateStr}0000-$sequence"
        val voucherNumber = "R${dateStr}0000-$sequence"
        val businessDateStr = businessDate.formatString("yyyy-MM-dd")
        val checkoutTime = "$businessDateStr 12:00:00"
        val openTime = "$businessDateStr 11:00:00"
        val closeTime = "$businessDateStr 23:59:59"
        val checkoutCharge = Random.nextInt(100, 20000)
        val factCharge: Long = (checkoutCharge * 0.8).roundToLong()
        val performance = (factCharge * 0.8).roundToLong()
        val guestInfo = GuestInfo(
            customer_id = Random.nextInt().toString(),
            name = "张三",
            phone = Random.nextLong(13300000000, 139999999999).toString(),
            grade_id = Random.nextInt(),
            grade_name = "鑫卡"
        )
        val roomTypeID = Random.nextInt()
        val roomInfo = RoomInfo(
            room_id = boxId,
            room_name = boxName,
            room_type_id = roomTypeID,
            room_type_name = if (roomTypeID % 2 == 0) "大床房" else "标准房"
        )
        val charges = Charge.Helper.randomCharge(factCharge, offsetMoney = (checkoutCharge * 0.2).roundToLong())
        val payments = Payment.ModelMapper.randomPayments(factCharge)
        return Voucher(
            companyCode = companyCode,
            business_date = businessDateStr,
            date = businessDate,
            updateAt = Date(),
            checkout_voucher = checkoutVoucher,
            voucher = voucherNumber,
            voucher_type = 1,
            voucher_info = "包厢单",
            open_datetime = openTime,
            close_datetime = closeTime,
            checkout_datetime = checkoutTime,
            account_start_datetime = openTime,
            account_end_datetime = closeTime,
            checkout_charge = checkoutCharge.toString(),
            fact_charge = factCharge,
            charges = charges,
            payments = payments,
            version = 1,
            guest_info = guestInfo,
            room_info = roomInfo,
            revoke = 0,
            modify = 0,
            saleMainStaffCode = staffCode,
            boxWaiterStaffCode = staffCode,
            saleManPerformance = performance
        )
    }
}
