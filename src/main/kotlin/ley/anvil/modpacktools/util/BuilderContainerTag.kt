package ley.anvil.modpacktools.util

import j2html.tags.ContainerTag
import j2html.tags.DomContent

class BuilderContainerTag(tagName: String?) : ContainerTag(tagName) {
    companion object {
        fun html(block: BuilderContainerTag.() -> Unit) = BuilderContainerTag("html").apply {block()}
    }

    override fun with(child: DomContent?): BuilderContainerTag {
        super.with(child)
        return this
    }

    override fun withText(text: String?): BuilderContainerTag {
        super.withText(text)
        return this
    }

    @JvmOverloads
    operator fun String.invoke(block: (BuilderContainerTag.() -> Unit)? = null): BuilderContainerTag =
        BuilderContainerTag(this).apply {block?.invoke(this)}
            .apply {this@BuilderContainerTag.with(this)}

    operator fun String.invoke(content: String, block: (BuilderContainerTag.() -> Unit)? = null): BuilderContainerTag =
        BuilderContainerTag(this).withText(content)
            .apply {
                block?.invoke(this)
                this@BuilderContainerTag.with(this)
            }
}
