package io.mkg20001.nixosimage.install

interface ImageInstallMethod {
    public fun isAvailable(): Boolean
    public fun installImage (image: String)
    public fun needsCleanup(): Boolean
    public fun doCleanup()
}