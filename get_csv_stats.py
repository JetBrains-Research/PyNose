from pathlib import Path
import json
import pandas as pd


DETECTOR_OUTPUT = Path('C:\\Users\\tjwan\\PycharmProjects\\new_test_repo_outDir')
JSON_FILE_PATHS = [p for p in DETECTOR_OUTPUT.iterdir() if p.is_file() and p.suffix == '.json']
all_test_smells = {}
count = 0

aggregated_lines = []

for json_file_path in JSON_FILE_PATHS:
    with json_file_path.open() as f:
        repo_stats = json.load(f)

    lines = []

    for test_file_stats in repo_stats:
        for test_case in test_file_stats['testCases']:
            test_case['detectorResults'].sort(key=lambda r: r['name'])
            smell_result = []
            for smell in test_case['detectorResults']:
                all_test_smells[smell['name']] = None
                smell_result.append(smell['hasSmell'])
            lines.append([test_file_stats['name'], test_case['name']] + smell_result)

    df = pd.DataFrame(lines, columns=['file_name', 'test_case'] + list(all_test_smells.keys()))
    df.to_csv(json_file_path.parent / f'{json_file_path.stem}.csv', index=False)
    count += 1

    smell_count = [sum(df[smell]) for smell in all_test_smells.keys()]
    aggregated_lines.append([json_file_path.stem] + smell_count)

print(f'Converted {count} JSON file(s).')

aggregated_df = pd.DataFrame(aggregated_lines, columns=['repo_name'] + list(all_test_smells.keys()))
aggregated_df.to_csv(DETECTOR_OUTPUT / 'aggregated.csv', index=False)
print('Aggregated result saved')
