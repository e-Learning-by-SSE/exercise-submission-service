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
                script {
                    version = sh(
                            returnStdout: true,
                            script: 'mvn -q -Dexec.executable=echo -Dexec.args=\'${project.version}\' --non-recursive exec:exec')
                        .trim()
                    image = docker.image("${env.DOCKER_TARGET}")
                    docker.withRegistry('https://ghcr.io', 'github-ssejenkins') {
                        image.push("${version}") // pom project version
                        image.push() // tagged version from name
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
