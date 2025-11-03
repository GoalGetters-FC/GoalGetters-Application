package com.ggetters.app.core

import com.ggetters.app.data.model.BroadcastStatus
import org.junit.Assert
import org.junit.Test

class CombinedBroadcastStatusRepositoryTest {

    @Test
    fun `broadcast status creation with required fields`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        Assert.assertEquals("broadcast1", status.broadcastId)
        Assert.assertEquals("user1", status.recipientId)
    }

    @Test
    fun `broadcast status has default timestamps`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        Assert.assertNotNull(status.createdAt)
        Assert.assertNotNull(status.updatedAt)
        Assert.assertNull(status.stainedAt)
        Assert.assertNull(status.stashedAt)
        Assert.assertNull(status.noticedAt)
    }

    @Test
    fun `notice marks status as noticed`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        Assert.assertNull(status.noticedAt)

        status.notice()

        Assert.assertNotNull(status.noticedAt)
    }

    @Test
    fun `review clears noticed flag`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        status.notice()
        Assert.assertNotNull(status.noticedAt)

        status.review()

        Assert.assertNull(status.noticedAt)
    }

    @Test
    fun `stain marks status as read`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        Assert.assertNull(status.stainedAt)

        status.stain()

        Assert.assertNotNull(status.stainedAt)
    }

    @Test
    fun `unstain clears read flag`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        status.stain()
        Assert.assertNotNull(status.stainedAt)

        status.unstain()

        Assert.assertNull(status.stainedAt)
    }

    @Test
    fun `stash archives status`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        Assert.assertNull(status.stashedAt)

        status.stash()

        Assert.assertNotNull(status.stashedAt)
    }

    @Test
    fun `unstash un-archives status`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        status.stash()
        Assert.assertNotNull(status.stashedAt)

        status.unstash()

        Assert.assertNull(status.stashedAt)
    }

    @Test
    fun `multiple recipients for same broadcast`() {
        val status1 = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        val status2 = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user2"
        )

        Assert.assertEquals("broadcast1", status1.broadcastId)
        Assert.assertEquals("broadcast1", status2.broadcastId)
        Assert.assertEquals("user1", status1.recipientId)
        Assert.assertEquals("user2", status2.recipientId)
    }

    @Test
    fun `notice and stain can be independent`() {
        val status = BroadcastStatus(
            broadcastId = "broadcast1",
            recipientId = "user1"
        )

        status.notice()
        Assert.assertNotNull(status.noticedAt)
        Assert.assertNull(status.stainedAt)

        status.stain()
        Assert.assertNotNull(status.noticedAt)
        Assert.assertNotNull(status.stainedAt)
    }
}