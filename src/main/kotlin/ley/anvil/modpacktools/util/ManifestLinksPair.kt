package ley.anvil.modpacktools.util

import ley.anvil.addonscript.curse.ManifestJSON
import ley.anvil.addonscript.wrapper.FileOrLink

class ManifestLinksPair {

    var manifest: ManifestJSON? = null
    var links: MutableMap<FileOrLink, String> = HashMap()

}