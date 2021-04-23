package pynose.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class PyNoseGUIFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var pyNoseGUI = new PyNoseGUI(project);
        var contentFactory = ContentFactory.SERVICE.getInstance();
        var content = contentFactory.createContent(pyNoseGUI.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
