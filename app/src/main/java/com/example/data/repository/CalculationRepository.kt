package com.example.data.repository

import com.example.data.local.CalculationDao
import com.example.data.local.CalculationEntity
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val history: Flow<List<CalculationEntity>> = dao.getAllCalculations()

    suspend fun saveCalculation(population: Long, marginPercent: Double, rawResult: Double, roundedResult: Long) {
        val entity = CalculationEntity(
            population = population,
            marginOfErrorPercent = marginPercent,
            rawResult = rawResult,
            roundedResult = roundedResult
        )
        dao.insertCalculation(entity)
    }

    suspend fun deleteCalculation(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
