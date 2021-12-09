# PyNose: a Test Smell Detector for Python

PyNose is a test smell detector tool for Python. The tool runs inside PyCharm IDE.

## Description

PyNose can be used inside the IDE to study the test smells within a specific
opened project. This can help python developers avoid test smells in their code.

[comment]: <> (installation??)

When you open a project in PyCharm, a number of inspections will be available for usage.

Some inspections are initially disabled, however you can enable them from the settings.

## Available inspections

| Inspections                   |Unittest|Pytest |Quick fix                               |Warning level|Description|
| -------------                 |:------:|:-----:|:--------------------------------------:|:-----------:|:----------|
|Assertion Roulette             |+       |+      |-                                       |Disabled*     | A test case contains more than one assertion statement without an explanation|
|Conditional Test Logic         |+       |+      |-                                       |Disabled     | A test case contains one or more control statements (i.e., if, for, while)|
|Constructor Initialization     |+       |-      |+ (move logic to setup)                 |Weak warning | A test suite contains a constructor declaration (an __init__ method)|
|Default Test                   |+       |-      |+ (suggest rename refactoring)          |Weak warning | A test suite is called MyTestCase|
|Duplicate Assert               |+       |+      |+ (remove duplicate)                    |Warning      | A test case contains more than one assertion statement with the same parameters|
|Empty Test                     |+       |+      |+ (safe delete)                         |Warning      | A test case does not contain a single executable statement|
|Exception Handling             |+       |+      |+ (replace with framework raise handler)|Warning      | A test case contains either the try/except statement or the raise statement|
|Lack of Cohesion of Test Cases |+       |+      |-                                       |Disabled     | Test suites in a test case are not cohesive according to pairwise cosine similarities metric|
|Magic Number Test              |+       |+      |-                                       |Disabled     | A test case contains an assertion statement that contains a numeric literal as an argument|
|Obscure In-Line Setup          |+       |+      |-                                       |Disabled     | A test case contains ten or more local variables declarations|
|Redundant Assertion            |+       |+      |+ (remove assertion)                    |Warning      | Test case contains assertions the result of which never changes (i.e., assert 1 == 1)|
|Redundant Print                |+       |+      |+ (remove statement)                    |Disabled     | A test case invokes the print() function|
|Sleepy Test                    |+       |+      |+ (remove statement)                    |Disabled     | A test case invokes the time.sleep() function with no comment|
|Suboptimal Assert              |+       |-      |+ (replace with optimal)                |Warning      | A test case contains at least one of the suboptimal asserts**|
|Test Maverick                  |+       |+      |-                                       |Disabled     | A test suite contains at least one test case that does not use a single field from the setup method|

*if enabled - weak warning

**list of suboptimal assertions if available here TODO