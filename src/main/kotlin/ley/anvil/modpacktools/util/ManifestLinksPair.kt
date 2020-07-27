package ley.anvil.modpacktools.util

import ley.anvil.addonscript.curse.ManifestJSON

class ManifestLinksPair {

    var manifest: ManifestJSON? = null
    var links: MutableMap<String, String> = HashMap()

}