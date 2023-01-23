pipeline {
    agent {
        label 'maven && jenkins && jdk17'
    }
    
    stages {
        stage ('Maven') {
            steps {
                withMaven(mavenSettingsConfig: 'mvn-elearn-repo-settings') {
                    sh 'mvn clean deploy spring-boot:build-image -Dspring-boot.build-image.publish=true'
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
