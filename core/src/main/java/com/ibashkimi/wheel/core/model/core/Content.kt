package com.ibashkimi.wheel.core.model.core

sealed class Content {

    data class Text(val text: String) : Content()

    sealed class Media(open val uri: String, open val text: String?) : Content() {

        data class Image(override val uri: String, override val text: String?) : Media(uri, text)

        data class Video(override val uri: String, override val text: String?) : Media(uri, text)

        data class Animation(override val uri: String, override val text: String?) :
            Media(uri, text)
    }

    data class Unsupported(val type: String) : Content()

}