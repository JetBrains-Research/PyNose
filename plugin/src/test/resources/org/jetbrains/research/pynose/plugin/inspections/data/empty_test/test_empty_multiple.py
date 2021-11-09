import unittest


class SomeClass(unittest.TestCase):
    def <warning descr="Test does not contain executable statements, consider removing it">test_something</warning>(self):
        pass

    def do_something(self):
        pass

    def <warning descr="Test does not contain executable statements, consider removing it">test_something_else</warning>(self):
        pass


class OtherClass(unittest.TestCase):
    def <warning descr="Test does not contain executable statements, consider removing it">test_something_other</warning>(self):
        pass


class AnotherClass:
    def test_something_another(self):
        pass
