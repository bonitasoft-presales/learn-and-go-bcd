@Library('github.com/bonitasoft-presales/bonita-jenkins-library@1.0.1') _

def version = "7.12.2"
def versionShortened = "7122"
def nodeName = "bcd-${versionShortened}"

node("${nodeName}") {
    def scenarioFile = "/home/bonita/bonita-continuous-delivery/scenarios/scenario-ec2.yml"
    def bonitaConfiguration = "Qualification"

    // used to archive artifacts
    def jobBaseName = "${env.JOB_NAME}".split('/').last()

    // used to set build description and bcd_stack_id
    def gitRepoName = "${env.JOB_NAME}".split('/')[1]
    def normalizedGitRepoName = gitRepoName.toLowerCase().replaceAll('-','_')

    // used to set bcd_stack_id
    def branchName = env.BRANCH_NAME
    def normalizedBranchName = branchName.toLowerCase().replaceAll('-','_')

    //bcd_stack_id overrides scenario value
    def stackName = "${normalizedGitRepoName}_${normalizedBranchName}_${versionShortened}"

    // set to true/false if bonitaConfiguration requires a .bconf file
    // e.g. configuration has parameters
    def useBConf = true

    // set to true/false to switch verbose mode
    def debugMode = false	

    def debug_flag = ''
    if ("${debugMode}".toBoolean()) {
        debug_flag = '-X'
    }
    
    def extraVars = "--extra-vars bcd_stack_id=${stackName} --extra-vars bonita_version=${version}"

  ansiColor('xterm') {
    timestamps {
    withCredentials([file(credentialsId: 'bcdVaultPasswordFile', variable: 'ANSIBLE_VAULT_PASSWORD_FILE')]) {
        stage("Checkout") {
            checkout scm
            echo "jobBaseName: $jobBaseName"
            echo "gitRepoName: $gitRepoName"
        }

        stage("Build LAs") {
            bcd scenario:scenarioFile, args: "${extraVars} livingapp build ${debug_flag} -p ${WORKSPACE} --environment ${bonitaConfiguration}"
        }

        stage("Create stack") {
            bcd scenario:scenarioFile, args: "${extraVars} stack create", ignore_errors: false
        }

		stage("Undeploy server") {
            bcd scenario:scenarioFile, args: "${extraVars} stack undeploy", ignore_errors: true
        }
        
        stage("Deploy server") {
            def json_path = pwd(tmp: true) + '/bcd_stack.json'
            bcd scenario:scenarioFile, args: "${extraVars} -e bcd_stack_json=${json_path} stack deploy"
            // set build description using bcd_stack.json file
            def props = readJSON file: json_path
            currentBuild.description = "<a href='${props.bonita_url}'>${props.bcd_stack_id}</a>"
        }

        stage('Deploy LAs') {
            println "looking for :" + "target/*_${jobBaseName}-${bonitaConfiguration}-*.zip"
            def zip_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.zip")
            println "zip file artifact: ${zip_files}"
            if (useBConf){
                def bconf_files = findFiles(glob: "target/*_${jobBaseName}-${bonitaConfiguration}-*.bconf")
                println "bconf file artifact: ${bconf_files}"
                bcd scenario:scenarioFile, args: "${extraVars} livingapp deploy ${debug_flag} -p ${WORKSPACE}/${zip_files[0].path} -c ${WORKSPACE}/${bconf_files[0].path} --development-mode"
            }
            else{
                bcd scenario:scenarioFile, args: "${extraVars} livingapp deploy ${debug_flag} -p ${WORKSPACE}/${zip_files[0].path} --development-mode"
            }
        }

        stage('Archive') {
            archiveArtifacts artifacts: "target/*.zip, target/*.bconf, target/*.xml, target/*.bar", fingerprint: true, flatten:true
        }
    } // credentials
  	} // timestamps
  } // ansiColor
} // node
