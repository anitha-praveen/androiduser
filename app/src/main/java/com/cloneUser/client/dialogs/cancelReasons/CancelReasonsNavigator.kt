package com.cloneUser.client.dialogs.cancelReasons

import com.cloneUser.client.base.BaseViewOperator
import com.cloneUser.client.connection.responseModels.CancelReason

interface CancelReasonsNavigator : BaseViewOperator {
    fun close()
    fun sendCancelReason(reason : CancelReason.Reason)
    fun setCancelReason(reason : CancelReason.Reason)
}