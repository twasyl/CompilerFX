package com.twasyl.compilerfx.utils;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.exceptions.MissingConfigurationFileException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationWorker {

    private static File configurationFile;

    private static final String UI_LOCALE = "application.uiLocale";
    private static final String MAVEN_COMMAND_PROPERTY = "maven.command";
    private static final String REPOSITORY_NAME = "repository.id.%1$s.name";
    private static final String REPOSITORY_PATH = "repository.id.%1$s.path";
    private static final String REPOSITORY_STATUS = "repository.id.%1$s.status";
    private static final String REPOSITORY_SELECTED = "repository.id.%1$s.selected";
    private static final String REPOSITORY_OPTIONS = "repository.id.%1$s.options";
    private static final String REPOSITORY_POST_BUILD_COMMANDS = "repository.id.%1$s.postBuildCommands";
    private static final String REPOSITORY_PRIORITY = "repository.id.%1$s.priority";
    private static final String REPOSITORY_GOAL_ACTIVE = "repository.id.%1$s.goal.%2$s.active";
    private static final String REPOSITORY_WORKSPACE = "repository.id.%1$s.workspace";
    private static final String WORKSPACE_NAME = "workspace.id.%1$s.name";
    private static final String WORKSPACE_ACTIVE = "workspace.id.%1$s.active";
    private static final String CUSTOM_MAVEN_OPTION = "maven.customOption.id.%1$s.option";
    private static final String CUSTOM_MAVEN_OPTION_DESCRIPTION = "maven.customOption.id.%1$s.description";

    private static File getConfigurationFile() {
        if(configurationFile == null) {
            final String userHome = System.getProperty("user.home");
            configurationFile = new File(userHome.concat("/.compilerfx.properties"));
        }

        return configurationFile;
    }

    public static void load() throws MissingConfigurationFileException {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(getConfigurationFile()));
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Can not load software's configuration");
            throw new MissingConfigurationFileException(e);
        }

        Configuration.getInstance().setUiLocale(LocaleUtils.parseString(properties.getProperty(UI_LOCALE)));
        Configuration.getInstance().setMavenCommand((String) properties.get(MAVEN_COMMAND_PROPERTY));
        Configuration.getInstance().getRepositories().clear();
        Configuration.getInstance().getWorkspaces().clear();

        MavenRepository repository;
        MavenRepository.MavenOption customOption;
        Workspace workspace;

        /* Repositories and workspaces and custom options keys */
        List<String> repositoriesId = new ArrayList<>();
        List<String> workspacesId = new ArrayList<>();
        List<String> customOptionsId = new ArrayList<>();

        /* Used for loading the repositories and set their workspace. Avoid loop of the collection in Configuration */
        Map<String, Workspace> tmpWorkspaces = new HashMap<>();

        for(String key : properties.stringPropertyNames()) {
            if(key.startsWith("repository.id.") && key.endsWith(".name")) {
                repositoriesId.add(key.replace("repository.id.", "").replace(".name", ""));
            } else if(key.startsWith("workspace.id") && key.endsWith(".name")) {
                workspacesId.add(key.replace("workspace.id.", "").replace(".name", ""));
            } else if(key.startsWith("maven.customOption.id.") && key.endsWith(".option")) {
                customOptionsId.add(key.replace("maven.customOption.id.", "").replace(".option", ""));
            }
        }

        /* Load custom options */
        for(String customOptionId : customOptionsId) {
            customOption = new MavenRepository.MavenOption();
            customOption.setId(Long.parseLong(customOptionId));

            customOption.setOption(properties.getProperty(String.format(CUSTOM_MAVEN_OPTION, customOptionId)));
            customOption.setDescription(properties.getProperty(String.format(CUSTOM_MAVEN_OPTION_DESCRIPTION, customOptionId)));

            Configuration.getInstance().getCustomMavenOptions().add(customOption);
        }

        /* Load workspaces */
        for(String workspaceId : workspacesId) {
            workspace = new Workspace();
            workspace.setId(Long.parseLong(workspaceId));
            workspace.setName((String) properties.get(String.format(WORKSPACE_NAME, workspaceId)));
            workspace.setActive(Boolean.parseBoolean((String) properties.get(String.format(WORKSPACE_ACTIVE, workspaceId))));

            Configuration.getInstance().getWorkspaces().add(workspace);
            tmpWorkspaces.put(workspaceId, workspace);
        }

        /* Manage default workspace */
        if(!tmpWorkspaces.containsKey("0")) {
            workspace = createDefaultWorkpace();

            Configuration.getInstance().getWorkspaces().add(workspace);
            tmpWorkspaces.put("0", workspace);
        }

        /* Load repositories */
        for(String repositoryId : repositoriesId) {
            repository = new MavenRepository();
            repository.setId(Long.parseLong(repositoryId));
            repository.setRepositoryName((String) properties.get(String.format(REPOSITORY_NAME, repositoryId)));
            repository.setPath((String) properties.get(String.format(REPOSITORY_PATH, repositoryId)));
            repository.setStatus(Status.valueOf((String) properties.get(String.format(REPOSITORY_STATUS, repositoryId))));
            repository.setSelected(Boolean.parseBoolean((String) properties.get(String.format(REPOSITORY_SELECTED, repositoryId))));
            repository.setPriority(Integer.parseInt((String) properties.get(String.format(REPOSITORY_PRIORITY, repositoryId))));
            repository.setOptions((String) properties.get(String.format(REPOSITORY_OPTIONS, repositoryId)));
            repository.setPostBuildCommands((String) properties.get(String.format(REPOSITORY_POST_BUILD_COMMANDS, repositoryId)));

            workspace = tmpWorkspaces.get(properties.get(String.format(REPOSITORY_WORKSPACE, repositoryId)));
            if(workspace != null) {
                workspace.getRepositories().add(repository);
            } else {
                tmpWorkspaces.get("0").getRepositories().add(repository);
            }

            repository.getGoals().put(
                    new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()),
                    new SimpleBooleanProperty(Boolean.parseBoolean((String) properties.get(String.format(REPOSITORY_GOAL_ACTIVE, repositoryId, MavenRepository.Goal.CLEAN.getGoalName()))))
            );
            repository.getGoals().put(
                    new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()),
                    new SimpleBooleanProperty(Boolean.parseBoolean((String) properties.get(String.format(REPOSITORY_GOAL_ACTIVE, repositoryId, MavenRepository.Goal.INSTALL.getGoalName()))))
            );

            Configuration.getInstance().getRepositories().add(repository);
        }
    }

    public static void save() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(getConfigurationFile()));

            writer.println(String.format("%1$s=%2$s", UI_LOCALE, Configuration.getInstance().getUiLocale() == null ? "" : LocaleUtils.toString(Configuration.getInstance().getUiLocale())));
            writer.println(String.format("%1$s=%2$s", MAVEN_COMMAND_PROPERTY, Configuration.getInstance().getMavenCommand()));

            for(MavenRepository.MavenOption option : Configuration.getInstance().getCustomMavenOptions()) {
                writer.println(String.format(CUSTOM_MAVEN_OPTION.concat("=%2$s"), option.getId(), option.getOption()));
                writer.println(String.format(CUSTOM_MAVEN_OPTION_DESCRIPTION.concat("=%2$s"), option.getId(),
                        option.getDescription() != null && !option.getDescription().trim().isEmpty() ? option.getDescription() : ""));
            }

            for(Workspace workspace : Configuration.getInstance().getWorkspaces()) {
                writer.println(String.format(WORKSPACE_NAME.concat("=%2$s"), workspace.getId(), workspace.getName()));
                writer.println(String.format(WORKSPACE_ACTIVE.concat("=%2$s"), workspace.getId(), workspace.getActive()));
            }

            for(MavenRepository repository : Configuration.getInstance().getRepositories()) {
                writer.println(String.format(REPOSITORY_NAME.concat("=%2$s"), repository.getId(), repository.getRepositoryName()));
                writer.println(String.format(REPOSITORY_PATH.concat("=%2$s"), repository.getId(), repository.getPath()));
                writer.println(String.format(REPOSITORY_STATUS.concat("=%2$s"), repository.getId(), repository.getStatus()));
                writer.println(String.format(REPOSITORY_SELECTED.concat("=%2$s"), repository.getId(), repository.isSelected()));
                writer.println(String.format(REPOSITORY_PRIORITY.concat("=%2$s"), repository.getId(), repository.getPriority()));
                writer.println(String.format(REPOSITORY_OPTIONS.concat("=%2$s"), repository.getId(),
                        repository.getOptions() != null && !repository.getOptions().trim().isEmpty() ? repository.getOptions() : ""));
                writer.println(String.format(REPOSITORY_POST_BUILD_COMMANDS.concat("=%2$s"), repository.getId(),
                        repository.getPostBuildCommands() != null && !repository.getPostBuildCommands().trim().isEmpty() ? repository.getPostBuildCommands() : ""));
                writer.println(String.format(REPOSITORY_WORKSPACE.concat("=%2$s"), repository.getId(), repository.getWorkspace() == null ? "" : repository.getWorkspace().getId()));

                for(Map.Entry<StringProperty, BooleanProperty> entry : repository.getGoals().entrySet()) {
                    writer.println(String.format(REPOSITORY_GOAL_ACTIVE.concat("=%3$s"),
                            repository.getId(),
                            entry.getKey().get(),
                            entry.getValue().get()));
                }
            }

            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                writer.close();
            }
        }
    }

    public static Workspace createDefaultWorkpace() {
        Workspace defaultWorkspace = new Workspace();
        defaultWorkspace.setName("default");
        defaultWorkspace.setId(0);
        defaultWorkspace.setActive(false);

        return defaultWorkspace;
    }

    public static int getNextAvailablePriority() {
        // Get the priority of the last element in the list and increment it by 1 for this repository
        MavenRepository last = null;
        Iterator<MavenRepository> iterator = Configuration.getInstance().getRepositories().iterator();

        while(iterator.hasNext()) {
            last = iterator.next();
        }

        if(last == null) return 1;
        else return last.getPriority() + 1;
    }
}
