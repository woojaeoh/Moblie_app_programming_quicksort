package com.example.quicksort.utils

object CarbonCalculator {

    // 품목별 CO₂ 절감량 (kg CO₂eq)
    private val carbonReductionMap = mapOf(
        "캔류" to 0.105,
        "고철류" to 8.100,
        "페트병" to 0.036,
        "플라스틱" to 0.060,
        "스티로폼" to 0.010,
        "비닐류" to 0.006,
        "유리병류" to 0.090,
        "종이류" to 0.500,
        "의류" to 1.000,
        "전자제품" to 3.500,
        "형광등" to 0.150
    )

    /**
     * 카테고리에 따른 CO₂ 절감량 반환
     * @param category 분리수거 카테고리
     * @return 해당 카테고리의 CO₂ 절감량 kg (없으면 기본 0.0kg)
     */
    fun calculateCarbon(category: String): Double {
        return carbonReductionMap[category] ?: 0.0
    }

    /**
     * 모든 카테고리와 CO₂ 절감량 목록 가져오기
     */
    fun getAllCarbonReductions(): Map<String, Double> {
        return carbonReductionMap
    }

    /**
     * 카테고리가 유효한지 확인
     */
    fun isValidCategory(category: String): Boolean {
        return carbonReductionMap.containsKey(category)
    }
}
