package com.ktvme.mq.data

data class WjResponse(
    val ret: Int, val msg: String, val data: Any? = null, val total: Int = 0
) {

    companion object {
        fun parameterError(msg: String): WjResponse = WjResponse(-1, msg)
        fun ok(vouchers: List<Voucher>): WjResponse = WjResponse(
            ret = 0,
            msg = "OK",
            data = vouchers,
            total = vouchers.size
        )

        fun zipOk(base64: String, total: Int): WjResponse = WjResponse(
            ret = 0,
            msg = "OK",
            data = base64,
            total = total
        )
    }

}
