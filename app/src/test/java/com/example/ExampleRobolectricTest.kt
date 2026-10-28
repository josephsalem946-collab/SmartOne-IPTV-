package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SmartOneRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SmartOne IPTV", appName)
  }

  @Test
  fun `verify support email configuration`() {
    assertEquals("supportdisponible@gmail.com", SmartOneRepository.SUPPORT_EMAIL)
  }

  @Test
  fun `verify card payment is blocked by default`() {
    val isCardBlocked = SmartOneRepository.isGatewayBlocked("google_play_stripe")
    assertEquals(true, isCardBlocked)
  }

  @Test
  fun `verify toggle payment gateway blocking`() {
    SmartOneRepository.togglePaymentGatewayStatus("moncash")
    assertEquals(true, SmartOneRepository.isGatewayBlocked("moncash"))
    SmartOneRepository.togglePaymentGatewayStatus("moncash")
    assertEquals(false, SmartOneRepository.isGatewayBlocked("moncash"))
  }

  @Test
  fun `verify admin client canPayForOthers toggle`() {
    val initialClient = SmartOneRepository.adminClients.value.first { it.id == "u1" }
    val initialStatus = initialClient.canPayForOthers
    SmartOneRepository.toggleClientPayForOthers("u1")
    val updatedStatus = SmartOneRepository.adminClients.value.first { it.id == "u1" }.canPayForOthers
    assertEquals(!initialStatus, updatedStatus)
  }

  @Test
  fun `verify paying subscription for third party client`() {
    val plan = SmartOneRepository.subscriptionPlans.first { it.id == "plan_1y" }
    val success = SmartOneRepository.paySubscriptionForThirdParty(
      recipientIdentifier = "marie.c@gmail.com",
      plan = plan,
      payerName = "Test Admin",
      deductFromBalance = false
    )
    assertEquals(true, success)
    val client = SmartOneRepository.adminClients.value.first { it.email == "marie.c@gmail.com" }
    assertEquals("plan_1y", client.planId)
    val lastThirdParty = SmartOneRepository.thirdPartySubscriptions.value.first()
    assertEquals("marie.c@gmail.com", lastThirdParty.recipientIdentifier)
  }
}
