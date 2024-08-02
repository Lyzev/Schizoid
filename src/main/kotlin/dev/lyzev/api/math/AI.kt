/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.api.math

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import dev.lyzev.schizoid.Schizoid
import java.io.File

object AI {

    val PATH = "/${Schizoid.MOD_ID}/ai"

    fun loadWeights(filePath: String): Array<DoubleArray> {
        val weights = mutableListOf<DoubleArray>()
        val reader: CsvReader = csvReader {
            delimiter = ','
        }

        reader.open(javaClass.getResourceAsStream("$PATH/$filePath")!!) {
            readAllAsSequence().forEach { row ->
                weights.add(row.map { it.toDouble() }.toDoubleArray())
            }
        }
        return weights.toTypedArray()
    }

    fun loadBiases(filePath: String): DoubleArray {
        val biases = mutableListOf<Double>()
        val reader: CsvReader = csvReader {
            delimiter = ','
        }

        reader.open(javaClass.getResourceAsStream("$PATH/$filePath")!!) {
            readAllAsSequence().forEach { row ->
                row.forEach { biases.add(it.toDouble()) }
            }
        }
        return biases.toDoubleArray()
    }

    fun applyLayer(
        input: DoubleArray, weights: Array<DoubleArray>, biases: DoubleArray, useSigmoid: Boolean = false
    ): DoubleArray {
        val output = DoubleArray(biases.size)
        for (i in output.indices) {
            output[i] = biases[i]
            for (j in input.indices) {
                output[i] += input[j] * weights[j][i]
            }
            output[i] = if (useSigmoid) sigmoid(output[i]) else relu(output[i])
        }
        return output
    }
}
