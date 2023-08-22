package com.example.privaseektv2

class DataRepository private constructor() {

    private val dataList: MutableList<String> = mutableListOf()

    companion object {
        @Volatile
        private var instance: DataRepository? = null

        fun getInstance(): DataRepository {
            return instance ?: synchronized(this) {
                instance ?: DataRepository().also { instance = it }
            }
        }
    }

    fun addOrUpdateData(newData: String) {
        // Check if newData already exists, and if it does, replace it
        val index = dataList.indexOfFirst { it == newData }
        if (index >= 0) {
            dataList[index] = newData
        } else {
            dataList.add(newData)
        }
    }

    fun getAllData(): List<String> {
        return dataList.toList()
    }
}
