package com.cocido.oficios.core.ui.gallery

data class GalleryMedia(
    val id: Long,
    val url: String?,
    val title: String? = null,
    val kind: GalleryMediaKind = url.toGalleryMediaKind(),
    val badge: String? = null,
)

enum class GalleryMediaKind { Image, Pdf }

data class MosaicSlice(
    val visibleCount: Int,
    val hiddenCount: Int,
) {
    val hasOverflow: Boolean get() = hiddenCount > 0
}

fun mosaicSlice(total: Int, previewLimit: Int?): MosaicSlice {
    if (total <= 0) return MosaicSlice(visibleCount = 0, hiddenCount = 0)
    val visible = (previewLimit ?: total).coerceIn(0, total)
    return MosaicSlice(visibleCount = visible, hiddenCount = total - visible)
}

fun String?.toGalleryMediaKind(): GalleryMediaKind {
    val path = orEmpty().substringBefore('?').substringBefore('#').lowercase()
    return if (path.endsWith(".pdf")) GalleryMediaKind.Pdf else GalleryMediaKind.Image
}

fun String?.isViewableImage(): Boolean =
    !isNullOrBlank() && toGalleryMediaKind() == GalleryMediaKind.Image
