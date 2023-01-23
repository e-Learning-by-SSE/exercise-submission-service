pipeline {
    agent {
     label 'maven && docker && jdk17'
    }
    
    stages {
        stage ('Maven') {
            steps {
                withMaven(mavenSettingsConfig: 'mvn-elearn-repo-settings') {
                    sh 'mvn clean deploy'
                }
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        stage ('Analysis') {
            steps {
                jacoco()
                script {
                    def checkstyle = scanForIssues tool: [$class: 'CheckStyle'], pattern: 'target/checkstyle-result.xml'
                    publishIssues issues:[checkstyle]
                } 
            }
        }
    }
}
