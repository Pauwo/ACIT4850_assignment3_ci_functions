def call(dockerRepoName, imageName) {
  pipeline {
    agent any
    stages {
      stage('Build') {
        steps {
          sh 'pip install -r requirements.txt'
        }
      }
      stage('Python Lint') {
        steps {
          sh 'pylint-fail-under --fail_under 5.0 *.py'
        }
      }
      stage('Package') {
        when { branch 'main' }
        steps {
            withCredentials([string(credentialsId: 'pauwo', variable: 'TOKEN')]) {
            sh "docker login -u 'pauwo' -p '$TOKEN' docker.io"
            sh "docker build -t ${dockerRepoName}:latest --tag pauwo/${dockerRepoName}:${imageName} ."
            sh "docker push pauwo/${dockerRepoName}:${imageName}"
            }
        }
      }
      stage('Docker Scan') {
        steps {
          sh "grype pauwo/${dockerRepoName}:${imageName}"
        }
      }
    //   stage('Deploy') {
    //     steps {
    //       sshagent(credentials: ['SSH_Key_Paulo']) {
    //         sh """
    //           ssh -o StrictHostKeyChecking=no azureuser@acit3855-nginx.eastus2.cloudapp.azure.com \
    //             "docker pull pauwo/${dockerRepoName}:${imageName} && docker compose up -d"
    //         """
    //       }
    //     }
    //   }
    }
  }
}