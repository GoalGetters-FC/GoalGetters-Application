package com.ggetters.app.core

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.TeamComposition
import com.ggetters.app.data.model.TeamDenomination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CombinedTeamRepositoryTest {

    @Test
    fun `team creation with defaults`() {
        val team = Team()

        assertNotNull(team.id)
        assertNotNull(team.createdAt)
        assertNotNull(team.updatedAt)
        assertNull(team.stainedAt)
        assertEquals("", team.name)
        assertEquals(TeamComposition.UNISEX_MALE, team.composition)
        assertEquals(TeamDenomination.OPEN, team.denomination)
        assertFalse(team.isActive)
    }

    @Test
    fun `team creation with required fields`() {
        val team = Team(
            name = "Warriors FC",
            code = "WAFC123"
        )

        assertNotNull(team.id)
        assertEquals("Warriors FC", team.name)
        assertEquals("WAFC123", team.code)
    }

    @Test
    fun `team has default timestamps`() {
        val team = Team(
            name = "Eagles United"
        )

        assertNotNull(team.createdAt)
        assertNotNull(team.updatedAt)
        assertNull(team.stainedAt)
    }

    @Test
    fun `team with optional fields`() {
        val team = Team(
            name = "Phoenix FC",
            alias = "PHX",
            description = "Youth soccer team",
            yearFormed = "2020",
            contactCell = "+27123456789",
            contactMail = "info@phoenixfc.com",
            clubAddress = "123 Stadium Road"
        )

        assertEquals("Phoenix FC", team.name)
        assertEquals("PHX", team.alias)
        assertEquals("Youth soccer team", team.description)
        assertEquals("2020", team.yearFormed)
        assertEquals("+27123456789", team.contactCell)
        assertEquals("info@phoenixfc.com", team.contactMail)
        assertEquals("123 Stadium Road", team.clubAddress)
    }

    @Test
    fun `team with different compositions`() {
        val unisexMale = Team(
            name = "Team A",
            composition = TeamComposition.UNISEX_MALE
        )

        val unisexFemale = Team(
            name = "Team B",
            composition = TeamComposition.UNISEX_FEMALE
        )


        assertEquals(TeamComposition.UNISEX_MALE, unisexMale.composition)
        assertEquals(TeamComposition.UNISEX_FEMALE, unisexFemale.composition)

    }

    @Test
    fun `team with different denominations`() {
        val open = Team(
            name = "Team A",
            denomination = TeamDenomination.OPEN
        )



        assertEquals(TeamDenomination.OPEN, open.denomination)

    }

    @Test
    fun `team active status`() {
        val inactiveTeam = Team(
            name = "Inactive Team",
            isActive = false
        )

        val activeTeam = Team(
            name = "Active Team",
            isActive = true
        )

        assertFalse(inactiveTeam.isActive)
        assertEquals(true, activeTeam.isActive)
    }

    @Test
    fun `team with unique code`() {
        val team1 = Team(
            name = "Team Alpha",
            code = "ALPHA123"
        )

        val team2 = Team(
            name = "Team Beta",
            code = "BETA456"
        )

        assertEquals("ALPHA123", team1.code)
        assertEquals("BETA456", team2.code)
    }

    @Test
    fun `team without optional fields`() {
        val team = Team(
            name = "Simple Team"
        )

        assertNull(team.alias)
        assertNull(team.description)
        assertNull(team.yearFormed)
        assertNull(team.contactCell)
        assertNull(team.contactMail)
        assertNull(team.clubAddress)
        assertNull(team.code)
    }

    @Test
    fun `team name can be updated`() {
        val team = Team(
            name = "Original Name"
        )

        assertEquals("Original Name", team.name)

        team.name = "Updated Name"

        assertEquals("Updated Name", team.name)
    }
}