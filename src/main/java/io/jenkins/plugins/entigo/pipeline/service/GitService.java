package io.jenkins.plugins.entigo.pipeline.service;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.plugins.git.GitTool;
import io.jenkins.plugins.entigo.pipeline.model.GitBranch;
import io.jenkins.plugins.entigo.pipeline.util.CredentialsUtil;
import jenkins.model.Jenkins;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author: Märt Erlenheim
 * Date: 2020-09-30
 */
public class GitService {

    private static final String GIT_AUTHOR_NAME = "jenkins";
    private static final String GIT_AUTHOR_EMAIL = "jenkins@localhost";
    private static final String GIT_HEAD = "HEAD";
    private static final String GIT_REMOTE_NAME = "origin";
    private static final String GIT_HEAD_REFERENCE = "refs/heads/";

    private final GitClient git;
    private final GitBranch gitBranch;

    public GitService(TaskListener listener, EnvVars envVars, FilePath localRepoPath, String credentialsId,
                      String repoUrl, String targetRevision)
            throws IOException, InterruptedException {
        this.git = createClient(listener, envVars, localRepoPath);
        if (credentialsId != null) {
            addGitCredentials(credentialsId);
        } else {
            listener.getLogger().println("[WARNING] No git credentials have been set in the global config");
        }
        this.gitBranch = getGitBranch(repoUrl, targetRevision);
    }

    private GitClient createClient(TaskListener listener, EnvVars envVars, FilePath localRepoPath)
            throws IOException, InterruptedException {
        GitTool tool = findGitTool(getWorkspaceNode(localRepoPath), envVars, listener);
        String gitExe = tool == null ? null : tool.getGitExe();
        GitClient git = Git.with(listener, envVars)
                .using(gitExe) // Null exe will default to Jgit
                .in(localRepoPath)
                .getClient();
        git.setAuthor(GIT_AUTHOR_NAME, GIT_AUTHOR_EMAIL);
        return git;
    }

    private void addGitCredentials(String credentialsId) throws AbortException {
        StandardCredentials credentials = CredentialsUtil.findCredentialsById(credentialsId,
                StandardUsernameCredentials.class, Collections.emptyList());
        git.addDefaultCredentials(credentials);
    }

    private GitBranch getGitBranch(String repoUrl, String targetRevision) throws AbortException, InterruptedException {
        String branchName = targetRevision;
        if (GIT_HEAD.equals(targetRevision)) {
            branchName = getHeadBranchName(repoUrl);
        }
        return new GitBranch(repoUrl, branchName);
    }

    public GitBranch getGitBranch() {
        return gitBranch;
    }

    public void checkout() throws InterruptedException {
        // Using a refspec causes git to fetch only the branch we need
        git.clone_().url(gitBranch.getUrl()).refspecs(Collections.singletonList(refSpec()))
                .repositoryName(GIT_REMOTE_NAME).shallow(true).execute();
        git.checkout().branch(gitBranch.getName()).ref(GIT_REMOTE_NAME + "/" + gitBranch.getName())
                .deleteBranchIfExist(true).execute();
    }

    public void push(String commitMessage, List<String> filePatterns) throws InterruptedException, URISyntaxException {
        if (git.hasGitRepo()) {
            for (String filePattern : filePatterns) {
                git.add(filePattern);
            }
            git.commit(commitMessage);
            git.push().to(new URIish(gitBranch.getUrl())).ref(gitBranch.getName()).execute();
        } else {
            throw new IllegalStateException("Git repository does not exist");
        }
    }

    private Node getWorkspaceNode(FilePath workspace) {
        Jenkins jenkins = Jenkins.get();
        if (workspace != null && workspace.isRemote()) {
            for (Computer computer : jenkins.getComputers()) {
                if (computer.getChannel() == workspace.getChannel()) {
                    Node node = computer.getNode();
                    if (node != null) {
                        return node;
                    }
                }
            }
        }
        return jenkins;
    }

    private GitTool findGitTool(Node buildNode, EnvVars envVars, TaskListener listener) {
        GitTool tool = GitTool.getDefaultInstallation();
        if (tool != null) {
            if (buildNode != null) {
                try {
                    tool = tool.forNode(buildNode, listener);
                } catch (IOException | InterruptedException e) {
                    listener.getLogger().println("Failed to get git executable for node");
                }
            }
            if (tool != null) {
                tool = tool.forEnvironment(envVars);
            }
        }
        return tool;
    }

    private String getHeadBranchName(String url) throws AbortException, InterruptedException {
        Map<String, String> headReferences = git.getRemoteSymbolicReferences(url, GIT_HEAD);
        if (headReferences.size() == 1) {
            String branchName = headReferences.values().iterator().next();
            int prefixIndex = branchName.indexOf(GIT_HEAD_REFERENCE);
            if (prefixIndex > -1) {
                return branchName.substring(prefixIndex + GIT_HEAD_REFERENCE.length());
            }
        }
        throw new AbortException("Couldn't find a branch name for remote HEAD revision. Set the ArgoCD " +
                "application target revision to a branch instead of HEAD to avoid this problem." +
                " Might be caused by git client plugin not finding the correct git version.");
    }

    private RefSpec refSpec() {
        return new RefSpec(String.format("+refs/heads/%s:refs/remotes/%s/%s", gitBranch.getName(), GIT_REMOTE_NAME,
                gitBranch.getName()));
    }
}
