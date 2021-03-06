# Entigo Pipeline Plugin

Jenkins plugin for building CI/CD pipelines on top of Kubernetes and ArgoCD.

* [Introduction](#introduction)
* [Installation](#installation)
* [ArgoCD Integration](#argocd-integration)
    * [Configuration](#argocd-configuration)
    * [Environmental variables](#argocd-environmental-variables)
    * [Job options](#argocd-job-options)
    * [Pipeline steps](#argocd-pipeline-steps)
    * [Working example](#argocd-working-example)

## Introduction

The goal of this project is to provide easy to use tools for setting up a software pipeline. Functionality will be based on the CI/CD pipeline best practises developed by Entigo which describe how to build the software, run it through analysis, tests and environments, and deploy the results to production.

This plugin is still in active development and more functionality will be implemented soon.

## Installation

The released .hpi file can be installed into Jenkins by using the "Upload Plugin" option found under Manage Jenkins -> Plugin Manager -> Advanced

## ArgoCD Integration

Before using any build steps

* Configure the ArgoCD connections and their matchers in Configure System -> Entigo Pipeline
* Note, steps select the connection to use based on a priority order:
    1. Selector parameter in the step command
    2. Selector set with env variable ARGO_CD_SELECTOR
    3. Default ArgoCD connection set in the global configuration

### ArgoCD Configuration

* Connections
    * Connection name - unique name for a connection which is used when selecting a connection during a build.
    * Host uri - has to include http or https scheme and not include the api path.
    * Credentials - Jenkins Credential with Secret text type and with a value of ArgoCD user authentication token.
    * Ignore ArgoCD SSL Certificate Errors disables all SSL errors and enables insecure connections.
    * App wait timeout - in seconds for how long ArgoCD step will wait for application requests to finish, fails the build if timeout is exceeded.
    * Generate a name based matcher - after applying the changes, it will automatically generate a matcher that has a pattern that strictly matches the connection name.
* Default ArgoCD connection - default connection to use when neither step nor env variable specify a selector for choosing a connection.
* Connection matchers
    * Matching Pattern - Java regex based pattern which will be matched against the specified selector value.
    * Connection name - name of the connection to use when pattern matches.
    
### ArgoCD Environmental variables

* ARGO_CD_SELECTOR - **Optional**, overrides the global default connection, sets a value which is used to select a connection based on the configured connection matchers. For example: `env.GIT_BRANCH`

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

* name - **Required**, name of the ArgoCD application.
* wait - Wait for application sync to complete. Default **true**.
* waitTimeout - Overrides Global configuration. View App wait timeout from ArgoCD configuration section.
* waitFailure - Fail the build when waiting for the sync to complete exceeds the timeout. Default **true**.
* connectionSelector - Overrides the ARGO_CD_SELECTOR env variable, value which is used to select a connection based on the configured connection matchers.

Minimal usage example

```syncArgoApp 'application-name'```

Full example

```syncArgoApp wait: true, name: 'application-name', waitTimeout: 600, connectionSelector: 'selector-value'```

#### getArgoApp

Gets information about the ArgoCD application. Parameters:

* name - **Required**, name of the ArgoCD application.
* projectName - Optional, name of the ArgoCD project.
* connectionSelector - Overrides the ARGO_CD_SELECTOR env variable, value which is used to select a connection based on the configured connection matchers.
* waitTimeout - Overrides Global configuration. View App wait timeout from ArgoCD configuration section.

Returned values:

* repoUrl - ArgoCD application source repo URL
* revision - ArgoCD application source target revision
* path - ArgoCD application source path
* connectionName - name of the ArgoCD connection that was used

Minimal usage example

```getArgoApp 'application-name'```

Full example

```getArgoApp connectionSelector: 'selector-value', name: 'application-name', projectName: 'project-name', waitTimeout: 60```

#### deleteArgoApp

Deletes ArgoCD application. Parameters:

* name - **Required**, name of the ArgoCD application.
* cascade - Optional, perform a cascaded deletion of all application resources. Default **true**.
* connectionSelector - Overrides the ARGO_CD_SELECTOR env variable, value which is used to select a connection based on the configured connection matchers.
* waitTimeout - Overrides Global configuration. View App wait timeout from ArgoCD configuration section.

Minimal usage example

```deleteArgoApp 'application-name'```

Full example

```deleteArgoApp cascade: true, connectionSelector: 'selector-value', name: 'application-name', waitTimeout: 60```

#### listArgoConnections

Lists the configured connection names from the Global Configuration.

Usage example

```listArgoConnections()```

Example use case

```
def destinations = listArgoConnections()
pipeline {
  parameters {
      choice(name: 'dstenv', choices: destinations)
  }
...
}
```

#### withArgoCDConnection

Binds env variables based on the given connection selector.
* ARGO_CD_SERVER - ArgoCD Host uri
* ARGO_CD_TOKEN - ArgoCD Credentials user authentication token

Parameters:
* connectionSelector - value which is used to select a connection based on the configured connection matchers.

Example use case

```
steps{
  script{
    withArgoCDConnection(connectionSelector:"selector-value"){
      echo env.ARGO_CD_SERVER
      echo env.ARGO_CD_TOKEN
      ...
    }
  }
}
```

### ArgoCD Working example

When creating a connection in the configuration, don't uncheck the matcher generation. Replace the connection-name value with the name of a pre-configured connection and application-name with the name of the ArgoCD application to synchronize.

```
pipeline {
    agent any
    stages {
        stage('ArgoCD sync') {
            steps {
                script {
                    appInfo=getArgoApp name: 'application-name', connectionSelector: 'connection-name'
                }
                echo 'Application repoURL: ' + appInfo.repoUrl
                echo 'Application target revision: ' + appInfo.revision
                echo 'Application path: ' + appInfo.path
                syncArgoApp name: 'application-name', connectionSelector: 'connection-name'
            }
        }
    }
}
```