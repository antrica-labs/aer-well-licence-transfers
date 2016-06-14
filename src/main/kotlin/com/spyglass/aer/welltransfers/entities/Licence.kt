package com.spyglass.aer.welltransfers.entities

data class Licence(val licNum: String, val keepCurrentWellName: String, val wellName: String, val sl: String, val workingInterests: List<WorkingInterestPartner>)