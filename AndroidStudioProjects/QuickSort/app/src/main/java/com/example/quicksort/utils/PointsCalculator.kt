package com.example.quicksort.utils

object PointsCalculator {

    private val categoryPoints = mapOf(
        "고철류" to 10,
        "캔류" to 9,
        "페트병" to 9,
        "유리병류" to 8,
        "종이류" to 7,
        "플라스틱" to 6,
        "비닐류" to 5,
        "스티로폼" to 4,
        "의류" to 4,
        "섬유류" to 4,
        "전자제품" to 8,
        "형광등" to 7
    )

    /**
     * 카테고리에 따른 점수 반환
     * @param category 분리수거 카테고리
     * @return 해당 카테고리의 점수 (없으면 기본 5점)
     */
    fun getPoints(category: String): Int {
        return categoryPoints[category] ?: 5  // 기본값 5점
    }

    /**
     * 모든 카테고리와 점수 목록 가져오기
     */
    fun getAllCategoryPoints(): Map<String, Int> {
        return categoryPoints
    }

    /**
     * 카테고리가 유효한지 확인
     */
    fun isValidCategory(category: String): Boolean {
        return categoryPoints.containsKey(category)
    }
}
