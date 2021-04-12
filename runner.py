import os
import subprocess
from pathlib import Path

import tqdm

DETECTOR_OUTPUT = Path('C:\\Users\\tjwan\\PycharmProjects\\new_test_repo_outDir')
PLUGIN_ROOT = Path('C:\\Users\\tjwan\\IdeaProjects\\pycharm')
REPO_PREFIX = Path('C:\\Users\\tjwan\\PycharmProjects\\new_test_repo')
PYTHON_INTERPRETER_NAME = 'Python 3.8'

PROJECT_LIST = [p for p in REPO_PREFIX.iterdir() if p.is_dir()]

with (DETECTOR_OUTPUT / 'log.txt').open('w') as f:
    pass

for project in tqdm.tqdm(PROJECT_LIST):
    process = subprocess.run(
        [PLUGIN_ROOT / ('gradlew.bat' if os.name == 'nt' else 'gradlew'), '-p', str(PLUGIN_ROOT), 'runIde',
         f'-PmyPath="{project}"',
         f'-PmyPython="{PYTHON_INTERPRETER_NAME}"',
         f'-PmyOutDir="{DETECTOR_OUTPUT}"'], capture_output=True, text=True)

    with (DETECTOR_OUTPUT / 'log.txt').open('a') as f:
        f.write(f'{project=}\n====STDOUT====\n{process.stdout}\n====STDERR===={process.stderr}\n\n')
