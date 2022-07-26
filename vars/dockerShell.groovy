def call(String registryCred = 'a', String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a') {

pipeline {
environment { 
		registryCredential = "${registryCred}"
		registry = "$registryin" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
	}
		
    agent none

    stages {
        stage("POLL SCM"){
		agent{label 'dock'}
            	steps {
                	checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])             
            	}
        } 
        stage('BUILD IMAGE') {
		agent{label 'dock'}
            	steps {
                	sh 'docker build -t $registry:$dockerTag .'             
            	}
        }
        stage('PUSH HUB') { 
		agent{label 'dock'}
            	steps {
			sh 'docker push $registry:$dockerTag'                   	
                }    
        }
        stage('DEPLOY IMAGE') {
		agent{label 'dock'}
		steps {
			sh 'docker run -itd -p 3080:3080 --name "jenkinsintegration-$dockerTag" "$registry:$dockerTag"'
		}
	}  
    }
}  
}
