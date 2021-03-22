# PyCharm Test Smell Plugin

# Usage

Open this project in IntelliJ IDEA, and change the `pathString` in the `PluginRunner`'s `main` method to the path of the
project. Then run the task `runIde`. It should print the result and save the result to a text file right next to the 
project directory.

For example, if the project is named `PythonTestSmellTestProject` whose path is `/path/to/PythonTestSmellTestProject`, 
then the `pathString` in `PluginRunner` should be `/path/to/PythonTestSmellTestProject` and the text file output is 
`/path/to/PythonTestSmellTestProject.txt`.
