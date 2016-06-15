package com.spyglass.aer.welltransfers.entities.db

import java.util.*

data class WellLicenceRecord(var UWI: String, var licenceNumber: String, var wellName: String, val DOIs: LinkedList<WellDOIRecord> = LinkedList<WellDOIRecord>())