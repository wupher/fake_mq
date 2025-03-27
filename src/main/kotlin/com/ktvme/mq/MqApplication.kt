package com.ktvme.mq

import com.fasterxml.jackson.databind.ObjectMapper
import com.ktvme.mq.data.Voucher
import com.ktvme.mq.data.VoucherHelper
import com.ktvme.mq.data.WjResponse
import com.ktvme.mq.util.SecurityUtil
import com.ktvme.mq.util.SecurityUtil.base64
import com.ktvme.mq.util.Util.parseDate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.random.Random

@RestController
@SpringBootApplication
class MqApplication {
	/**
	 * 默认生成 100 条数据
	 */
	private var randomNum = 100

	private val objectMapper = ObjectMapper()

	@PostMapping("/send_json")
	fun sendJson(@RequestBody body: Map<String, String>): WjResponse {
		val companyCode = body["companycode"] ?: return WjResponse.parameterError("companyCode is required")
		val dateString = body["date"] ?: return WjResponse.parameterError("date is required")
		val date: Date = dateString.parseDate("yyyy-MM-dd")
		val zip: Int = body["zip"]?.toInt() ?: 0 // 默认不压缩
		val randomVouchers: List<Voucher> = (1..randomNum).map { generateVoucher(companyCode, date) }
		if (0 == zip) {
			return WjResponse.ok(randomVouchers)
		}
		val json = objectMapper.writeValueAsString(randomVouchers)
		val zipJson = SecurityUtil.zip(json)
		val base6: String = zipJson.base64()
		return WjResponse.zipOk(base6, randomVouchers.size)
	}

	private fun generateVoucher(companyCode: String, date: Date): Voucher {
		val staffCode = Random.nextInt(100000, 999999).toString()
		val boxId = Random.nextInt(1000, 9999).toString()
		val boxName = "包厢${boxId}"
		return VoucherHelper.randomVoucher(companyCode, date, staffCode, boxId, boxName)
	}

	@PostMapping("/voucher_number/{value}")
	fun updateVoucherNumber(@PathVariable value: Int): WjResponse {
		randomNum = value
		return WjResponse(ret = 0, msg = "voucher number set to $value", data = randomNum)
	}

}

fun main(args: Array<String>) {
	runApplication<MqApplication>(*args)
}


