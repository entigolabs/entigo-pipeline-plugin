@Library('entigo-jenkins-library')
import entigo.Common

def common = new Common()


pipeline {
  agent {
    kubernetes {
      yaml common.loadKubernetesYaml('openjdk11')
    }
  }
  options {
        ansiColor('xterm')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
  }
  stages {
    stage('Build Jenkins plugin') {
      steps {
        container('build') {
          sh './mvnw install'
        }
      }
    }
  }
  post {
    always {
        archiveArtifacts artifacts: 'target/plugin.hpi', onlyIfSuccessful: true
        cleanWs()
    }
  }
}