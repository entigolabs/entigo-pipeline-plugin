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
```

## Building plugin

Plugin can be built by using Maven install command.

`mvn install`

Plugin package will be built into the target subdirectory. The .hpi file can be installed to Jenkins through Plugin Manager > Advanced.

## Development

Requires Maven settings.xml to be modified.
Add next lines to the settings.xml which is usually located in user home .m2 directory.

```
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>

  <profiles>
    <!-- Give access to Jenkins plugins -->
    <profile>
      <id>jenkins</id>
      <activation>
        <activeByDefault>true</activeByDefault> <!-- change this to false, if you don't like to have it on per default -->
      </activation>
      <repositories>
        <repository>
          <id>repo.jenkins-ci.org</id>
          <url>https://repo.jenkins-ci.org/public/</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>repo.jenkins-ci.org</id>
          <url>https://repo.jenkins-ci.org/public/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <mirrors>
    <mirror>
      <id>repo.jenkins-ci.org</id>
      <url>https://repo.jenkins-ci.org/public/</url>
      <mirrorOf>m.g.o-public</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```