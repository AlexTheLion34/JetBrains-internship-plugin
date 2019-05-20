package plugin;

import com.intellij.notification.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.util.*;

public class Manager implements ProjectComponent {

    private final Project myProject;

    private final String POM = "pom.xml";
    private final String GRADLE = "build.gradle";
    private final String UNKNOWN = "unknown";

    private String configFileName;

    public Manager(Project project) {
        myProject = project;
    }

    @Override
    public void projectOpened() {

        DumbService.getInstance(myProject).runWhenSmart(() -> {

            Notification notification = new Notification("ProjectOpenNotification",
                    "Project type",
                    defineDependencyManager(),
                    NotificationType.INFORMATION);

            if (!configFileName.equals(UNKNOWN))
                notification.addAction(NotificationAction.createSimple(configFileName, this::openConfigFile));

            notification.notify(myProject);
        });
    }

    private String defineDependencyManager() {

        String basePath = myProject.getBasePath();

        if (basePath != null) {
            File projectDirectory = new File(basePath);
            List<String> configFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(projectDirectory.list())));

            if (configFiles.contains(POM)) {
                configFileName = POM;
                return "This is Maven project";
            }
            if (configFiles.contains(GRADLE)) {
                configFileName = GRADLE;
                return "This is Gradle project";
            }
        }
        configFileName = UNKNOWN;
        return "This is unknown project";
    }

    private void openConfigFile() {

        List<VirtualFile> allConfigFiles = new ArrayList<>(FilenameIndex
                                                     .getVirtualFilesByName(myProject,
                                                                            configFileName,
                                                                            GlobalSearchScope.projectScope(myProject)));
        String projectBaseDirectory = myProject.getBaseDir().getPath();

        Optional<VirtualFile> rootConfigFile = allConfigFiles.stream()
                                                  .filter(file ->
                                                          file.getPath()
                                                                  .contains(projectBaseDirectory + "/" + configFileName))
                                                  .findFirst();
        rootConfigFile.ifPresent(virtualFile -> new OpenFileDescriptor(myProject, virtualFile).navigate(true));
    }
}
