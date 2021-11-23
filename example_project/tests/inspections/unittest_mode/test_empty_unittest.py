import unittest


class SomeClass(unittest.TestCase):
    def test_something(self):
        pass

    def do_something(self):
        pass

    def do_something_else(self):
        self.test_something()
        pass

    def test_something_else(self):
        pass


class OtherClass(unittest.TestCase):
    def test_something_other(self):
        pass


class AnotherClass:
    def test_something_another(self):
        pass
