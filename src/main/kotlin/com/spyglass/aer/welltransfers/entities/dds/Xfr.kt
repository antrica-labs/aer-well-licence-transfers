package com.spyglass.aer.welltransfers.entities.dds

data class Xfr(val application: LicenceTransferApplication, val contact: LicenceTransferContact, val wells : List<Licence>)