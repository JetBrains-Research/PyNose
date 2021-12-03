import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        x = 5
        print("print")

    def do_something(self):
        print("print")

    def test_something_else(self):
        x = 6
        print("print")


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        print("print")


class AnotherClass:

    def test_something_another(self):
        print("print")