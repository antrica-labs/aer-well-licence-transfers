package com.spyglass.aer.welltransfers.entities.dds

import java.util.*

data class Licence(val licNum: String, val keepCurrentWellName: String = "Reject", val wellName: String, val sl: String = "", val workingInterests: LinkedList<WorkingInterestPartner> = LinkedList<WorkingInterestPartner>())