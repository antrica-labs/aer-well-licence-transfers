package com.spyglass.aer.welltransfers

import com.spyglass.aer.welltransfers.entities.db.WellDOIRecord
import com.spyglass.aer.welltransfers.entities.db.WellLicenceRecord
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import java.sql.DriverManager
import java.sql.Statement
import java.util.*

object Import {
    @JvmStatic
    fun main(args: Array<String>) {
        val licences = getLicences("C:\\Users\\stwogood\\Desktop\\WellTransferList.csv")

        populateDatabase(licences)
    }

    fun getLicences(filename : String) : List<WellLicenceRecord> {
        val licences = LinkedList<WellLicenceRecord>()

        val input = FileReader(filename)
        val records = CSVFormat.EXCEL.withHeader(ImportHeaders::class.java).parse(input)

        var row = 0
        var current: WellLicenceRecord? = null

        for (record in records) {
            row++

            if (row == 1) continue

            if (current == null || current.licenceNumber != record.get(ImportHeaders.LicenceNumber)) {
                current = WellLicenceRecord(
                        UWI = record.get(ImportHeaders.UWI),
                        licenceNumber = record.get(ImportHeaders.LicenceNumber),
                        wellName = record.get(ImportHeaders.WellName)
                )

                licences.add(current)
            }

            val doi = WellDOIRecord(
                    partnerName = record.get(ImportHeaders.Partner),
                    partnerBACode = record.get(ImportHeaders.PartnerBACode),
                    workingInterest = record.get(ImportHeaders.PartnerPercent).toFloat()
            )

            current.DOIs.add(doi)
        }

        records.close()
        input.close()

        return licences
    }

    fun populateDatabase(licences : List<WellLicenceRecord>) {
        val wellInsert = "INSERT INTO well_licences (uwi, licence_number, well_name) values (?, ?, ?)"
        val doiInsert = "INSERT INTO well_doi (well_licence_id, partner_name, partner_ba_code, working_interest) values (?, ?, ?, ?)"

        Class.forName("org.sqlite.JDBC")

        var connection = DriverManager.getConnection("jdbc:sqlite:./database/well_licences.db")

        val wellStatement = connection.prepareStatement(wellInsert, Statement.RETURN_GENERATED_KEYS)
        val doiStatement = connection.prepareStatement(doiInsert)

        for (licence in licences) {
            wellStatement.setString(1, licence.UWI)
            wellStatement.setString(2, licence.licenceNumber)
            wellStatement.setString(3, licence.wellName)

            wellStatement.executeUpdate()

            val rs = wellStatement.generatedKeys

            if (rs.next()) {
                val id = rs.getInt(1)

                doiStatement.setInt(1, id)

                for  (doi in licence.DOIs) {
                    doiStatement.setString(2, doi.partnerName)
                    doiStatement.setString(3, doi.partnerBACode)
                    doiStatement.setFloat(4, doi.workingInterest)

                    doiStatement.addBatch()
                }

                doiStatement.executeBatch()
            }

            rs.close()

            println("Inserted licence number ${licence.licenceNumber}")
        }

        wellStatement.close()
        doiStatement.close()

        connection.close()
    }

}