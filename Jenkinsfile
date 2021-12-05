pipeline {
    agent any
    options {
        ansiColor('xterm')
        timestamps()
    }
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
            sh '''echo "VERSION=${TEST_IMAGE}" > ./allure-results/environment.properties'''
            allure results: [[path: 'allure-results']]
        }
        cleanup {
            cleanWs()
        }
    }
}
