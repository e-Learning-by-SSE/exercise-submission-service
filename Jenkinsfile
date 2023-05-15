pipeline {
    agent {
        label 'maven && docker && jdk17'
    }
    
    environment {
        DOCKER_TARGET = 'ghcr.io/e-learning-by-sse/exercise-submission-service:latest'
    }

    stages {

        stage('Maven') {
            steps {
                withMaven(mavenSettingsConfig: 'mvn-elearn-repo-settings') {
                    sh "mvn clean deploy spring-boot:build-image -Dspring-boot.build-image.imageName=${env.DOCKER_TARGET}"
                }
                archiveArtifacts artifacts: 'target/openapi.json', fingerprint: true
            }
        }

        stage('Publish Docker') {
            steps {
                ssedocker {
                    publish {
                        imageName "${env.DOCKER_TARGET}"
                        additionalTag "${maven.getProjectVersion()}"
                    }
                }
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
