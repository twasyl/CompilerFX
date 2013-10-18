package com.twasyl.compilerfx.utils;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.enums.Status;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MavenExecutor {

    public static List<MavenRepository.MavenOption> getMavenOptions() {

        final String[] command = OSUtils.isWindows() ?
                new String[] {"cmd.exe", "/C", Configuration.getInstance().getMavenCommand(), "--help"} :
                new String[] {Configuration.getInstance().getMavenCommand(), "--help"};
        final List<MavenRepository.MavenOption> options = new ArrayList<>();

        for(MavenRepository.MavenOption option : Configuration.getInstance().getCustomMavenOptions()) {
            options.add(option);
        }

        final ProcessBuilder builder = new ProcessBuilder(command);

        BufferedReader reader = null;

        try {
            final Process process = builder.start();

            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            MavenRepository.MavenOption option = null;
            int spaceIndex, argTokenIndex, commaIndex;

            while((line = reader.readLine()) != null) {
                if(line.startsWith("Options:")) {
                    line = reader.readLine();

                    do {
                        line = line.trim();

                        if(line.startsWith("-")) {
                            if(option != null) options.add(option);

                            option = new MavenRepository.MavenOption();

                            // Search options
                            spaceIndex = line.indexOf(' ');
                            argTokenIndex = line.indexOf("<arg>", spaceIndex);

                            // Test if the option has the <arg> token after its name
                            if(argTokenIndex != -1) {
                                option.setOptionName(line.substring(0, argTokenIndex + 5));
                            } else {
                                option.setOptionName(line.substring(0, spaceIndex));
                            }

                            // Extract the description
                            if(argTokenIndex != -1) option.setDescription(line.substring(argTokenIndex + 5).trim());
                            else option.setDescription(line.substring(spaceIndex).trim());

                            // Set the option
                            commaIndex = option.getOptionName().indexOf(',');
                            if(commaIndex != -1) option.setOption(option.getOptionName().substring(0, commaIndex));
                            else option.setOption(option.getOptionName().substring(0));
                        } else {
                           option.setDescription(option.getDescription() + " " + line.trim());
                        }
                    } while((line = reader.readLine()) != null);
                }
            }
        } catch (IOException e) {
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }

            return options;
        }
    }

    public static void execute(MavenRepository repository) {
        execute(repository, false);
    }

    public static void execute(MavenRepository repository, final boolean executePostBuildCommands) {
        ObservableList<MavenRepository> repositories = FXCollections.observableArrayList();
        repositories.add(repository);
        execute(repositories, executePostBuildCommands);
    }

    public static void execute(ObservableList<MavenRepository> repositories) {
        execute(repositories, false, true);
    }

    public static void execute(ObservableList<MavenRepository> repositories, final boolean executePostBuildCommands) {
        execute(repositories, executePostBuildCommands, true);
    }

    public static void execute(final ObservableList<MavenRepository> repositories, final boolean executePostBuildCommands, final boolean stopIfFailure) {

        final File mavenCommand = new File(Configuration.getInstance().getMavenCommand());
        boolean commandValid = true;
        String message = null;

        if(!mavenCommand.exists()) {
            commandValid = false;
            message = String.format(
                    FXMLLoader.getResourceBundle().getString("message.error.mavenCommandDoesNotExist"),
                    Configuration.getInstance().getMavenCommand());
        }

        if(commandValid && !mavenCommand.canExecute()) {
            commandValid = false;
            message = String.format(FXMLLoader.getResourceBundle().getString("message.error.mavenCommandNotExecutable"));
        }

        if(!commandValid) {
            Dialog.showErrorDialog(null,
                    FXMLLoader.getResourceBundle().getString("dialog.title.error"), message);
        } else {
            Runnable run = new Runnable() {
                @Override
                public void run() {

                    final ProcessBuilder processBuilder = new ProcessBuilder();
                    final List<String> command = new ArrayList<>();

                    Process process = null;
                    File repositoryDirectory = null;
                    final boolean isWindows = OSUtils.isWindows();

                    for (final MavenRepository repository : repositories) {

                        repositoryDirectory = new File(repository.getPath());

                        if(!repositoryDirectory.exists()) {
                            Dialog.showErrorDialog(null,
                                    FXMLLoader.getResourceBundle().getString("dialog.title.error"),
                                    String.format(
                                            FXMLLoader.getResourceBundle().getString("message.error.canNotCompileNotExistingRepository"),
                                            repository.getRepositoryName()));
                            if(stopIfFailure) break;
                        } else {
                            repository.setLastExecutionStack(null);

                            try {
                                Configuration.getInstance().currentBuildsProperty().add(repository);

                                command.clear();
                                if(isWindows) {
                                    command.add("cmd.exe");
                                    command.add("/C");
                                }

                                command.add(Configuration.getInstance().getMavenCommand());

                                if(repository.getOptions() != null && !repository.getOptions().trim().isEmpty()) {
                                    for(String option : repository.getOptions().split(" ")) {
                                        command.add(option);
                                    }
                                }

                                for (Map.Entry<StringProperty, BooleanProperty> entry : repository.getGoals().entrySet()) {
                                    if (entry.getValue().get()) {
                                        command.add(entry.getKey().get());
                                    }
                                }

                                repository.setStatus(Status.COMPILING);

                                processBuilder.command(command);
                                processBuilder.directory(new File(repository.getPath()));

                                process = processBuilder.start();
                                repository.setActiveProcess(process);

                                (new Thread(new OutputProcessRunnable(process.getInputStream(), repository))).start();
                                (new Thread(new OutputProcessRunnable(process.getErrorStream(), repository))).start();

                                process.waitFor();

                                if (stopIfFailure && process.exitValue() != 0) break;

                                if(executePostBuildCommands && repository.getPostBuildCommands() != null &&
                                        !repository.getPostBuildCommands().trim().isEmpty()) {
                                    repository.setStatus(Status.POST_BUILD);

                                    Pattern regex = Pattern.compile("(.*?);");
                                    Matcher matcher = regex.matcher(repository.getPostBuildCommands());

                                    while(matcher.find()) {
                                        short groupIndex = 1;

                                        while (groupIndex <= matcher.groupCount()) {
                                            processBuilder.directory(new File(repository.getPath()));
                                            processBuilder.command(Arrays.asList(matcher.group(groupIndex).split(" ")));

                                            process = processBuilder.start();
                                            repository.setActiveProcess(process);

                                            (new Thread(new OutputProcessRunnable(process.getInputStream(), repository))).start();
                                            (new Thread(new OutputProcessRunnable(process.getErrorStream(), repository))).start();

                                            process.waitFor();

                                            if(process.exitValue() != 0) break;

                                            groupIndex += 2;
                                        }

                                        if(process.exitValue() != 0) break;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                break;
                            } finally {
                                if(repository.getStatus() != Status.ABORTED) {
                                    if (process == null || process.exitValue() != 0) repository.setStatus(Status.IN_ERROR);
                                    else repository.setStatus(Status.DONE);
                                }

                                repository.setActiveProcess(null);

                                Configuration.getInstance().currentBuildsProperty().remove(repository);
                            }
                        }
                    }

                    // Put status ready for all repos that are DONE
                    for(MavenRepository repository : repositories) {
                        if(repository.getStatus() == Status.DONE) {
                            repository.setStatus(Status.READY);
                        }
                    }
                }
            };

           Thread t = new Thread(run);
           t.start();
        }
    }

    public static void executePostBuildCommands(MavenRepository repository) {
        ObservableList<MavenRepository> repositories = FXCollections.observableArrayList();
        repositories.add(repository);
        executePostBuildCommands(repositories, true);
    }

    public static void executePostBuildCommands(final ObservableList<MavenRepository> repositories, final boolean stopIfFailure) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                final boolean isWindows = OSUtils.isWindows();
                final ProcessBuilder processBuilder = new ProcessBuilder();
                List<String> command = new ArrayList<>();

                Process process = null;
                File repositoryDirectory = null;

                for (final MavenRepository repository : repositories) {

                    repositoryDirectory = new File(repository.getPath());

                    if(!repositoryDirectory.exists()) {
                        Dialog.showErrorDialog(null,
                                FXMLLoader.getResourceBundle().getString("dialog.title.error"),
                                String.format(
                                        FXMLLoader.getResourceBundle().getString("message.error.canNotExecutePostBuildCommandsNotExistingRepository"),
                                        repository.getRepositoryName()));
                        if(stopIfFailure) break;
                    } else {
                        try {
                            if(repository.getPostBuildCommands() != null && !repository.getPostBuildCommands().trim().isEmpty()) {
                                Configuration.getInstance().currentBuildsProperty().add(repository);

                                repository.setStatus(Status.POST_BUILD);
                                repository.setLastExecutionStack(null);

                                Pattern regex = Pattern.compile("(.*?);");
                                Matcher matcher = regex.matcher(repository.getPostBuildCommands());

                                while(matcher.find()) {
                                    short groupIndex = 1;

                                    while (groupIndex <= matcher.groupCount()) {
                                        processBuilder.directory(new File(repository.getPath()));
                                        command.clear();

                                        if(isWindows) {
                                            command.add("cmd.exe");
                                            command.add("/C");
                                        }

                                        command.addAll(Arrays.asList(matcher.group(groupIndex).split(" ")));
                                        processBuilder.command(command);

                                        process = processBuilder.start();
                                        repository.setActiveProcess(process);

                                        (new Thread(new OutputProcessRunnable(process.getInputStream(), repository))).start();
                                        (new Thread(new OutputProcessRunnable(process.getErrorStream(), repository))).start();

                                        process.waitFor();

                                        if(process.exitValue() != 0) break;

                                        groupIndex += 2;
                                    }

                                    if(process.exitValue() != 0) break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            if (process == null || process.exitValue() != 0) repository.setStatus(Status.IN_ERROR);
                            else repository.setStatus(Status.DONE);

                            repository.setActiveProcess(null);

                            Configuration.getInstance().currentBuildsProperty().remove(repository);
                        }
                    }
                }

                // Put status ready for all repos that are DONE
                for(MavenRepository repository : repositories) {
                    if(repository.getStatus() == Status.DONE) {
                        repository.setStatus(Status.READY);
                    }
                }
            }
        };

        Thread t = new Thread(run);
        t.start();
    }

    private static class OutputProcessRunnable implements Runnable {

        private InputStream stream;
        private MavenRepository repository;

        public OutputProcessRunnable(InputStream stream, MavenRepository repository) {
            this.stream = stream;
            this.repository = repository;
        }

        private InputStream getStream() {
            return stream;
        }

        private void setStream(InputStream stream) {
            this.stream = stream;
        }

        private MavenRepository getRepository() {
            return repository;
        }

        private void setRepository(MavenRepository repository) {
            this.repository = repository;
        }

        @Override
        public void run() {
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));

            try {
                while((line = in.readLine()) != null) {
                    try {
                        repository.setLastExecutionStack(
                                (repository.getLastExecutionStack() == null ? "" : repository.getLastExecutionStack())
                                    .concat(line.concat("\n"))
                        );
                    } catch(NullPointerException npe) {
                    }
                }
            } catch (IOException e) {
                Logger.getAnonymousLogger().log(Level.WARNING, null, e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
