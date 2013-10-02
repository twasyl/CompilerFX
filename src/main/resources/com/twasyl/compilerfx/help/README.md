# Introduction

**CompilerFX** is an application for compiling multiple Maven repositories. 
You can add existing repositories and then compile them sequentially but also compile them individually.  
It is also possible to execute post build commands after each repository compilation.  
All outputs
can be displayed live in a window.

# How does it work

**CompilerFX** uses a local ***mvn*** command that you must configure using the **Tools** menu.  
Each build is launched inside the repository folder. In order to determine if the build is a success
or not, the application checks the result of the mvn command.

## Workspaces

A workspace allows you to have multiple repositories available in ***CompilerFX***, grouped
by versions, folders, or whatever makes sense for you. Bulk operations like removall and global
compilation are always done in the current displayed workspace.  
  
If you want to delete a workspace, right click on the tab and choose delete. If the workspace
is empty, it will directly be deleted otherwhise a window will ask you if you want to trash the 
repositories present in it or move them to another one.  
  
To rename a workspace, right click on the tab and choose rename. To validate the change press enter and to cancel 
press escape.  
  
## Adding a repository

By clicking on the *Add repository* button, you will be able to add a repository to **CompilerFX**.
If you don't enter a name but click on the *Browse* button, the name of the chosen directory will be
added as name for the repository. Of course you can change it.  
**Warning:** you can not add a repository that doesn't contain a **pom.xml** file.

## Maven goals

Currently **CompilerFX** only supports ***clean*** and ***install*** maven goals. ***clean***
is executed before ***install***. Custom goals will be available in a next version.

## Maven options

Maven option can be added when adding/editing a repository using the **Options** field.

## Post build commands

Post build commands are executed after the maven build. Each command is executed separately
in a process. If the maven build fails, post build commands won't be executed. If one post build command
fails, the others (if exist) aren't executed. Each command **must** end with a **;** sign in order to be valid.  

Example:  
	cd /test;  
	rm file.txt;

## Build abortion

It is possible to abort a build by right-clicking on the repository which is compiling. By doing so, the build is aborted
and if there are post build commands, they won't be launched. Moreover if a build belongs to a bulk build operations,
next builds won't be launched.
