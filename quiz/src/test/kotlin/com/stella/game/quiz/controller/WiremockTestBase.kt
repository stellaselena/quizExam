package com.stella.game.quiz.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.stella.game.quiz.domain.model.Quiz
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.logging.Logger


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
abstract class WiremockTestBase {

    private val logger : Logger = Logger.getLogger(WiremockTestBase::class.java.canonicalName)

    companion object {
        lateinit var wiremockServerSubcategory: WireMockServer

        @BeforeClass
        @JvmStatic

        fun initClass() {
            RestAssured.baseURI = "http://localhost"
            RestAssured.port = 7083
            RestAssured.basePath = "/quizzes"
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

            wiremockServerSubcategory = WireMockServer(WireMockConfiguration.wireMockConfig().port(8082).notifier(ConsoleNotifier(true)))

            wiremockServerSubcategory.start()

        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            wiremockServerSubcategory.stop()
        }
    }

    @Before
    @After
    fun cleanDatabase() {

        val list = RestAssured.given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<Quiz>::class.java)
                .toList()


        list.stream().forEach {
            RestAssured.given().pathParam("id", it.id)
                    .delete("/{id}")
                    .then()
                    .statusCode(204)
        }

        RestAssured.given().get()
                .then()
                .statusCode(200)
                .body("size()", CoreMatchers.equalTo(0))
    }
}