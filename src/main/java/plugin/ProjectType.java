package plugin;

public enum ProjectType {

    POM("pom.xml", "Maven"),
    GRADLE("build.gradle", "Gradle"),
    UNKNOWN("unknown", "unknown");

    private String fileName;
    private String type;

    ProjectType(String fileName, String type) {
        this.fileName = fileName;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return "This is " + type + " project";
    }
}
