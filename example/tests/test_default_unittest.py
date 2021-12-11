import unittest


class SomeClass(unittest.TestCase):
    def some_fun(self):
        pass


class MyTestCase(SomeClass):
    def test_something(self):
        pass
