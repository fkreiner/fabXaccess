package cloud.fabx

import cloud.fabx.db.DbHandler
import cloud.fabx.model.*
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.util.InternalAPI
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.encodeBase64
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before

@KtorExperimentalAPI
open class CommonTest {
    @Before
    fun before() = transaction(DbHandler.db) {
        println("BeforeEach")

        SchemaUtils.drop(Devices)
        SchemaUtils.drop(Tools)
        SchemaUtils.drop(Users)
        SchemaUtils.drop(Admins)
        SchemaUtils.drop(Qualifications)
        SchemaUtils.drop(UserQualifications)
        SchemaUtils.drop(ToolQualifications)

        SchemaUtils.create(Devices)
        SchemaUtils.create(Tools)
        SchemaUtils.create(Users)
        SchemaUtils.create(Admins)
        SchemaUtils.create(Qualifications)
        SchemaUtils.create(UserQualifications)
        SchemaUtils.create(ToolQualifications)

        Unit
    }

    @InternalAPI
    protected fun TestApplicationRequest.addBasicAuth(user: String, password: String) {
        val encoded = "$user:$password".toByteArray(Charsets.UTF_8).encodeBase64()
        addHeader(HttpHeaders.Authorization, "Basic $encoded")
    }
}