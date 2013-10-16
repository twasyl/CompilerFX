package com.twasyl.compilerfx.utils;

public class OSUtils {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public enum OperatingSystem {
        WINDOWS, UNIX, MAC
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static OperatingSystem getOperatingSystem() {
        OperatingSystem os = null;

        if (isWindows()) {
            os = OperatingSystem.WINDOWS;
        } else if(isUnix()) {
            os = OperatingSystem.UNIX;
        } else if(isMac()) {
            os = OperatingSystem.MAC;
        }

        return os;
    }
}
