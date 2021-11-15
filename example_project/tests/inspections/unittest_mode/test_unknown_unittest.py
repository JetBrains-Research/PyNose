import unittest


class SomeClass(unittest.TestCase):

    def test_something(self):
        pass

    def do_something(self):
        pass

    def test_something_else(self):
        x = 2


class OtherClass(unittest.TestCase):

    def test_something_other(self):
        print("Hello")


class AnotherClass:

    def test_something_another(self):
        x = 3 + 6
