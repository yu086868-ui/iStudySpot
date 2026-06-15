package com.example.scylier.istudyspot.models.studyroom

data class StudyRoomGuideSummary(
    val studyRoomId: Long,
    val studyRoomName: String,
    val address: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val description: String? = null
) {
    val openingHours: String
        get() = "${openTime ?: ""}-${closeTime ?: ""}".trim('-')
}

data class StudyRoomGuideDetail(
    val studyRoomId: Long,
    val studyRoomName: String,
    val address: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val description: String? = null,
    val contactInfo: String,
    val learningAreas: String,
    val convenienceFacilities: String,
    val transportationGuide: String
) {
    val openingHours: String
        get() = "${openTime ?: ""}-${closeTime ?: ""}".trim('-')
}
