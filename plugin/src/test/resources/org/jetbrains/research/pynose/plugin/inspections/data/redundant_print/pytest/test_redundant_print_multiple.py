class TestClass:

    def test_something(self):
        <warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</warning>

    def do_something(self):
        print("print")

    def test_something_else(self):
        <warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</warning>


def test_something_other():
    <warning descr="Print statements are considered to be redundant in unit tests, consider removing them">print("print")</warning>


class AnotherClass:

    def test_something_another(self):
        print("print")