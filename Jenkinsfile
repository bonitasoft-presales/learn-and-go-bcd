@Library('github.com/bonitasoft-presales/bonita-jenkins-library@1.0.1') _

def bonitaVersion = "7.13.1"
def bonitaVersionShortened = "7131"
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
  	def verbose_mode= ''
  	if ("${debugMode}".toBoolean()) {
		debug_flag = '-X'
		verbose_mode='-v'
  	}
  	
  	// used in steps, do not change
    def yamlFile = "${WORKSPACE}/props.yaml"
    def bconfFolder = '/home/bonita/bonita-continuous-delivery/bconf'
    def yamlStackProps
    def privateDnsName
  	
  	def extraVars="${verbose_mode} --extra-vars bcd_stack_id=${stackName} --extra-vars bonita_version=${bonitaVersion}"
  
  ansiColor('xterm') {
    timestamps {
  
        stage("Checkout") {
            checkout scm
            echo "jobBaseName: $jobBaseName"
            echo "gitRepoName: $gitRepoName"
        }

	
		stage("Create stack") {
	       		sh """
cd ~/ansible/aws
java -jar bonita-aws-1.0-SNAPSHOT-jar-with-dependencies.jar -c create --stack-id ${stackName} --name ${normalizedGitRepoName} --key-file ~/.ssh/presale-ci-eu-west-1.pem
cp ${stackName}.yaml ${WORKSPACE}

"""       
                yamlStackProps = readYaml file: "${WORKSPACE}/${stackName}.yaml"
                //echo "yaml read props: ${yamlStackProps}"
                
                privateDnsName = yamlStackProps.privateDnsName
                echo "privateDnsName: [${privateDnsName}]" 
                 
                  
		    }


        stage("Build LAs") {
            bcd scenario:scenarioFile, args: "${extraVars} livingapp build ${debug_flag} -p ${WORKSPACE} --environment ${bonitaConfiguration}"
        }
        
        
	    stage("Deploy server") {
	     	 	// ensure ip is added in known hosts
	     	 	// keep bcd build stage with stack create, to ensure SSHd is up & running on created stack
	     	 	sh """
ssh-keygen -R ${privateDnsName}
ssh -o StrictHostKeyChecking=no -i ~/.ssh/presale-ci-eu-west-1.pem  ubuntu@${privateDnsName} ls
"""     
	     	    sh """
cd ~/ansible
ansible-playbook bonita.yaml -i aws/private-inventory-${stackName}.yaml
"""

	        	def bonitaUrl = "http://${yamlStackProps.publicDnsName}:8081/bonita/login.jsp"
	            currentBuild.description = "<a href='${bonitaUrl}'>${stackName}</a>"
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
  	} // timestamps
  } // ansiColor
} // node
