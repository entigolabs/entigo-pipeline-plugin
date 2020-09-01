## ArgoCD integration

Before using any ArgoCD pipeline steps make sure ArgoCD endpoint has been configured through the Jenkins System Configuration.

Requires Jenkins Credential with type Secret and value of ArgoCD user authentication token.

#### Pipeline steps

##### syncArgoApp
Sends application sync request to ArgoCD. Parameters:
- name - ArgoCD application name, required
- async - Step won't wait for application sync to complete. Default `false`
- waitTimeout - How long will step wait in seconds for sync to complete, aborts the build when time is exceeded. Default taken from Global configuration.

Example usage

```
syncArgoApp 'application-name'
syncArgoApp async: false, name: 'application-name', waitTimeout: 600