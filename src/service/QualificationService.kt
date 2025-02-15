package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.EditQualificationDto
import cloud.fabx.dto.NewQualificationDto
import cloud.fabx.dto.QualificationDto
import cloud.fabx.dto.ToolDto
import cloud.fabx.model.Device
import cloud.fabx.model.Devices
import cloud.fabx.model.Qualification
import cloud.fabx.model.User
import cloud.fabx.model.Users
import cloud.fabx.toolService
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.and

class QualificationService {

    suspend fun getAllQualifications(): List<QualificationDto> = dbQuery {
        Qualification.all().map { toQualificationDto(it) }.toCollection(ArrayList())
    }

    suspend fun getQualificationById(id: Int): QualificationDto? = dbQuery {
        Qualification.findById(id)?.let { toQualificationDto(it) }
    }

    suspend fun createNewQualification(qualification: NewQualificationDto) = dbQuery {
        val newQualification = Qualification.new {
            name = qualification.name
            description = qualification.description
            colour = qualification.colour
            orderNr = qualification.orderNr
        }

        toQualificationDto(newQualification)
    }

    suspend fun editQualification(id: Int, editQualification: EditQualificationDto) = dbQuery {
        val qualification = Qualification.findById(id)

        requireNotNull(qualification) { "Qualification with id $id does not exist" }

        editQualification.name?.let { qualification.name = it }
        editQualification.description?.let { qualification.description = it }
        editQualification.colour?.let { qualification.colour = it }
        editQualification.orderNr?.let { qualification.orderNr = it }
    }

    suspend fun deleteQualification(id: Int) = dbQuery {
        val qualification = Qualification.findById(id) ?: throw IllegalArgumentException("Qualification with id $id does not exist")
        qualification.delete()
    }

    suspend fun addUserQualification(userId: Int, qualificationId: Int) = dbQuery {
        val user = User.findById(userId)
        val qualification = Qualification.findById(qualificationId)

        requireNotNull(user) { "User with id $userId does not exist" }
        requireNotNull(qualification) { "Qualification with id $qualificationId does not exist" }

        val newQualifications = user.qualifications.toCollection(ArrayList())
        newQualifications.add(qualification)

        user.qualifications = SizedCollection(newQualifications)
    }

    suspend fun removeUserQualification(userId: Int, qualificationId: Int) = dbQuery {
        val user = User.findById(userId)
        val qualification = Qualification.findById(qualificationId)

        requireNotNull(user) { "User with id $userId does not exist" }
        requireNotNull(qualification) { "Qualification with id $qualificationId does not exist" }

        val newQualifications = user.qualifications.toCollection(ArrayList())
        val success = newQualifications.remove(qualification)

        require(success) { "User never had qualification ${qualification.id}/${qualification.name}" }

        user.qualifications = SizedCollection(newQualifications)
    }

    suspend fun getQualifiedToolsForCardId(deviceMac: String, cardId: String, cardSecret: String): List<ToolDto> = dbQuery {
        val user = User.find { (Users.cardId eq cardId) and (Users.cardSecret eq cardSecret) }.firstOrNull()
        requireNotNull(user) { "User with cardId $cardId and cardSecret $cardSecret does not exist" }

        val device = Device.find { Devices.mac eq deviceMac }.firstOrNull()
        requireNotNull(device) { "Device with mac $deviceMac does not exist" }

        device.tools.filter { tool ->
            tool.qualifications.all { user.qualifications.contains(it) }
        }.map { toolService.toToolDto(it) }
    }

    fun toQualificationDto(qualification: Qualification): QualificationDto {
        return QualificationDto(
            qualification.id.value,
            qualification.name,
            qualification.description,
            qualification.colour,
            qualification.orderNr
        )
    }
}