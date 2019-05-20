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

import static plugin.ProjectType.*;

public class Manager implements ProjectComponent {

    private final Project myProject;

    public Manager(Project project) {
        myProject = project;
    }

    @Override
    public void projectOpened() {

        DumbService.getInstance(myProject).runWhenSmart(() -> {

            ProjectType type = defineProjectType();

            Notification notification = new Notification("ProjectOpenNotification",
                    "Project type",
                    type.getMessage(),
                    NotificationType.INFORMATION);

            if (type != UNKNOWN)
                notification.addAction(NotificationAction.createSimple(type.getFileName(),
                                                                    () -> openConfigFile(type.getFileName())));

            notification.notify(myProject);
        });
    }

    private ProjectType defineProjectType() {

        String basePath = myProject.getBasePath();

        if (basePath != null) {
            File projectDirectory = new File(basePath);
            List<String> configFiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(projectDirectory.list())));

            if (configFiles.contains(POM.getFileName())) {
                return POM;
            }
            if (configFiles.contains(GRADLE.getFileName())) {
                return GRADLE;
            }
        }

        return UNKNOWN;
    }

    private void openConfigFile(String configFileName) {

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
