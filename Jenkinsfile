#!groovy
library "atomSharedLibraries@newDirs"

@Library('atomSharedLibraries@newDirs')
import org.FileManager
import org.DirKeeper
import org.Constants
//@Library('atomSharedLibraries@newDirs') import org.fileManager.Route

def warningEcho(message) {
    echo "\033[1;33m${message}\033[0m"
}

def copyTerraformFiles(FileManager fileManager, DirKeeper dirKeeper) {
    // sh "cp provision/${params.orchestrator}/${params.orchestrator}.tf ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.orchestrator}.tf"
    // sh "cp provision/${params.orchestrator}/variables.tf ../${mainDirectoryName}/${params.Login}/${params.MachineName}/variables.tf"
    fileManager.copy("${params.orchestrator}.tf", dirKeeper.tfFilesDir(), dirKeeper.machineDir())
    fileManager.copy("variables.tf", dirKeeper.tfFilesDir(), dirKeeper.machineDir())
}

def copyDaemonFile(FileManager fileManager, DirKeeper dirKeeper) {
    //sh "cp provision/daemon.json ../${mainDirectoryName}/${params.Login}/${params.MachineName}/daemon.json"
    fileManager.copy("daemon.json", dirKeeper.provisionDir(), dirKeeper.machineDir())
}

def resolveInstancesYamlFile(FileManager fileManager, DirKeeper dirKeeper) {
    if ("${instances_yaml}" != "") {
        def instancesYaml = unstashParam "instances_yaml"
        //sh "mv ${instancesYaml} ../${mainDirectoryName}/${params.Login}/${params.MachineName}/template.yaml"
        fileManager.move("${instancesYaml}", "${dirKeeper.machineDir()}/template.yaml")
    } else {
        warningEcho("Didn't get instances yaml file. Using default file.")
        //sh "cp provision/template.yaml ../${mainDirectoryName}/${params.Login}/${params.MachineName}/template.yaml"
        fileManager.copy("template.yaml", dirKeeper.provisionDir(), dirKeeper.machineDir())
    }
}

def prepareMachineDirectory(FileManager fileManager, DirKeeper dirKeeper) {
    //sh "mkdir ../${mainDirectoryName}/${params.Login}/${params.MachineName}"
    fileManager.newDir(dirKeeper.machineDir())
    sh "chmod 777 provision/prepare_template"
    //sh "cp provision/prepare_template ../${mainDirectoryName}/${params.Login}/${params.MachineName}/prepare_template"
    fileManager.copy("prepare_template", dirKeeper.provisionDir(), dirKeeper.machineDir())
    copyTerraformFiles(fileManager, dirKeeper)
    copyDaemonFile(fileManager, dirKeeper)
    resolveInstancesYamlFile(fileManager, dirKeeper)
    prepareKeyFiles(fileManager, dirKeeper)
}

def prepareKeyFiles(FileManager fileManager, DirKeeper dirKeeper) {
    // This is a workaround!
    // https://bitbucket.org/janvrany/jenkins-27413-workaround-library
    if ("${sshpubkey}" != "" && "${sshprivkey}" != "") {
        def sshPubKeyFile = unstashParam "sshpubkey"
        def sshPrivKeyFile = unstashParam "sshprivkey"
        // sh "mv ${sshPubKeyFile} ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.pub"
        // sh "mv ${sshPrivKeyFile} ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.priv"
        fileManager.move("${sshPubKeyFile}", "${dirKeeper.machineDir()}/${params.Login}-key.pub")
        fileManager.move("${sshPrivKeyFile}", "${dirKeeper.machineDir()}/${params.Login}-key.priv")
        // sh "rm -rf ${sshPubKeyFile}"
        // sh "rm -rf ${sshPrivKeyFile}"
        //fileManager.del("${sshPubKeyFile}")
        //fileManager.del("${sshPrivKeyFile}")
    } else {
        warningEcho("Didn't get Public and Private SSH key. Using default keys.")
        // sh "cp provision/id_rsa.pub ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.pub"
        // sh "cp provision/id_rsa ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.priv"
        fileManager.copy("${dirKeeper.provisionDir()}/id_rsa.pub", "${dirKeeper.machineDir()}/${params.Login}-key.pub")
        fileManager.copy("${dirKeeper.provisionDir()}/id_rsa", "${dirKeeper.machineDir()}/${params.Login}-key.priv")
    }
    sh "chmod 600 ${dirKeeper.machineDir()}/${params.Login}-key.pub"
    sh "chmod 600 ${dirKeeper.machineDir()}/${params.Login}-key.priv"
}

pipeline {
    agent any

    options {
        ansiColor('xterm')
    }

    parameters {
        string(defaultValue: "", description: "", name: "Login")
        password(defaultValue: "", description: "", name: "Password")
        string(defaultValue: "default", description: "", name: "MachineName")
        choice(choices: ["--create", "--destroy"], description: "", name: "CreateDestroy")
        string(description: "", name: "branch", defaultValue: "${branch}")
        string(description: "", name: "routerID", defaultValue: "${routerID}")
        string(description: "", name: "routerName", defaultValue: "${routerName}")
        string(description: "", name: "routerIP", defaultValue: "${routerIP}")
        string(description: "", name: "networkID", defaultValue: "${networkID}")
        string(description: "", name: "networkName", defaultValue: "${networkName}")
        string(description: "", name: "projectName", defaultValue: "${projectName}")
        string(description: "", name: "ProjectID", defaultValue: "${ProjectID}")
        string(description: "", name: "domainName", defaultValue: "${domainName}")
        file(description: "sshpubkey", name: "sshpubkey")
        file(description: "sshprivkey", name: "sshprivkey")
        file(description: "instances_yaml", name: "instances_yaml")
        choice(choices: ["kubernetes", "openstack"], description: "", name: "orchestrator")
        choice(choices: ["vnc_api", "contrail-go"], description: "", name: "contrail_type")
        string(description: "", name: "flavor", defaultValue: "${flavor}")
        string(defaultValue: "master", description: "", name: "patchset_ref")
    }

    stages {
        stage('Main') {
            steps {
                script {
                    FileManager fileManager = ["${WORKSPACE}"]

                    DirKeeper dirKeeper = [
                        "../${Constants.mainDirectoryName}",
                        "../${Constants.mainDirectoryName}/${params.Login}",
                        "../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName}",
                        "provision",
                        "provision/${params.orchestrator}"
                    ]

                    if ("${params.CreateDestroy}" == "--create") {
                        deleteDir()
                        // Use the same repo and branch as was used to checkout Jenkinsfile:
                        retry(3) {
                            checkout scm
                        }
                        stash name: "Provision", includes: "provision/**"
                        unstash "Provision"

                        fileManager.newDir(dirKeeper.mainDir())
                        fileManager.newDir(dirKeeper.userDir())

                        if(fileManager.exists(dirKeeper.machineDir())){
                            error("It seems that there are actually resources with that name.\nPlease destroy them first or use if you just forgot about them. :)")
                        } else {
                            prepareMachineDirectory(fileManager, dirKeeper)
                            sh "set +x && cd provision && terraform init ../../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName} && ./createcontrail \"--create\" \"${params.Login}\" \"${params.Password}\" \"${params.MachineName}\" \"${params.ProjectID}\" \"${params.domainName}\" \"${params.projectName}\" \"${params.networkName}\" \"../../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.pub\" \"../../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.priv\" \"${params.routerIP}\" \"${params.orchestrator}\" \"${params.branch}\" \"${params.flavor}\" \"${Constants.terraformStateFileName}\" \"${Constants.mainDirectoryName}\" \"${params.contrail_type}\" \"${params.patchset_ref}\" && set -x"
                        }
                    } else {
                        // set +x and set -x are workaround to not print user password in jenkins output log
                        sh "set +x && cd provision && terraform init ../../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName} && ./createcontrail \"--destroy\" \"${params.Login}\" \"${params.Password}\" \"${params.MachineName}\" \"${params.ProjectID}\" \"${params.domainName}\" \"${params.projectName}\" \"${params.orchestrator}\" \"${Constants.terraformStateFileName}\" \"${Constants.mainDirectoryName}\" && set -x"
                        //sh "rm -rf ../${mainDirectoryName}/${params.Login}/${params.MachineName}"
                        fileManager.del(dirKeeper.machineDir())
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                FileManager fileManager = ["${WORKSPACE}"]

                    DirKeeper dirKeeper = [
                        "../${Constants.mainDirectoryName}",
                        "../${Constants.mainDirectoryName}/${params.Login}",
                        "../${Constants.mainDirectoryName}/${params.Login}/${params.MachineName}",
                        "provision",
                        "provision/${params.orchestrator}"
                    ]

                if ("${sshpubkey}" != "" && "${sshprivkey}" != "" && "${params.CreateDestroy}" == "--create") {
                    // sh "rm -rf ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.pub"
                    // sh "rm -rf ../${mainDirectoryName}/${params.Login}/${params.MachineName}/${params.Login}-key.priv"
                    fileManager.del("${dirKeeper.machineDir()}/${params.Login}-key.pub")
                    fileManager.del("${dirKeeper.machineDir()}/${params.Login}-key.priv")
                }
            }
        }
    }
}
