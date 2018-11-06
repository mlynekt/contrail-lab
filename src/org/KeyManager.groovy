#!groovy

package org

class KeyManager {
    KeyManager(String privKeyPath, String pubKeyPath, FileManager fileManager) {
        this.privKeyPath = privKeyPath
        this.pubKeyPath = pubKeyPath
        this.fileManager = fileManager
    }

    public void CopyKeysTo(String destination) {
        String pubKeyLocation = fileManager.GetFileLocation(this.pubKeyPath)
        String privKeyLocation = fileManager.GetFileLocation(this.privKeyPath)
        String pubKeyName = fileManager.GetFileName(this.pubKeyPath)
        String privKeyName = fileManager.GetFileName(this.privKeyPath)
        fileManager.Copy(pubKeyName, pubKeyName, pubKeyLocation, destination)
        fileManager.Copy(privKeyName, privKeyName, privKeyLocation, destination)
        this.pubKeyPath = destination + "/" + pubKeyName
        this.privKeyPath = destination + "/" + privKeyName
    }

    public void RenamePubKey(String name) {
        String oldName = fileManager.GetFileName(this.pubKeyPath)
        String location = fileManager.GetFileLocation(this.pubKeyPath)
        fileManager.Move(oldName, name, location, location)
        this.pubKeyPath = location + "/" + name
    }

    public void RenamePrivKey(String name) {
        String oldName = fileManager.GetFileName(this.privKeyPath)
        String location = fileManager.GetFileLocation(this.privKeyPath)
        fileManager.Move(oldName, name, location, location)
        this.privKeyPath = location + "/" + name
    }

    public void AddPermissions() {
        "chmod 600 ${this.privKeyPath}".execute()
        "chmod 600 ${this.pubKeyPath}".execute()
    }

    public boolean PrepareKeyFiles(String machineDir) {
        if (!this.KeysExist())
            return false
        this.CopyKeysTo(machineDir)
        this.RenamePubKey("id_rsa.pub")
        this.RenamePrivKey("id_rsa")
        this.AddPermissions()
        return true
    }

    public boolean KeysExist() {
        return this.privKeyPath != "" && this.pubKeyPath != ""
    }

    // TODO: Set terraform script to not require key file when executing
    // anything else than create method.
    public void CreateEmptyKeyFiles(String nameForPrivKey, String nameForPubKey, String destination) {
        fileManager.NewFile("${destination}/${nameForPrivKey}")
        fileManager.NewFile("${destination}/${nameForPubKey}")
        "chmod 600 ${destination}/${nameForPrivKey}".execute()
        "chmod 600 ${destination}/${nameForPubKey}".execute()
    }

    public String PrivKey() {
        return this.privKeyPath
    }

    public String PubKey() {
        return this.pubKeyPath
    }


    public void DeleteKeys() {
        fileManager.Del(this.privKeyPath)
        fileManager.Del(this.pubKeyPath)
        this.pubKeyPath = ""
        this.privKeyPath = ""
    }

    private String privKeyPath
    private String pubKeyPath
    private FileManager fileManager
}