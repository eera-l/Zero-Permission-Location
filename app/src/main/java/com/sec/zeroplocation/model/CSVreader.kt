package com.sec.zeroplocation.model

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

class CSVreader {

    public fun readWithHeader(file : String) : List<Map<String, String>> {
        val rows: List<Map<String, String>> = csvReader().readAllWithHeader(file)
        return rows
    }
}