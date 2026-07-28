package com.example.icaibatchchecker.data.remote

import com.example.icaibatchchecker.data.model.Batch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

object IcaiScraper {

    private const val URL = "https://www.icaionlineregistration.org/LaunchBatchDetail.aspx"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/137.0.0.0 Safari/537.36 Edg/137.0.0.0"

    data class AspNetFields(
        val viewState: String = "",
        val viewStateGenerator: String = "",
        val eventValidation: String = "",
        val eventTarget: String = "",
        val eventArgument: String = ""
    )

    private fun extractAspNetFields(html: String): AspNetFields {
        val doc = Jsoup.parse(html)
        val form = doc.select("form#form1").first()

        fun fieldVal(name: String): String {
            return form?.select("input[name=$name]")?.attr("value") ?: ""
        }

        return AspNetFields(
            viewState = fieldVal("__VIEWSTATE"),
            viewStateGenerator = fieldVal("__VIEWSTATEGENERATOR"),
            eventValidation = fieldVal("__EVENTVALIDATION"),
            eventTarget = fieldVal("__EVENTTARGET"),
            eventArgument = fieldVal("__EVENTARGUMENT")
        )
    }

    private fun buildFormBody(fields: AspNetFields, params: Map<String, String> = emptyMap()): FormBody {
        val builder = FormBody.Builder()
            .add("__VIEWSTATE", fields.viewState)
            .add("__VIEWSTATEGENERATOR", fields.viewStateGenerator)
            .add("__EVENTVALIDATION", fields.eventValidation)
            .add("__EVENTTARGET", fields.eventTarget)
            .add("__EVENTARGUMENT", fields.eventArgument)

        for ((key, value) in params) {
            builder.add(key, value)
        }

        return builder.build()
    }

    private fun get(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        return client.newCall(request).execute().use { response ->
            response.body?.string() ?: throw Exception("Empty response")
        }
    }

    private fun post(url: String, body: FormBody): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .post(body)
            .build()

        return client.newCall(request).execute().use { response ->
            response.body?.string() ?: throw Exception("Empty response")
        }
    }

    fun checkBatches(regionValue: String, pouText: String, courseValue: String): Result<List<Batch>> {
        return try {
            // Step 1: GET initial page
            val html1 = get(URL)
            var fields = extractAspNetFields(html1)

            // Step 2: POST with region to populate POU dropdown
            val body2 = buildFormBody(fields, mapOf("ddl_reg" to regionValue))
            val html2 = post(URL, body2)
            fields = extractAspNetFields(html2)

            // Find POU value
            val doc2 = Jsoup.parse(html2)
            val pouSelect = doc2.select("select#ddlPou").first()
                ?: return Result.failure(Exception("POU dropdown not found"))

            val pouValue = pouSelect.select("option")
                .firstOrNull { it.text().trim().equals(pouText, ignoreCase = true) }
                ?.attr("value")
                ?: return Result.failure(Exception("POU '$pouText' not found"))

            // Step 3: POST with region + POU
            val body3 = buildFormBody(fields, mapOf(
                "ddl_reg" to regionValue,
                "ddlPou" to pouValue
            ))
            val html3 = post(URL, body3)
            fields = extractAspNetFields(html3)

            // Step 4: POST with all fields + course to get batch list
            val body4 = buildFormBody(fields, mapOf(
                "ddl_reg" to regionValue,
                "ddlPou" to pouValue,
                "ddl_course" to courseValue,
                "btn_getlist" to "Get List"
            ))
            val html4 = post(URL, body4)

            // Parse results
            val doc4 = Jsoup.parse(html4)
            var rows = doc4.select("#upd_grid table tbody tr")
            if (rows.isEmpty()) {
                rows = doc4.select("#upd_grid table tr")
            }

            val batches = mutableListOf<Batch>()
            for (i in 1 until rows.size) {
                val cells = rows[i].select("td")
                if (cells.size >= 2) {
                    val seatsText = cells[1].text().trim().replace(",", "")
                    val seats = seatsText.toIntOrNull() ?: continue
                    val batchName = cells[0].text().trim()
                    batches.add(Batch(name = batchName, seats = seats))
                }
            }

            Result.success(batches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
