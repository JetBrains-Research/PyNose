import unittest


class TestClass:
    def <warning descr="Test does not contain executable statements, consider removing it">test_something</warning>(self):
        pass

    def do_something(self):
        pass

    def <warning descr="Test does not contain executable statements, consider removing it">test_something_else</warning>(self):
        pass


def <warning descr="Test does not contain executable statements, consider removing it">test_something_other</warning>():
    pass


def do_something():
    pass


class AnotherClass:
    def test_something_another(self):
        pass
