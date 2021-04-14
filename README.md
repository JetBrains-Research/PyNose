# PyCharm Test Smell Plugin

## Get Started

### Download and Open Project

After cloning this project, open it with IntelliJ IDEA.

![open-with-intellij-idea](./README.assets/open-with-intellij-idea.png)

Select the project in the pop-up window and open the project. The IDE will prompt detecting a Gradle project. Please load this Gradle project by clicking "Load Gradle Project" option.

![load-gradle-project-prompt](./README.assets/load-gradle-project-prompt.png)

The IDE may also prompt that this is a project from the web. Click "Trust Project" if you trust this project. You may need to check if JDK is 1.8 and manually install and set up it to be 1.8.

![trust-project-prompt](./README.assets/trust-project-prompt.png)

### Set up License and Python

Since this tool is developed in PyCharm Professional, a license is required. We have to modify some files in order to enter the license.

Navigate to [`build.gradle`](./build.gradle) in the project root, select line 31-41 and press ` Ctrl+/` (`Cmd+/` on macOS) to comment them out. The `intellij` section will look like this.

```groovy
// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = '2020.2.1'
    type = 'PY'
    downloadSources = false
    plugins 'Pythonid'
//    tasks {
//        runIde {
//            args(
//                    'test_smell',
//                    project.hasProperty('myPath') ? myPath : '',
//                    project.hasProperty('myPython') ? myPython : '',
//                    project.hasProperty('myOutDir') ? myOutDir : ''
//            )
//            jvmArgs('-Djava.awt.headless=true')
//        }
//    }
}
```

Then, navigate to [`src/main/resources/META-INF/plugin.xml`](./src/main/resources/META-INF/plugin.xml), select line 43 and press `Ctrl+/` (`Cmd+/` on macOS) to comment it out. The `extensions` section will look like this.

```xml
  <extensions defaultExtensionNs="com.intellij">
    <toolWindow id="Sample Calendar" secondary="true" icon="AllIcons.General.Modified" anchor="right"
                factoryClass="org.intellij.sdk.toolWindow.MyToolWindowFactory"/>
<!--    <appStarter implementation="PluginRunner"/>-->
  </extensions>
```

After that, open the Gradle side panel, expand the list as shown below, and finally double click "runIde" to start the IDE.

![runide](./README.assets/runide.png)

The PyCharm splash screen should appear, then an error about the license should appear. Click "Enter License" to continue.

![license-issue-prompt](./README.assets/license-issue-prompt.png)

If you are using JetBrains account, just enter the username and password and click "Activate".

![enter-license](./README.assets/enter-license.png)

When you see the welcome screen, it means you have successfully entered the license. Now we need to set up Python for PyCharm. Click "Configure" and then "Settings".

![pycharm-welcome-config-setting](./README.assets/pycharm-welcome-config-setting.png)

Go to "Python Interpreter" section. If no interpreter is shown, click the top-right gear icon and choose "Add..." from its small pop-up menu.

![python-interpreter-empty](./README.assets/python-interpreter-empty.png)

You can just choose an existed system interpreter.

![sys-interpreter](./README.assets/sys-interpreter.png)

You should be able to see the interpreter as well as all its packages from PyPI in the "Python Interpreter" section. Remember the name of the Python Interpreter. In my case, it is "Python 3.8". We need to use this name later.

![python-interpreter-final-step](./README.assets/python-interpreter-final-step.png)

Click "OK" and then close the welcome screen. Now the license and Python is set up.

### Run the Tool for Test Smell Analysis

First, we need to undo the two modifications to the code we have done above. If you get this project by cloning with git, and you did not modify anything else, you can open a terminal, navigate to the project root, and type

```bash
git reset --hard HEAD
```

to undo the modification.

Then open the `runner.py` in the project root. We need to modify four variables.

- `DETECTOR_OUTPUT` stores the output of the tool. Make sure it is existed before running the tool.
- `PLUGIN_ROOT` is the root of the detector project (i.e. the folder that contains `runner.py` (this file) and `build.gradle`).
- `REPO_PREFIX` is the folder containing all repositories that need to be analyzed. The detector will treat each folder inside this `REPO_PREFIX` as an individual repository.
- `PYTHON_INTERPRETER_NAME` is the name of the Python interpreter. Use the one shown in the last step of ["Set up License and Python"](#set-up-license-and-python) section (in my case it is "Python 3.8").

Just save and run this script. The script will then start the tool.

### Generate Test Smell Statistics

You can run `get_csv_stats.py` to generate an aggregated statistics for all analyzed repositories. Remember to set `DETECTOR_OUTPUT` variable in that file.
