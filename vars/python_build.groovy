// def call(dockerRepoName, imageName) {
//   pipeline {
//     agent any
//     stages {
//       stage('Build') {
//         steps {
//             sh 'pip install -r requirements.txt'
//           }
//         }
//       stage('Python Lint') {
//         steps {
//           dir(config.servicePath) {
//             sh 'pylint-fail-under --fail_under 5.0 *.py'
//           }
//         }
//       }
//       stage('Package') {
//         when { branch 'main' }
//         steps {
//           dir(config.servicePath) {
//             withCredentials([string(credentialsId: 'pauwo', variable: 'TOKEN')]) {
//             sh "docker login -u 'pauwo' -p '$TOKEN' docker.io"
//             sh "docker build -t ${config.dockerRepoName}:latest --tag pauwo/${dockerRepoName}:${imageName} ."
//             sh "docker push pauwo/${config.dockerRepoName}:${imageName}"
//             }
//           } 
//         }
//       }
//       stage('Docker Scan') {
//         steps {
//           dir(config.servicePath) {
//             sh "grype ${config.dockerUsername}/${config.dockerRepoName}:${config.imageTag}"
//           }
//         }
//       }
//       stage('Deploy') {
//         steps {
//           sshagent(credentials: ['SSH_Key_Paulo']) {
//             sh """
//               ssh -o StrictHostKeyChecking=no paulo@paulo-acit3855-lab9.eastus.cloudapp.azure.com \
//                 "docker pull pauwo/${dockerRepoName}:${imageName} && docker compose up -d"
//             """
//           }
//         }
//       }
//     }
//   }
// }


def call(dockerRepoName, imageName){
    pipeline {
    agent any
    stages{
        stage('Build') {
            steps {
                sh 'pip install -r requirements.txt'
            }
        }
        stage('Python Lint'){
            steps{
                sh 'pylint-fail-under --fail_under 5.0 *.py'
            }
        }

        stage('Package') {
            when {
            expression { env.GIT_BRANCH == 'origin/main' }
            }
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
                withCredentials([string(credentialsId: 'pauwo', variable: 'TOKEN')]) {
                sh "grype pauwo/${dockerRepoName}:${imageName}"
                }
                }
        }

        stage('Deploy') {
            steps{
                sshagent(credentials:['SSH_Key_Anson']){
                    sh "ssh  -o StrictHostKeyChecking=no paulo@paulo-acit3855-lab9.eastus.cloudapp.azure.com docker pull pauwo/${dockerRepoName}:${imageName}"
                    sh "ssh  -o StrictHostKeyChecking=no paulo@paulo-acit3855-lab9.eastus.cloudapp.azure.com docker compose up -d"
                }
            }
        }
    }

    }
}