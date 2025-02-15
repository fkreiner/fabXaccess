package cloud.fabx.service

import cloud.fabx.db.DbHandler.dbQuery
import cloud.fabx.dto.DeviceDto
import cloud.fabx.dto.EditDeviceDto
import cloud.fabx.dto.NewDeviceDto
import cloud.fabx.model.Device
import cloud.fabx.model.Devices

class DeviceService {

    private val toolService = ToolService()

    suspend fun getAllDevices(): List<DeviceDto> = dbQuery {
        Device.all().map{ toDeviceDto(it) }.toCollection(ArrayList())
    }

    suspend fun getDeviceById(id: Int): DeviceDto? = dbQuery {
        Device.findById(id)?.let { toDeviceDto(it) }
    }

    suspend fun getDeviceByMac(mac: String): DeviceDto? = dbQuery {
        Device.find { Devices.mac eq mac }.firstOrNull()?.let { toDeviceDto(it) }
    }

    suspend fun createNewDevice(device: NewDeviceDto): DeviceDto = dbQuery {
        val newDevice = Device.new {
            name = device.name
            mac = device.mac
            secret = device.secret
            bgImageUrl = device.bgImageUrl
            backupBackendUrl = device.backupBackendUrl
        }

        toDeviceDto(newDevice)
    }

    suspend fun editDevice(id: Int, editDevice: EditDeviceDto) = dbQuery {
        val device = Device.findById(id) ?: throw IllegalArgumentException("Device with id $id does not exist")

        editDevice.name?.let { device.name = it }
        editDevice.mac?.let { device.mac = it }
        editDevice.secret?.let { device.secret = it }
        editDevice.bgImageUrl?.let { device.bgImageUrl = it }
        editDevice.backupBackendUrl?.let { device.backupBackendUrl = it }
    }

    suspend fun deleteDevice(id: Int) = dbQuery {
        val device = Device.findById(id) ?: throw IllegalArgumentException("Device with id $id does not exist")
        device.delete()
    }

    private fun toDeviceDto(device: Device): DeviceDto {
        return DeviceDto(
            device.id.value,
            device.name,
            device.mac,
            device.secret,
            device.bgImageUrl,
            device.backupBackendUrl,
            device.tools.map { toolService.toToolDto(it) }.toCollection(ArrayList())
        )
    }

    suspend fun checkDeviceCredentials(mac: String, secret: String): Boolean = dbQuery {
        val device = Device.find {
            Devices.mac eq mac
        }.firstOrNull()

        if (device != null) {
            device.secret == secret
        } else {
            Device.new {
                this.name = "new device $mac"
                this.mac = mac
                this.secret = secret
                this.bgImageUrl = ""
                this.backupBackendUrl = ""
            }

            true
        }
    }
}