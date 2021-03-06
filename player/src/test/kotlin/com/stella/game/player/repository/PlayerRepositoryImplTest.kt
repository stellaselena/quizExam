package com.stella.game.player.repository

import com.stella.game.player.domain.model.Player
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
class EntityRepositoryImplTest {

    @Autowired
    private lateinit var repo: PlayerRepository

    @Before
    fun setup() {
        Assert.assertEquals(0, repo.count())
    }

    @Test
    fun testCreatePlayer_Valid() {
        val player = getValidPlayers()[0]

        val savedId = repo.createPlayer(
                player.username,
                player.correctAnswers!!
        )

        Assert.assertEquals(1, repo.count())

        val foundPlayer = repo.findOne(savedId)
        foundPlayer.username = player.username
    }


    @Test
    fun testCreatePlayer_Invalid() {
        val player = getValidPlayers()[0]

        player.username = " "

        try {
            repo.createPlayer(
                    player.username,
                    player.correctAnswers!!
            )
            Assert.fail()
        } catch (e: Exception) {

        }
    }

    @Test
    fun testFindPlayerByUsername() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]

        createPlayer(player1)
        createPlayer(player2)

        Assert.assertEquals(2, repo.count())
        val playersFound1 = repo.findAllByUsername(player1.username)

        Assert.assertEquals(1, playersFound1.count())
        Assert.assertTrue(playersFound1.any({ e -> e.username == player1.username }))

    }

    @Test
    fun testUpdatePlayer() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]

        val savedId = createPlayer(player1)
        Assert.assertEquals(1, repo.count())

        val wasSuccessful = updatePlayer(player2, savedId)
        Assert.assertEquals(true, wasSuccessful)

        val readPlayer = repo.findOne(savedId)

        Assert.assertEquals(readPlayer?.username, player2.username)
        Assert.assertEquals(readPlayer?.id, savedId)

        Assert.assertEquals(1, repo.count())
    }

    @Test
    fun testChangeIdInUpdate_ShouldFail() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]

        val savedId = createPlayer(player1)

        player2.id = savedId * 2
        updatePlayer(player2, savedId)


        // Validate that id did not change
        val playerFound = repo.findOne(savedId * 2)
        Assert.assertNull(playerFound)
        Assert.assertEquals(1, repo.count())
    }

    @Test
    fun testDeletePlayer() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]

        val savedId1 = createPlayer(player1)
        val savedId2 = createPlayer(player2)

        Assert.assertEquals(2, repo.count())
        repo.delete(savedId1)

        Assert.assertEquals(1, repo.count())

        repo.delete(savedId2)
        Assert.assertEquals(0, repo.count())
    }

    @Test
    fun testDeleteWhenNoPlayerExists() {
        Assert.assertEquals(0, repo.count())
        try {
            repo.delete(2323)
            Assert.fail("Delete id that doesnt should throw exception")
        } catch (e: Exception) {

        }
        Assert.assertEquals(0, repo.count())
    }

    @Test
    fun testExistsByUsername() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]
        Assert.assertEquals(false, repo.existsByUsername(player1.username))

        createPlayer(player1)
        Assert.assertEquals(true, repo.existsByUsername(player1.username))

        Assert.assertEquals(false, repo.existsByUsername(player2.username))
    }

    @Test
    fun testFindAllByUsername() {
        val player1 = getValidPlayers()[0]
        val player2 = getValidPlayers()[1]

        createPlayer(player1)
        createPlayer(player2)

        val usersFound = repo.findAllByUsername(username = player1.username)

        Assert.assertEquals(1, usersFound.count())
    }

//                  Constraints


    @Test
    fun testUsernameConstraint() {
        var savedId = -1L
        val player = getValidPlayers()[0]
        player.username = "invalidUsername".repeat(20)

        try {
            savedId = createPlayer(player)
            Assert.fail()
        } catch (e: Exception) {}

        player.username = ""
        try {
            createPlayer(player)
            Assert.fail()
        } catch (e: Exception) {}

        Assert.assertEquals(-1, savedId)
    }

    fun getValidPlayers(): List<Player> {
        return listOf(
                Player(
                        "Stella",
                        0,
                        44
                ),
                Player(
                        "Athena",
                        1,
                        46
                ),
                Player(
                        "Stefan",
                        0,
                        96
                )
        )
    }


    fun createPlayer(player: Player): Long {
        val savedId = repo.createPlayer(
                player.username,
                player.correctAnswers!!
        )

        return savedId
    }


    fun updatePlayer(player: Player, id: Long): Boolean {
        return repo.updatePlayer(
                username = player.username,
                correctAnswers = 1,
                id = id
        )
    }
}