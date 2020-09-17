## How to use

Plugin functionality has been described in Entigo wiki.

[ArgoCD Integration](https://entigo.atlassian.net/wiki/spaces/PD/pages/1099202561/ArgoCD+Integration)

## Building the plugin

Plugin can be built by using the Maven install command.

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

Plugin development uses the Maven HPI Plugin.
Development environment is ran through HPI which launches a Jenkins instance with the plugin by default on `http://localhost:8080`

`mvn hpi:run`

Debugging is possible by adding that command as Run/Debug configuration in IntelliJ and then debugging like a regular Java application.