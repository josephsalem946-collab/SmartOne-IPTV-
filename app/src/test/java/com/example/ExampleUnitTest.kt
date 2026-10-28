package com.example

import com.example.data.payment.OrderStatus
import com.example.data.payment.PaymentBackendService
import com.example.data.repository.SmartOneRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRepositoryInitialState() {
        assertNotNull(SmartOneRepository.channels.value)
        assertTrue(SmartOneRepository.channels.value.isNotEmpty())
        assertEquals(4, SmartOneRepository.subscriptionPlans.size)
    }

    @Test
    fun testDepositAndSubscriptionActivation() {
        val initialBalance = SmartOneRepository.balance.value
        val depositAmount = 3000.0
        val deposited = SmartOneRepository.depositFunds(depositAmount, "Natcash Online")
        assertTrue(deposited)
        assertEquals(initialBalance + depositAmount, SmartOneRepository.balance.value, 0.01)

        val plan1y = SmartOneRepository.subscriptionPlans.first { it.id == "plan_1y" }
        val activated = SmartOneRepository.activateSubscription(plan1y)
        assertTrue(activated)
        assertNotNull(SmartOneRepository.activeSubscription.value)
        assertEquals("plan_1y", SmartOneRepository.activeSubscription.value?.planId)

        // Test auto-cancellation of previous plan when activating a new one
        val plan1m = SmartOneRepository.subscriptionPlans.first { it.id == "plan_1m" }
        val activatedNew = SmartOneRepository.activateSubscription(plan1m)
        assertTrue(activatedNew)
        assertEquals("plan_1m", SmartOneRepository.activeSubscription.value?.planId)
    }

    @Test
    fun testPlanCurrencyConversions() {
        val plan = SmartOneRepository.subscriptionPlans.first { it.id == "plan_1m" }
        assertEquals(250.0, plan.priceGds, 0.01)
        assertTrue(plan.priceUsd > 1.8 && plan.priceUsd < 2.0)
        assertTrue(plan.priceEur > 1.7 && plan.priceEur < 1.9)
    }

    @Test
    fun testMonCashAndNatcashBackendCheckoutCreation() {
        // MonCash creation
        val mcResp = PaymentBackendService.createMonCashPayment("usr_test_123", 1000.0)
        assertTrue(mcResp.success)
        assertTrue(mcResp.checkoutUrl.startsWith("https://moncashbutton.digicelgroup.com"))
        assertTrue(mcResp.checkoutUrl.contains("order_id=${mcResp.orderId}"))

        // Natcash creation
        val ncResp = PaymentBackendService.createNatcashPayment("usr_test_123", 1500.0)
        assertTrue(ncResp.success)
        assertTrue(ncResp.checkoutUrl.startsWith("https://natpay.natcom.com.ht"))
        assertTrue(ncResp.checkoutUrl.contains("order_id=${ncResp.orderId}"))
    }

    @Test
    fun testAntiReplayProtectionOnWebhooks() {
        val resp = PaymentBackendService.createMonCashPayment("usr_anti_replay", 500.0)
        assertTrue(resp.success)

        val initialBalance = SmartOneRepository.balance.value

        // First webhook confirmation -> Success
        val firstResult = PaymentBackendService.simulateSuccessfulGatewayPayment(resp.orderId)
        assertTrue(firstResult.success)
        assertEquals(initialBalance + 500.0, SmartOneRepository.balance.value, 0.01)

        val order = PaymentBackendService.getOrder(resp.orderId)
        assertEquals(OrderStatus.COMPLETED, order?.status)

        // Second duplicate webhook confirmation -> REJECTED by anti-replay protection
        val secondResult = PaymentBackendService.simulateSuccessfulGatewayPayment(resp.orderId)
        assertFalse(secondResult.success)
        assertTrue(secondResult.isDuplicateReplay)
        // Ensure no duplicate crediting occurred!
        assertEquals(initialBalance + 500.0, SmartOneRepository.balance.value, 0.01)
    }

    @Test
    fun testNowPaymentsCryptoCreationAndIpnValidation() {
        val initialBalance = SmartOneRepository.balance.value

        // 1. Create NOWPayments payment for $20 USD
        val createResp = PaymentBackendService.createNowPaymentsCryptoPayment("usr_crypto_tester", 20.0, "usdttrc20")
        assertTrue(createResp.success)
        assertTrue(createResp.checkoutUrl.contains("nowpayments.io"))

        // 2. Simulate valid IPN notification with HMAC-SHA512
        val ipnResult = PaymentBackendService.simulateSuccessfulNowPaymentsPayment(createResp.orderId)
        assertTrue(ipnResult.success)
        assertTrue(SmartOneRepository.balance.value >= initialBalance + (20.0 * 132.0))

        // 3. Test duplicate IPN replay rejection
        val duplicateResult = PaymentBackendService.simulateSuccessfulNowPaymentsPayment(createResp.orderId)
        assertFalse(duplicateResult.success)
        assertTrue(duplicateResult.isDuplicateReplay)
    }

    @Test
    fun testStripeCheckoutSessionCreationAndWebhookValidation() {
        val initialBalance = SmartOneRepository.balance.value

        // 1. Create Stripe Checkout Session
        val resp = PaymentBackendService.createStripeCheckoutSession("usr_stripe_tester", 1500.0)
        assertTrue(resp.success)
        assertTrue(resp.checkoutUrl.contains("checkout.stripe.com"))

        // 2. Simulate webhook checkout.session.completed
        val webhookResult = PaymentBackendService.simulateSuccessfulStripePayment(resp.orderId)
        assertTrue(webhookResult.success)
        assertEquals(initialBalance + 1500.0, SmartOneRepository.balance.value, 0.01)

        val order = PaymentBackendService.getOrder(resp.orderId)
        assertEquals(OrderStatus.COMPLETED, order?.status)

        // 3. Anti-Replay protection: duplicate webhook must be rejected
        val duplicateWebhook = PaymentBackendService.simulateSuccessfulStripePayment(resp.orderId)
        assertFalse(duplicateWebhook.success)
        assertTrue(duplicateWebhook.isDuplicateReplay)
    }

    @Test
    fun testDailyRevenueModificationIncreasesMonthlyAndAnnualReports() {
        val initialRevenues = SmartOneRepository.dailyRevenues.value
        val initialAvg = initialRevenues.sumOf { it.amount } / initialRevenues.size
        val initialMonthly = initialAvg * 30.0
        val initialAnnual = initialMonthly * 12.0

        // Increase today's revenue by +5,000 HTG
        SmartOneRepository.increaseDailyRevenue("Aujourd'hui", 5000.0)

        val updatedRevenues = SmartOneRepository.dailyRevenues.value
        val updatedAvg = updatedRevenues.sumOf { it.amount } / updatedRevenues.size
        val updatedMonthly = updatedAvg * 30.0
        val updatedAnnual = updatedMonthly * 12.0

        assertTrue(updatedAvg > initialAvg)
        assertTrue(updatedMonthly > initialMonthly)
        assertTrue(updatedAnnual > initialAnnual)
    }

    @Test
    fun testRecentCommentsListingAndActions() {
        val comments = SmartOneRepository.recentComments.value
        assertTrue(comments.size >= 10)
        assertEquals(5, comments[0].rating)
        assertNotNull(comments[0].author)

        // Add a new comment
        SmartOneRepository.addComment("Testeur Joseph", "Superbe expérience!", 5)
        val afterAdd = SmartOneRepository.recentComments.value
        assertEquals("Testeur Joseph", afterAdd[0].author)

        // Delete test comment
        SmartOneRepository.deleteComment(afterAdd[0].id)
        val afterDel = SmartOneRepository.recentComments.value
        assertFalse(afterDel.any { it.author == "Testeur Joseph" })
    }

    @Test
    fun testConcurrentTwoDevicesLimit() {
        assertEquals(2, SmartOneRepository.MAX_CONCURRENT_DEVICES)

        // Initially 2 devices are connected
        val initialDevices = SmartOneRepository.connectedDevices.value
        assertEquals(2, initialDevices.size)

        // Attempting to connect a 3rd device must be rejected
        val connected3rd = SmartOneRepository.connectNewDevice("Android Box Chambre", "BOX")
        assertFalse(connected3rd)
        assertTrue(SmartOneRepository.deviceLimitExceeded.value)
        assertEquals(2, SmartOneRepository.connectedDevices.value.size)

        // Disconnecting a device frees up a slot
        val devToDisconnect = initialDevices.first { !it.isCurrentDevice }
        SmartOneRepository.disconnectDevice(devToDisconnect.deviceId)
        assertEquals(1, SmartOneRepository.connectedDevices.value.size)
        assertFalse(SmartOneRepository.deviceLimitExceeded.value)

        // Now connecting a 2nd device succeeds
        val connectedNew = SmartOneRepository.connectNewDevice("Tablette Salon", "TABLET")
        assertTrue(connectedNew)
        assertEquals(2, SmartOneRepository.connectedDevices.value.size)

        // Again, a 3rd device fails
        val connectedOverLimit = SmartOneRepository.connectNewDevice("PC Bureau", "WEB")
        assertFalse(connectedOverLimit)
        assertTrue(SmartOneRepository.deviceLimitExceeded.value)
    }
}
