package com.datapipe.jenkins.vault.configuration;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import com.cloudbees.hudson.plugins.folder.AbstractFolderPropertyDescriptor;
import hudson.model.ItemGroup;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.SortedMap;

import static com.datapipe.jenkins.vault.configuration.VaultConfigurationSpec.completeTestConfig;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FolderVaultConfigurationSpec {

    private FolderVaultConfiguration completeTestConfigFolder(String identifier) {
        return new FolderVaultConfiguration(completeTestConfig(identifier));
    }

    @Test
    public void resolverShouldNotFailIfNotInFolder() {
        VaultConfigResolver folderResolver = new FolderVaultConfiguration.ForJob();
        Job job = mock(Job.class);
        when(job.getParent()).thenReturn(mock(Jenkins.class));

        VaultConfiguration result = folderResolver.forJob(job);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void resolverShouldCorrectlyMerge() {
        final DescribableList firstFolderProperties = mock(DescribableList.class);
        when(firstFolderProperties.get(FolderVaultConfiguration.class)).thenReturn(completeTestConfigFolder("firstParent"));

        final DescribableList secondFolderProperties = mock(DescribableList.class);
        when(secondFolderProperties.get(FolderVaultConfiguration.class)).thenReturn(completeTestConfigFolder("secondParent"));

        final AbstractFolder secondParent = generateMockFolder(secondFolderProperties, null);

        final AbstractFolder firstParent = generateMockFolder(firstFolderProperties, secondParent);

        final Job job = generateMockJob(firstParent);

        VaultConfiguration result = new FolderVaultConfiguration.ForJob().forJob(job);

        VaultConfiguration expected = completeTestConfig("firstParent").mergeWithParent(completeTestConfig("secondParent"));

        assertThat(result.getVaultCredentialId(), is(expected.getVaultCredentialId()));
        assertThat(result.getVaultUrl(), is(expected.getVaultUrl()));
    }


    @Test
    public void resolverShouldHandleAbsentConfigurationOnFolders() {

        final DescribableList firstFolderProperties = mock(DescribableList.class);
        when(firstFolderProperties.get(FolderVaultConfiguration.class)).thenReturn(completeTestConfigFolder("firstParent"));

        final DescribableList secondFolderProperties = mock(DescribableList.class);
        when(secondFolderProperties.get(FolderVaultConfiguration.class)).thenReturn(null);

        final AbstractFolder secondParent = generateMockFolder(secondFolderProperties, null);

        final AbstractFolder firstParent = generateMockFolder(firstFolderProperties, secondParent);

        final Job job = generateMockJob(firstParent);


        VaultConfiguration result = new FolderVaultConfiguration.ForJob().forJob(job);

        VaultConfiguration expected = completeTestConfig("firstParent").mergeWithParent(null);

        assertThat(result.getVaultCredentialId(), is(expected.getVaultCredentialId()));
        assertThat(result.getVaultUrl(), is(expected.getVaultUrl()));
    }

    private Job generateMockJob(final AbstractFolder firstParent) {
        return new Job(firstParent, "test-job") {
                @Override
                public boolean isBuildable() {
                    return true;
                }

                @Override
                protected SortedMap _getRuns() {
                    return null;
                }

                @Override
                protected void removeRun(Run run) {

                }

                @Nonnull
                @Override
                public ItemGroup getParent() {
                    return firstParent;
                }
            };
    }

    private AbstractFolder generateMockFolder(final DescribableList firstFolderProperties, final AbstractFolder parentToReturn) {
        return new AbstractFolder<TopLevelItem>(null, null) {
                @Nonnull
                @Override
                public ItemGroup getParent() {
                    return parentToReturn;
                }

                @Override
                public DescribableList<AbstractFolderProperty<?>, AbstractFolderPropertyDescriptor> getProperties() {
                    return firstFolderProperties;
                }
            };
    }
}
