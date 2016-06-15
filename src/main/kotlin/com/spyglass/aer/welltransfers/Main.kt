package com.spyglass.aer.welltransfers

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.spyglass.aer.welltransfers.entities.conf.SubmissionDetails
import com.spyglass.aer.welltransfers.entities.dds.*
import de.neuland.jade4j.JadeConfiguration
import java.io.FileReader
import java.io.PrintWriter
import java.sql.DriverManager
import java.sql.Statement
import java.util.*

object Main {
    val fullOwnershipQuery = """
        SELECT
          w.licence_number,
          w.well_name,
          CASE
            when d.partner_ba_code = 'A6J4' then 'A7AZ'
            else d.partner_ba_code
          end as partner_ba_code,
          case
            when d.partner_name = 'SPYGLASS RESOURCES CORP.' then 'SANLING ENERGY LTD.'
            else d.partner_name
          end as partner_name,
          d.working_interest
        FROM
          well_licences w INNER JOIN
          well_doi d
            ON d.well_licence_id = w.id
        WHERE
          d.partner_ba_code = 'A6J4' AND d.working_interest = 100
        ORDER BY
          w.licence_number
    """

    val partialOwnershipQuery = """
        SELECT
          w.licence_number,
          w.well_name,
          CASE
            when d.partner_ba_code = 'A6J4' then 'A7AZ'
            else d.partner_ba_code
          end as partner_ba_code,
          case
            when d.partner_name = 'SPYGLASS RESOURCES CORP.' then 'SANLING ENERGY LTD.'
            else d.partner_name
          end as partner_name,
          d.working_interest
        FROM
          well_licences w INNER JOIN
          well_doi d
            ON d.well_licence_id = w.id
        WHERE
          w.licence_number not in
          (
            SELECT
              w.licence_number
            FROM
              well_licences w INNER JOIN
              well_doi d
                ON d.well_licence_id = w.id
            WHERE
              d.partner_ba_code = 'A6J4' AND d.working_interest = 100
          )
        ORDER BY
          w.licence_number
    """

    @JvmStatic
    fun main(args: Array<String>) {
        val configFile = "./src/main/conf/transfer-config.json"
        val gson = Gson()

        val config =  gson.fromJson<SubmissionDetails>(FileReader(configFile))

        Class.forName("org.sqlite.JDBC")

        var connection = DriverManager.getConnection("jdbc:sqlite:./database/well_licences.db")
        var statement = connection.createStatement()

        val model = HashMap<String, Any>()

        model.put("submissionDate", config.submissionDate)
        model.put("fromContact", config.from)
        model.put("toContact", config.to)
        model.put("wells", getFullOwnershipLicences(statement))
        //model.put("wells", getPartialOwnershipLicences(statement))

        val jadeConfig = JadeConfiguration()
        val jadeTemplate = jadeConfig.getTemplate("./src/main/resources/transfer_document.jade")

        jadeConfig.isPrettyPrint = true

        val xml = jadeConfig.renderTemplate(jadeTemplate, model)

        val output = PrintWriter("./transfer_file.xml")

        output.print(xml)
        output.flush()
        output.close()
    }

    fun getFullOwnershipLicences(statement: Statement) : List<Licence> {
        val list = LinkedList<Licence>()

        val rs = statement.executeQuery(fullOwnershipQuery)

        while (rs.next()) {
            val licence = Licence(
                    licNum = rs.getString(1),
                    wellName = rs.getString(2)
            )

            val wi = WorkingInterestPartner(
                    baId = rs.getString(3),
                    name = rs.getString(4),
                    interest = rs.getFloat(5)
            )

            licence.workingInterests.add(wi)

            list.add(licence)
        }

        return list
    }

    fun getPartialOwnershipLicences(statement: Statement) : List<Licence> {
        val list = LinkedList<Licence>()

        val rs = statement.executeQuery(partialOwnershipQuery)

        var licence : Licence? = null
        while (rs.next()) {
            if (licence == null || licence.licNum != rs.getString(1)) {
                licence = Licence(
                        licNum = rs.getString(1),
                        wellName = rs.getString(2)
                )

                list.add(licence)
            }

            val wi = WorkingInterestPartner(
                    baId = rs.getString(3),
                    name = rs.getString(4),
                    interest = rs.getFloat(5)
            )

            licence!!.workingInterests.add(wi)
        }

        return list
    }
}
