package com.spyglass.aer.welltransfers

import de.neuland.jade4j.Jade4J
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val submissionDate = "2016-06-16"

        val model = HashMap<String, Any>()
        model.put("submissionDate", submissionDate)

        val xml = Jade4J.render("./src/main/resources/transfer_document.jade", model)

        println(xml)
    }
}