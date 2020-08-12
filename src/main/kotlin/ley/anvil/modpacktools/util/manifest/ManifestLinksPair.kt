package ley.anvil.modpacktools.util.manifest

import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.addonscript.wrapper.FileOrLink

data class ManifestLinksPair(
    var manifest: ManifestJSON? = null,
    var links: MutableMap<FileOrLink, String> = mutableMapOf()
)
