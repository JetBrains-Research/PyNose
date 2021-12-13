class TestClass:

    def test_something(self):
        <weak_warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</weak_warning>

    def do_something(self):
        print("print")

    def test_something_else(self):
        <weak_warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</weak_warning>


def test_something_other():
    <weak_warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</weak_warning>


class AnotherClass:

    def test_something_another(self):
        print("print")