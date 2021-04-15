from dataclasses import dataclass
import json
from pathlib import Path
from typing import List, Optional

import pandas as pd
from dataclasses_json import dataclass_json, LetterCase


@dataclass_json(letter_case=LetterCase.CAMEL)
@dataclass
class DetectorResult:
    name: str
    has_smell: bool


@dataclass_json(letter_case=LetterCase.CAMEL)
@dataclass
class TestCaseResult:
    name: str
    detector_results: List[DetectorResult]
    number_of_methods: Optional[int] = None


@dataclass_json(letter_case=LetterCase.CAMEL)
@dataclass
class FileResult:
    name: str
    test_cases: List[TestCaseResult]


@dataclass_json(letter_case=LetterCase.CAMEL)
@dataclass
class Result:
    result: List[FileResult]


DETECTOR_OUTPUT = Path('C:\\Users\\tjwan\\PycharmProjects\\new_test_repo_outDir')
JSON_FILE_PATHS = [p for p in DETECTOR_OUTPUT.iterdir() if p.is_file() and p.suffix == '.json']
ALL_SMELLS = None

count = 0
REPO_DATA_FRAMES = []
for json_file_path in JSON_FILE_PATHS:
    with json_file_path.open() as f:
        json_str = f.read()

    json_root = json.loads(json_str)
    if isinstance(json_root, list):
        json_root = {'result': json_root}
        result = Result.from_json(json.dumps(json_root))
    else:
        result = Result.from_json(json_str)
    lines = []
    for test_file, test_case in ((tf, tc) for tf in result.result for tc in tf.test_cases):
        line = [json_file_path.stem, test_file.name, test_case.name]

        detector_results = sorted(test_case.detector_results, key=lambda dr: dr.name)
        if ALL_SMELLS is None:
            ALL_SMELLS = [dr.name for dr in detector_results]
        else:
            assert ALL_SMELLS == [dr.name for dr in detector_results]

        for detector_result in detector_results:
            line.append(detector_result.has_smell)

        lines.append(line)
    df = pd.DataFrame(lines, columns=['repo_name', 'test_file', 'test_case'] + ALL_SMELLS)
    df.to_csv(json_file_path.parent / f'{json_file_path.stem}.csv', index=False)
    REPO_DATA_FRAMES.append(df)
    count += 1

print(f'Converted {count} JSON file(s).')

aggregated_lines = []
for repo_df in REPO_DATA_FRAMES:
    if len(repo_df) == 0:
        continue
    repo_name = repo_df['repo_name'][0]
    repo_test_file_count = len(set(repo_df['test_file']))
    repo_test_case_count = len(set(repo_df['test_case']))
    line = [repo_name, repo_test_file_count, repo_test_case_count]

    for smell in ALL_SMELLS:
        line.append(sum(repo_df[smell]))
    aggregated_lines.append(line)

aggregated_df = pd.DataFrame(aggregated_lines, columns=['repo_name', 'test_file_count', 'test_case_count'] + ALL_SMELLS)
aggregated_df.to_csv(JSON_FILE_PATHS[0].parent / 'aggregated.csv', index=False)
print('Aggregated result generated')
