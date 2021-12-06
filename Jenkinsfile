pipeline {
    agent any
    environment {
        TEST_IMAGE = "${TEST_IMAGE}"
    }
    stages {
        stage('Test') {
            steps {
                sh './gradlew clean test'
            }
        }
    }
    post {
        always {
            junit '**/test-reports/*.xml'
        }
        cleanup {
            cleanWs()
        }
    }
}
