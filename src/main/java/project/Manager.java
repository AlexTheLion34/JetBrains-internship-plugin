package project;

import com.intellij.notification.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.io.File;
import java.util.*;

public class Manager implements ProjectComponent {

    private final Project myProject;

    private String configFileName;

    public Manager(Project project) {
        myProject = project;
    }

    @Override
    public void projectOpened() {
        Notification notification = new Notification("ProjectOpenNotification",
                                                     "Type of project",
                                                      findDependencyManager(),
                                                      NotificationType.INFORMATION);
        notification.notify(myProject);
        notification.addAction(NotificationAction.createSimple(configFileName, new Runnable() {
            @Override
            public void run() {
                openConfigFile();
            }
        }));
    }

    private String findDependencyManager() {
        String basePath = myProject.getBasePath();
        if (basePath != null) {
            File projectDirectory = new File(basePath);
            List<String> configFiles = new ArrayList<>(Arrays.asList(projectDirectory.list()));
            if (configFiles.contains("pom.xml")) {
                configFileName = "pom.xml";
                return "This is Maven project";
            }
            if (configFiles.contains("build.gradle")) {
                configFileName = "build.gradle";
                return "This is Gradle project";
            }
        }
        return "This is unknown project";
    }

    private void openConfigFile() {
        List<VirtualFile> file = new ArrayList<>(FilenameIndex.getVirtualFilesByName(myProject, configFileName, GlobalSearchScope.projectScope(myProject)));
        System.out.println(file.get(0));
        new OpenFileDescriptor(myProject, file.get(0)).navigate(true);
    }
}
