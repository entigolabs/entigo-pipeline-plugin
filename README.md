# Entigo Pipeline Plugin

Jenkins plugin for building CI/CD pipelines on top of Kubernetes and ArgoCD.

* [Introduction](#introduction)
* [ArgoCD Integration](#argocd-integration)
    * [Configuration](#argocd-configuration)
    * [Environmental variables](#argocd-environmental-variables)
    * [Job options](#argocd-job-options)
    * [Pipeline steps](#argocd-pipeline-steps)
    * [Working example](#argocd-working-example)

## Introduction

The goal of this project is to provide easy to use tools for setting up a software pipeline. Functionality will be based on the CI/CD pipeline best practises developed by Entigo which describe how to build the software, run it through analysis, tests and environments, and deploy the results to production.

This plugin is still in active development and more functionality will be implemented soon.

## ArgoCD Integration

Before using any build steps

* Configure the ArgoCD connections and their matchers in Configure System -> Entigo Pipeline
* Set the env variable **ARGO_CD_SELECTOR** at the beginning of the pipeline script

### ArgoCD Configuration

* Connections
    * Connection name - unique name for a connection which is used when selecting a connection during a build.
    * Host uri - has to include http or https scheme and not include the api path.
    * Credentials - Jenkins Credential with Secret type and with a value of ArgoCD user authentication token.
    * Ignore ArgoCD SSL Certificate Errors disables all SSL errors and enables insecure connections.
    * App wait timeout - in seconds for how long ArgoCD commands will wait for application actions, fails the build if timeout is exceeded.
* Connection matchers
    * Matching Pattern - pattern is based on Java regex and will be matched against ARGO_CD_SELECTOR env variable.
    * Connection name - name of the connection to use when pattern matches. Connections have to be saved first to populate the selection list.
    
### ArgoCD Environmental variables

* ARGO_CD_SELECTOR - **Required**, sets a value which is used to select a connection based on the configured connection matchers. For example: `env.GIT_BRANCH`

### ArgoCD Job options

#### argoCDConnections

**Optional**, overrides the global configuration of connection matchers. Can be set through the Job UI or DSL. Parameters:

* List of connection matchers
    * Pattern - view Matching Pattern in ArgoCD configuration section
    * ConnectionName - view Connection name in ArgoCD configuration section

Example usage

```
options {
  argoCDConnections([
    [connectionName: 'dev', pattern: '^.*/dev']
  ])
}
```

### ArgoCD Pipeline steps

#### syncArgoApp

Sends application sync request to ArgoCD. Parameters:

* name - ArgoCD application name, required.
* async - Step won't wait for application sync to complete. Default false.
* waitTimeout - Overrides Global configuration. View App wait timeout from ArgoCD configuration section.

Minimal usage example

```syncArgoApp 'application-name'```

Full example

```syncArgoApp async: false, name: 'application-name', waitTimeout: 600```

### ArgoCD Working example

Replace the connection-matcher with a value that matches one of the pre-configured connection matchers, e.g set both to "dev".

Replace the application-name with the name of the ArgoCD application to synchronize.

```
pipeline {
    agent any
    environment {
      ARGO_CD_SELECTOR = "connection-matcher"
    }
    stages {
        stage('ArgoCD sync') {
            steps {
                syncArgoApp 'application-name'
            }
        }
    }
}
```